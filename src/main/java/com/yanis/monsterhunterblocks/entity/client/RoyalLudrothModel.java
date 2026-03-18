package com.yanis.monsterhunterblocks.entity.client;

import com.yanis.monsterhunterblocks.MonsterHunterBlocks;
import com.yanis.monsterhunterblocks.entity.custom.RoyalLudrothEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RoyalLudrothModel extends GeoModel<RoyalLudrothEntity> {
    @Override
    public Identifier getModelResource(RoyalLudrothEntity animatable) {
        return Identifier.of(MonsterHunterBlocks.MOD_ID, "geo/royal_ludroth.geo.json");
    }

    @Override
    public Identifier getTextureResource(RoyalLudrothEntity animatable) {
        return Identifier.of(MonsterHunterBlocks.MOD_ID, "textures/entity/royal_ludroth.png");
    }

    @Override
    public Identifier getAnimationResource(RoyalLudrothEntity animatable) {
        return Identifier.of(MonsterHunterBlocks.MOD_ID, "animations/royal_ludroth.animation.json");
    }
}
