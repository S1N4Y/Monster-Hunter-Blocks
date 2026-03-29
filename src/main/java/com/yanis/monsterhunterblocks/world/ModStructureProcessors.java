package com.yanis.monsterhunterblocks.world;

import com.yanis.monsterhunterblocks.MonsterHunterBlocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

public class ModStructureProcessors {

    public static final StructureProcessorType<FoundationProcessor> FOUNDATION_PROCESSOR =
            Registry.register(
                    Registries.STRUCTURE_PROCESSOR,
                    Identifier.of(MonsterHunterBlocks.MOD_ID, "foundation_processor"),
                    () -> FoundationProcessor.CODEC);

    public static final StructureProcessorType<VoidToAirProcessor> VOID_TO_AIR_PROCESSOR =
            Registry.register(
                    Registries.STRUCTURE_PROCESSOR,
                    Identifier.of(MonsterHunterBlocks.MOD_ID, "void_to_air_processor"),
                    () -> VoidToAirProcessor.CODEC);

    public static void register() {
        MonsterHunterBlocks.LOGGER.info("Registering Structure Processors for " + MonsterHunterBlocks.MOD_ID);
    }
}
