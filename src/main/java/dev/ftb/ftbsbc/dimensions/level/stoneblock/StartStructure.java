package dev.ftb.ftbsbc.dimensions.level.stoneblock;

import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.prebuilt.PrebuiltStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class StartStructure extends StructureFeature<JigsawConfiguration> {
	public StartStructure() {
		super(JigsawConfiguration.CODEC, StartStructure::generatePieces0);
	}

	@Override
	public GenerationStep.Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	private static Optional<PieceGenerator<JigsawConfiguration>> generatePieces0(PieceGeneratorSupplier.Context<JigsawConfiguration> context0) {
		int x = context0.chunkPos().getMinBlockX();
		int z = context0.chunkPos().getMinBlockZ();

		if (x == 0 && z == 0) {
			return generatePieces(context0);
		}

		return Optional.empty();
	}

	private static Optional<PieceGenerator<JigsawConfiguration>> generatePieces(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
		StructureManager structureManager = context.structureManager();
		if (!(context.chunkGenerator() instanceof StoneblockChunkGenerator stoneblockChunkGenerator)) {
			return Optional.empty();
		}

		ChunkPos chunkPos = context.chunkPos();
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureSeed(context.seed(), chunkPos.x, chunkPos.z);
		PrebuiltStructure start = StoneBlockDataKjs.PREBUILT_STRUCTURES.get(stoneblockChunkGenerator.prebuiltStructure.toString());

		if (start == null) {
			FTBStoneBlock.LOGGER.warn("Unable to find [{}] in the prebuild structure list", stoneblockChunkGenerator.prebuiltStructure.toString());
			return Optional.empty();
		}

		StructureTemplate template = structureManager.getOrCreate(start.id);
		StructurePlaceSettings placeSettings = StartStructurePiece.makeSettings(template);

		BlockPos spawnPos = locateSpawn(template, placeSettings);
		int x = -spawnPos.getX();
		int y = -spawnPos.getY();
		int z = -spawnPos.getZ();

		BlockPos blockPos = new BlockPos(x, y, z);

		return Optional.of((builder, ctxt) -> {
			builder.addPiece(new StartStructurePiece(structureManager, start.id, blockPos, template));
		});
	}

	public static BlockPos locateSpawn(StructureTemplate template, StructurePlaceSettings placeSettings) {
		BlockPos spawnPos = BlockPos.ZERO;

		for (var info : template.filterBlocks(BlockPos.ZERO, placeSettings, Blocks.STRUCTURE_BLOCK)) {
			if (info.nbt != null && StructureMode.valueOf(info.nbt.getString("mode")) == StructureMode.DATA) {
				FTBStoneBlock.LOGGER.info("Found data block at [{}] with data [{}]", info.pos, info.nbt.getString("metadata"));

				if (info.nbt.getString("metadata").equalsIgnoreCase("spawn_point")) {
					spawnPos = info.pos;
				}
			}
		}

		return spawnPos;
	}
}
