package com.yanis.monsterhunterblocks;

import com.yanis.monsterhunterblocks.entity.ModEntities;
import com.yanis.monsterhunterblocks.entity.client.RoyalLudrothRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MonsterHunterBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ROYAL_LUDROTH, RoyalLudrothRenderer::new);
    }
}
