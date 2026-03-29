package com.yanis.monsterhunterblocks.item;

import com.yanis.monsterhunterblocks.MonsterHunterBlocks;
import com.yanis.monsterhunterblocks.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item ROYAL_LUDROTH_SPAWN_EGG = registerItem("royal_ludroth_spawn_egg",
            new SpawnEggItem(ModEntities.ROYAL_LUDROTH, 0xEED231, 0x5D430C, new Item.Settings()) {
                @Override
                public net.minecraft.text.Text getName(net.minecraft.item.ItemStack stack) {
                    return net.minecraft.text.Text.literal("Royal Ludroth Spawn Egg");
                }
            });

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MonsterHunterBlocks.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MonsterHunterBlocks.LOGGER.info("Registering Mod Items for " + MonsterHunterBlocks.MOD_ID);

        // Adds the spawn egg to the spawn eggs creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(ROYAL_LUDROTH_SPAWN_EGG);
        });
    }
}
