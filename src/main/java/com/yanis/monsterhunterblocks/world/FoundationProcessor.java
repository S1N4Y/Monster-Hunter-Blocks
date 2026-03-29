package com.yanis.monsterhunterblocks.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * Processeur de structure custom qui génère des fondations sous une structure.
 * Pour chaque bloc solide de la structure, il vérifie la colonne en dessous.
 * Si un trou (air, eau, bloc remplaçable) est trouvé, il le comble avec de la
 * pierre/terre jusqu'au sol solide.
 */
public class FoundationProcessor extends StructureProcessor {

    public static final MapCodec<FoundationProcessor> CODEC = MapCodec.unit(FoundationProcessor::new);

    // Profondeur maximale de fondation (évite les boucles infinies au-dessus du vide)
    private static final int MAX_DEPTH = 64;

    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlacementData data) {

        BlockPos blockPos = currentBlockInfo.pos();
        BlockState structureBlock = currentBlockInfo.state();

        // On ne génère des fondations que sous les blocs solides de la structure
        if (structureBlock.isAir() || structureBlock.isOf(Blocks.STRUCTURE_VOID)) {
            return currentBlockInfo;
        }

        // Vérifier si ce bloc est au bord inférieur ou au-dessus d'un trou
        BlockPos below = blockPos.down();
        if (below.getY() < world.getBottomY()) {
            return currentBlockInfo;
        }

        BlockState belowState = world.getBlockState(below);

        // Si le bloc en dessous est déjà solide, pas besoin de fondation
        if (isSolidGround(belowState)) {
            return currentBlockInfo;
        }

        // Combler la colonne vers le bas avec pierre/cobblestone/terre
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            BlockPos fillPos = blockPos.down(depth);
            if (fillPos.getY() < world.getBottomY()) {
                break;
            }

            BlockState existingState = world.getBlockState(fillPos);

            if (isSolidGround(existingState)) {
                // On a atteint le sol, on s'arrête
                break;
            }

            // Choisir le bloc de fondation en fonction de la profondeur
            BlockState fillBlock;
            if (depth <= 1) {
                // Première couche : même matériau que le bloc de la structure si possible
                fillBlock = structureBlock;
            } else if (depth <= 3) {
                // Couches intermédiaires : cobblestone
                fillBlock = Blocks.COBBLESTONE.getDefaultState();
            } else {
                // Couches profondes : stone
                fillBlock = Blocks.STONE.getDefaultState();
            }

            world.getChunk(fillPos).setBlockState(fillPos, fillBlock, false);
        }

        return currentBlockInfo;
    }

    /**
     * Vérifie si un bloc est considéré comme du sol solide.
     */
    private boolean isSolidGround(BlockState state) {
        if (state.isAir()) return false;
        if (state.isOf(Blocks.WATER)) return false;
        if (state.isOf(Blocks.LAVA)) return false;
        if (state.isReplaceable()) return false;
        // Les blocs comme l'herbe haute, les fleurs, etc. ne comptent pas
        if (state.isIn(BlockTags.REPLACEABLE)) return false;
        return true;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessors.FOUNDATION_PROCESSOR;
    }
}
