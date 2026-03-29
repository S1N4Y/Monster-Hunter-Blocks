package com.yanis.monsterhunterblocks.entity;

import com.yanis.monsterhunterblocks.MonsterHunterBlocks;
import com.yanis.monsterhunterblocks.entity.custom.RoyalLudrothEntity;
import com.yanis.monsterhunterblocks.entity.custom.RoyalLudrothPartEntity;
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
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build());

    // EntityType pour les sous-parties de hitbox (invisibles, pas de spawn naturel)
    public static final EntityType<RoyalLudrothPartEntity> ROYAL_LUDROTH_PART = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MonsterHunterBlocks.MOD_ID, "royal_ludroth_part"),
            FabricEntityTypeBuilder.<RoyalLudrothPartEntity>create(SpawnGroup.MISC, RoyalLudrothPartEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                    .disableSummon()
                    .disableSaving()
                    .trackRangeChunks(10)
                    .build());

    public static void registerModEntities() {
        MonsterHunterBlocks.LOGGER.info("Registering Entities for " + MonsterHunterBlocks.MOD_ID);
        FabricDefaultAttributeRegistry.register(ROYAL_LUDROTH, RoyalLudrothEntity.createRoyalLudrothAttributes());
    }
}
