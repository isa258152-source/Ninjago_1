package com.example.sourcedragons;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Right-click to enter Dragonious Form for 60 seconds. Cooldown: 5 minutes. */
public class DragoniousCoreItem extends Item {
    private static final int FORM_TICKS = 20 * 60;
    private static final int COOLDOWN_TICKS = 20 * 300;

    public DragoniousCoreItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, FORM_TICKS, 2));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, FORM_TICKS, 2));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, FORM_TICKS, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, FORM_TICKS, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, FORM_TICKS, 3));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, FORM_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, FORM_TICKS, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, FORM_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, FORM_TICKS, 0));

        ServerWorld sw = (ServerWorld) world;
        sw.spawnParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1, player.getZ(), 120, 1.5, 1.0, 1.5, 0.05);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1f, 1f);
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(stack);
    }
}
