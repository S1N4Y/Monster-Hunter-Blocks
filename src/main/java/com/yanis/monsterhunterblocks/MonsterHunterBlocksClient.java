package com.yanis.monsterhunterblocks;

import com.yanis.monsterhunterblocks.entity.ModEntities;
import com.yanis.monsterhunterblocks.entity.client.RoyalLudrothRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;

public class MonsterHunterBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ROYAL_LUDROTH, RoyalLudrothRenderer::new);
        // Les sous-parties sont invisibles (pas de modèle), mais il faut un renderer
        EntityRendererRegistry.register(ModEntities.ROYAL_LUDROTH_PART, EmptyEntityRenderer::new);
    }
}
