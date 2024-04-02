package gay.lemmaeof.tatervator;

import gay.lemmaeof.tatervator.block.TatervatorBlock;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tatervator implements ModInitializer {
	public static final String MODID = "tatervator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final Block TATERVATOR = Registry.register(
			BuiltInRegistries.BLOCK,
			new ResourceLocation(MODID, "tatervator"),
			new TatervatorBlock(Block.Properties.ofFullCopy(Blocks.FLOATATER))
	);

	public static final Item TATERVATOR_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			new ResourceLocation(MODID, "tatervator"),
			new BlockItem(TATERVATOR, new Item.Properties())
	);

	public static final TagKey<Block> NON_STICKY = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "non_sticky"));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("potatoes")))
				.register(entries -> entries.accept(TATERVATOR_ITEM));
	}
}