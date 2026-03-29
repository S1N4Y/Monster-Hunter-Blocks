package com.yanis.monsterhunterblocks;

import com.yanis.monsterhunterblocks.entity.ModEntities;
import com.yanis.monsterhunterblocks.item.ModItems;
import com.yanis.monsterhunterblocks.world.ModStructureProcessors;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonsterHunterBlocks implements ModInitializer {
	public static final String MOD_ID = "monster-hunter-blocks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModEntities.registerModEntities();
		ModStructureProcessors.register();

		LOGGER.info("Hello Fabric world!");
	}
}