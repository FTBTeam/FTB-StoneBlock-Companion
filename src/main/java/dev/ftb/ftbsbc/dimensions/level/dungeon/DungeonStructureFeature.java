package dev.ftb.ftbsbc.dimensions.level.dungeon;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class DungeonStructureFeature extends StructureFeature<DungeonStructureFeature.CustomJigsawConfiguration> {
	public static String lookingFor = null;

	public DungeonStructureFeature() {
		super(CustomJigsawConfiguration.CODEC, arg -> {
			int x = arg.chunkPos().getMinBlockX();
			int z = arg.chunkPos().getMinBlockZ();

			double v = circularDistance(BlockPos.ZERO, new Vec3i(x, 0, z));
			if (v >= arg.config().minStructureDistance && v <= arg.config().maxStructureDistance) {
				BlockPos blockPos = new BlockPos(arg.chunkPos().getMinBlockX(), Mth.clamp(Mth.randomBetween(new Random(), arg.heightAccessor().getMinBuildHeight(), arg.heightAccessor().getMaxBuildHeight()), arg.config().minHeight, arg.config().maxHeight), arg.chunkPos().getMinBlockZ());
				Pools.bootstrap();

				return addPieces(arg, PoolElementStructurePiece::new, blockPos, false, true);
			}

			return Optional.empty();
		});
	}

	public static double circularDistance(BlockPos from, Vec3i to) {
		float f = to.getX() - from.getX();
		float g = to.getY() - from.getY();
		float h = to.getZ() - from.getZ();

		return Math.sqrt(Math.pow(f, 2) + Math.pow(g, 2) + Math.pow(h, 2));
	}

	@Override
	public GenerationStep.@NotNull Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static Optional<PieceGenerator<CustomJigsawConfiguration>> addPieces(PieceGeneratorSupplier.Context<CustomJigsawConfiguration> arg, JigsawPlacement.PieceFactory arg2, BlockPos arg3, boolean bl, boolean bl2) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(arg.seed(), arg.chunkPos().x, arg.chunkPos().z);
		RegistryAccess registryAccess = arg.registryAccess();
		CustomJigsawConfiguration jigsawConfiguration = arg.config();
		ChunkGenerator chunkGenerator = arg.chunkGenerator();
		StructureManager structureManager = arg.structureManager();
		LevelHeightAccessor levelHeightAccessor = arg.heightAccessor();
		Predicate<Holder<Biome>> predicate = arg.validBiome();
		StructureFeature.bootstrap();
		Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		Rotation rotation = Rotation.getRandom(worldgenRandom);
		StructureTemplatePool structureTemplatePool = jigsawConfiguration.startPool().value();
		StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
		if (structurePoolElement == EmptyPoolElement.INSTANCE) {
			return Optional.empty();
		}
		PoolElementStructurePiece poolElementStructurePiece = arg2.create(structureManager, structurePoolElement, arg3, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureManager, arg3, rotation));
		BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
		int i = (boundingBox.maxX() + boundingBox.minX()) / 2;
		int j = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
		int k =  arg3.getY();
		if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j)))) {
			return Optional.empty();
		}

		int l = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
		poolElementStructurePiece.move(0, k - l, 0);
		return Optional.of((arg10, arg11) -> {
			List<PoolElementStructurePiece> list = Lists.newArrayList();
			list.add(poolElementStructurePiece);
			if (jigsawConfiguration.maxDepth() > 0) {
				int v = 262;
				AABB aABB = new AABB(i - v, k - v, j - v, i + v + 1, k + v + 1, j + v + 1);
				VoxelShape voxelShape = Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
				JigsawPlacement.Placer placer = new JigsawPlacement.Placer(registry, jigsawConfiguration.maxDepth(), arg2, chunkGenerator, structureManager, list, worldgenRandom);
				placer.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece, new MutableObject<>(voxelShape), 0));

				while(!placer.placing.isEmpty()) {
					JigsawPlacement.PieceState pieceState = placer.placing.removeFirst();
					placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor);
				}

				Objects.requireNonNull(arg10);
				list.forEach(arg10::addPiece);
			}
		});
	}

	public static class CustomJigsawConfiguration implements FeatureConfiguration {
		public static final Codec<CustomJigsawConfiguration> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(CustomJigsawConfiguration::startPool),
						Codec.intRange(0, 100).fieldOf("size").orElseGet(() -> 7).forGetter(CustomJigsawConfiguration::maxDepth),
						Codec.intRange(-300, 300).fieldOf("min_height").orElseGet(() -> -300).forGetter(CustomJigsawConfiguration::minHeight),
						Codec.intRange(-300, 300).fieldOf("max_height").orElseGet(() -> 300).forGetter(CustomJigsawConfiguration::maxHeight),
						Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_structure_distance").orElseGet(() -> 0).forGetter(CustomJigsawConfiguration::minStructureDistance),
						Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_structure_distance").orElseGet(() -> Integer.MAX_VALUE).forGetter(CustomJigsawConfiguration::maxStructureDistance)
				).apply(instance, CustomJigsawConfiguration::new)
		);

		private final Holder<StructureTemplatePool> startPool;
		private final int maxDepth;
		private final int minHeight;
		private final int maxHeight;
		private final int minStructureDistance;
		private final int maxStructureDistance;

		public CustomJigsawConfiguration(Holder<StructureTemplatePool> arg, int i, int minHeight, int maxHeight, int minStructureDistance, int maxStructureDistance) {
			this.startPool = arg;
			this.maxDepth = i;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
			this.minStructureDistance = minStructureDistance;
			this.maxStructureDistance = maxStructureDistance;
		}

		public int maxDepth() {
			return this.maxDepth;
		}

		public int minHeight() {
			return this.minHeight;
		}

		public int maxHeight() {
			return this.maxHeight;
		}

		public int minStructureDistance() {
			return minStructureDistance;
		}

		public int maxStructureDistance() {
			return maxStructureDistance;
		}

		public Holder<StructureTemplatePool> startPool() {
			return this.startPool;
		}

		@Override
		public String toString() {
			return "CustomJigsawConfiguration{" +
					"startPool=" + startPool +
					", maxDepth=" + maxDepth +
					", minHeight=" + minHeight +
					", maxHeight=" + maxHeight +
					", minStructureDistance=" + minStructureDistance +
					", maxStructureDistance=" + maxStructureDistance +
					'}';
		}
	}
}
