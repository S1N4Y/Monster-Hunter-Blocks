package com.yanis.monsterhunterblocks.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Empêche la génération de features (canne à sucre, dead bush, cactus, etc.)
 * à l'intérieur de la bounding box de la structure arenaludrothroyal.
 * 
 * IMPORTANT : N'utilise JAMAIS getChunk() avec chargement forcé pendant le worldgen,
 * sinon deadlock garanti. On utilise getChunk(..., false) qui retourne null
 * si le chunk n'est pas encore prêt.
 */
@Mixin(Feature.class)
public class StructureFeatureMixin {

    private static final Identifier ARENA_ID = Identifier.of("monster-hunter-blocks", "arenaludrothroyal");

    @Inject(method = "generateIfValid", at = @At("HEAD"), cancellable = true)
    private void preventFeaturesInsideArena(
            FeatureConfig config,
            StructureWorldAccess world,
            ChunkGenerator chunkGenerator,
            net.minecraft.util.math.random.Random random,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {

        Chunk chunk = null;
        try {
            // Récupérer le chunk SANS forcer le chargement
            chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS, false);
        } catch (Exception e) {
            // Si le chunk est en dehors de la région actuelle de décoration, ChunkRegion throw une exception
            return;
        }

        if (chunk == null) return; // Chunk pas encore prêt, on laisse passer

        Map<Structure, StructureStart> starts = chunk.getStructureStarts();
        if (starts.isEmpty()) return; // Pas de structure ici, sortie rapide

        // Vérifier si l'arène est dans ce chunk
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            Structure structure = entry.getKey();

            // Comparer par l'identifiant de la structure
            var structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
            Identifier id = structureRegistry.getId(structure);

            if (ARENA_ID.equals(id)) {
                StructureStart start = entry.getValue();
                if (start.hasChildren() && start.getBoundingBox().contains(pos)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
