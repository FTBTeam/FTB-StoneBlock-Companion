package dev.ftb.ftbsbc.dimensions.level.stoneblock;

import com.google.common.collect.ImmutableList;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.DimensionsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Random;

public class StartStructurePiece extends TemplateStructurePiece {
	public static final BlockIgnoreProcessor IGNORE_PROCESSOR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_VOID, Blocks.STRUCTURE_BLOCK));
	public final ResourceLocation startId;

	public StartStructurePiece(StructureManager structureManager, ResourceLocation id, BlockPos pos, StructureTemplate template) {
		super(DimensionsRegistry.START_STRUCTURE_PIECE.get(), 0, structureManager, id, id.toString(), makeSettings(template), pos);
		startId = id;
	}

	public StartStructurePiece(StructureManager structureManager, CompoundTag tag) {
		super(DimensionsRegistry.START_STRUCTURE_PIECE.get(), tag, structureManager, id -> makeSettings(structureManager.getOrCreate(id)));
		startId = new ResourceLocation(tag.getString("Template"));
	}

	public static StructurePlaceSettings makeSettings(StructureTemplate template) {
		Vec3i size = template.getSize();
		StructurePlaceSettings settings = new StructurePlaceSettings();
		settings.setIgnoreEntities(true);
		settings.addProcessor(IGNORE_PROCESSOR);
		settings.setRotationPivot(new BlockPos(size.getX() / 2, size.getY() / 2, size.getZ() / 2));
		settings.setRotation(Rotation.NONE);
		return settings;
	}

	@Override
	protected void handleDataMarker(String id, BlockPos pos, ServerLevelAccessor level, Random random, BoundingBox boundingBox) {
		if (id.equalsIgnoreCase("spawn_point")) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			level.getLevel().getServer().getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, level.getLevel().getServer());

			FTBStoneBlock.LOGGER.info("Found valid spawn marker at [{}] and setting for [{}]", pos, level.getLevel().dimension());
		} else {
			FTBStoneBlock.LOGGER.warn("No spawn_point tag found on data marker");
		}
	}
}
