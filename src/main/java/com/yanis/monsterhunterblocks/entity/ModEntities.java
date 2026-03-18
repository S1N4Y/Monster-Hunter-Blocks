package com.yanis.monsterhunterblocks.entity;

import com.yanis.monsterhunterblocks.MonsterHunterBlocks;
import com.yanis.monsterhunterblocks.entity.custom.RoyalLudrothEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<RoyalLudrothEntity> ROYAL_LUDROTH = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MonsterHunterBlocks.MOD_ID, "royal_ludroth"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RoyalLudrothEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 1.5f)).build());

    public static void registerModEntities() {
        MonsterHunterBlocks.LOGGER.info("Registering Entities for " + MonsterHunterBlocks.MOD_ID);
        // We register attributes here because the entity type is already registered during static initialization
        FabricDefaultAttributeRegistry.register(ROYAL_LUDROTH, RoyalLudrothEntity.createRoyalLudrothAttributes());
    }
}
