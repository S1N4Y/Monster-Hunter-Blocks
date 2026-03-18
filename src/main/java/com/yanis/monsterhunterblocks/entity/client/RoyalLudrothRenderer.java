package com.yanis.monsterhunterblocks.entity.client;

import com.yanis.monsterhunterblocks.entity.custom.RoyalLudrothEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalLudrothRenderer extends GeoEntityRenderer<RoyalLudrothEntity> {

    public RoyalLudrothRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new RoyalLudrothModel());
        this.shadowRadius = 0.8f;
    }

    // La méthode doit être ICI, à l'intérieur de la classe, avant la dernière
    // accolade !
    @Override
    protected float getDeathMaxRotation(RoyalLudrothEntity entityLivingBaseIn) {
        // Empêche le jeu de coucher le mob sur le flanc quand il meurt
        return 0.0F;
    }

} // <--- C'est CETTE accolade qui ferme le fichier !