package com.example.sourcedragons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.EnumMap;
import java.util.Map;

public class SourceDragons implements ModInitializer {
    public static final String MOD_ID = "sourcedragons";
    public static final Map<Element, Item> ESSENCES = new EnumMap<>(Element.class);
    public static Item DRAGONIOUS_CORE;

    @Override
    public void onInitialize() {
        for (Element e : Element.values()) {
            Item item = Registry.register(
                    Registries.ITEM,
                    Identifier.of(MOD_ID, e.id),
                    new EssenceItem(e, new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
            ESSENCES.put(e, item);
        }
        DRAGONIOUS_CORE = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "dragonious_core"),
                new DragoniousCoreItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            ESSENCES.values().forEach(entries::add);
            entries.add(DRAGONIOUS_CORE);
        });
    }
}
