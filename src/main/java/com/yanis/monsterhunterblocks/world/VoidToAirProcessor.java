package com.yanis.monsterhunterblocks.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class VoidToAirProcessor extends StructureProcessor {

    public static final MapCodec<VoidToAirProcessor> CODEC = MapCodec.unit(VoidToAirProcessor::new);

    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlacementData data) {

        // Si le bloc actuel du template est un Structure Void, on le remplace par de l'air.
        // On ne fait AUCUN appel à world.getBlockState() pour éviter les crashs de chargement de chunks (deadlock).
        if (currentBlockInfo.state().isOf(Blocks.STRUCTURE_VOID)) {
            return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    Blocks.AIR.getDefaultState(),
                    currentBlockInfo.nbt()
            );
        }

        return currentBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessors.VOID_TO_AIR_PROCESSOR;
    }
}
