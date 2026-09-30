package com.example.sourcedragons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EssenceItem extends Item {
    private static final int COOLDOWN_TICKS = 60; // 3 seconds
    private final Element element;

    public EssenceItem(Element element, Settings settings) {
        super(settings);
        this.element = element;
    }

    // Passive power while the essence is in your hand
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !selected || !(entity instanceof LivingEntity living)) return;
        switch (element) {
            case FIRE -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));
            case ICE -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, true, false));
            case EARTH -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
            case LIGHTNING -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, true, false));
            case TECH -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 1, true, false));
            case LIFE -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, false));
        }
    }

    // Active power on right-click
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);

        ServerWorld sw = (ServerWorld) world;
        switch (element) {
            case FIRE -> fire(sw, player);
            case ICE -> ice(sw, player);
            case EARTH -> earth(sw, player);
            case LIGHTNING -> lightning(sw, player);
            case TECH -> tech(sw, player);
            case LIFE -> life(sw, player);
        }
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(stack);
    }

    private void fire(ServerWorld w, PlayerEntity p) {
        Vec3d look = p.getRotationVec(1.0f);
        SmallFireballEntity ball = new SmallFireballEntity(w, p, look.multiply(1.5));
        ball.setPosition(p.getX() + look.x, p.getEyeY() + look.y - 0.1, p.getZ() + look.z);
        w.spawnEntity(ball);
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
    }

    private void ice(ServerWorld w, PlayerEntity p) {
        List<LivingEntity> near = w.getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(6), e -> e != p);
        for (LivingEntity e : near) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            e.setFrozenTicks(200);
        }
        w.spawnParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 80, 3, 1, 3, 0.05);
        w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
    }

    private void earth(ServerWorld w, PlayerEntity p) {
        List<LivingEntity> near = w.getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(5), e -> e != p);
        for (LivingEntity e : near) {
            e.damage(w.getDamageSources().playerAttack(p), 6.0f);
            e.addVelocity(0, 0.8, 0);
            e.velocityModified = true;
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1));
        w.spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 0.2, p.getZ(), 10, 2, 0.1, 2, 0.0);
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.8f, 0.7f);
    }

    private void lightning(ServerWorld w, PlayerEntity p) {
        HitResult hit = p.raycast(30.0, 0.0f, false);
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
        if (bolt != null) {
            bolt.setPosition(hit.getPos());
            w.spawnEntity(bolt);
        }
    }

    // TECH: scan pulse. Nearby mobs glow and get weakened; you get a Haste burst.
    private void tech(ServerWorld w, PlayerEntity p) {
        List<LivingEntity> near = w.getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(16), e -> e != p);
        for (LivingEntity e : near) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200, 2));
        w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 100, 4, 1, 4, 0.1);
        w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 1.5f);
    }

    // LIFE: heals you and nearby players, and grows nearby crops and plants.
    private void life(ServerWorld w, PlayerEntity p) {
        p.heal(8.0f);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
        for (PlayerEntity other : w.getEntitiesByClass(PlayerEntity.class, p.getBoundingBox().expand(8), e -> e != p)) {
            other.heal(6.0f);
            other.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
        }
        BlockPos c = p.getBlockPos();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL), w, c.add(dx, dy, dz));
                }
            }
        }
        w.spawnParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 60, 4, 1, 4, 0.0);
        w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1f, 1f);
    }
}
