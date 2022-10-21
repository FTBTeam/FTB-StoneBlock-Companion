package dev.ftb.ftbsbc.dimensions.level.stoneblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.DimensionsMain;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNullableByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@ParametersAreNullableByDefault
@MethodsReturnNonnullByDefault
public class StoneblockChunkGenerator extends NoiseBasedChunkGenerator {
	public static final ResourceLocation STRUCTURE_SET = new ResourceLocation(FTBStoneBlock.MOD_ID, "stoneblock_structure_set");

	public static final Codec<StoneblockChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(StoneblockChunkGenerator::getStructureSetRegistry),
			RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(StoneblockChunkGenerator::getNoiseRegistry),
			RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(StoneblockChunkGenerator::getBiomeRegistry),
			ResourceLocation.CODEC.fieldOf("prebuilt_structure").forGetter(arg -> arg.prebuiltStructure)
	).apply(instance, instance.stable(StoneblockChunkGenerator::new)));

	public static final NoiseGeneratorSettings SETTINGS = new NoiseGeneratorSettings(
			NoiseSettings.create(-DimensionsMain.SIZE, DimensionsMain.HEIGHT,
					new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D),
					new NoiseSlider(0.9375D, 3, 0),
					new NoiseSlider(2.5D, 4, -1),
					1,
					2,
					TerrainProvider.caves()
			),
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			none(),
			SurfaceRules.state(Blocks.STONE.defaultBlockState()),
			32,
			false,
			false,
			false,
			false
	);

	private final Registry<Biome> biomeRegistry;
	private final Registry<NormalNoise.NoiseParameters> noiseRegistry;
	private Set<Biome> biomesWithCarvers;
	private final HolderSet<StructureSet> structures;
	public final ResourceLocation prebuiltStructure;

	public StoneblockChunkGenerator(@NotNull Registry<StructureSet> structureSetRegistry, @NotNull Registry<NormalNoise.NoiseParameters> noiseParameterRegistry, Registry<Biome> registry, ResourceLocation prebuiltStructure) {
		super(structureSetRegistry, noiseParameterRegistry, new StoneBlockBiomeSource(registry), 0, Holder.direct(SETTINGS));
		structures = getSet(structureSetRegistry);
		this.biomeRegistry = registry;
		this.noiseRegistry = noiseParameterRegistry;
		this.prebuiltStructure = prebuiltStructure;
	}

	private static HolderSet<StructureSet> getSet(Registry<StructureSet> structureSetRegistry) {
		return structureSetRegistry.getOrCreateTag(TagKey.create(Registry.STRUCTURE_SET_REGISTRY, STRUCTURE_SET));
	}

	private static NoiseRouterWithOnlyNoises none() {
		return new NoiseRouterWithOnlyNoises(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
	}

	@Override
	public Stream<Holder<StructureSet>> possibleStructureSets() {
		return (Stream<Holder<StructureSet>>) ((HolderSet) this.structures).stream();
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	public Registry<StructureSet> getStructureSetRegistry() {
		return structureSets;
	}

	public Registry<Biome> getBiomeRegistry() {
		return biomeRegistry;
	}

	public Registry<NormalNoise.NoiseParameters> getNoiseRegistry() {
		return noiseRegistry;
	}

	@Override
	public ChunkGenerator withSeed(long s) {
		return new StoneblockChunkGenerator(structureSets, noiseRegistry, biomeRegistry, prebuiltStructure);
	}

	@Override
	public Climate.Sampler climateSampler() {
		return new Climate.Sampler(DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), DensityFunctions.constant(0.0),
				DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), Collections.emptyList());
	}


	@Override
	public int getSpawnHeight(LevelHeightAccessor arg) {
		return 0;
	}

	@Override
	public void applyCarvers(WorldGenRegion region, long seed1, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		if (biomesWithCarvers == null) {
			biomesWithCarvers = new HashSet<>();
			Registry<Biome> registry = region.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

			for (StoneBlockDataKjs data : StoneBlockDataKjs.BIOMES) {
				if (data.carvers) {
					biomesWithCarvers.add(registry.getOrThrow(data.biome));
				}
			}
		}

		BiomeManager biomeManager1 = biomeManager.withDifferentSource((ix, jx, kx) -> biomeSource.getNoiseBiome(ix, jx, kx, climateSampler()));
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		ChunkPos chunkPos = chunkAccess.getPos();

		CarvingContext carvingContext = new StoneblockCarvingContext(this, region.registryAccess(), chunkAccess.getHeightAccessorForGeneration());
		CarvingMask carvingMask = ((ProtoChunk) chunkAccess).getOrCreateCarvingMask(carving);

		for (int x = -8; x <= 8; ++x) {
			for (int z = -8; z <= 8; ++z) {
				ChunkPos chunkPos1 = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
				ChunkAccess chunkAccess1 = region.getChunk(chunkPos1.x, chunkPos1.z);
				Holder<Biome> biome1 = chunkAccess1.carverBiome(() -> biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos1.getMinBlockZ()), climateSampler()));

				if (biomesWithCarvers.contains(biome1.value())) {
					var carvers = biome1.value().getGenerationSettings().getCarvers(carving);

					int i = 0;
					for (Holder<ConfiguredWorldCarver<?>> carver : carvers) {
						var configuredWorldCarver = carver.value();
						worldgenRandom.setLargeFeatureSeed(seed1 + i, chunkPos1.x, chunkPos1.z);

						if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
							Objects.requireNonNull(biomeManager1);
							configuredWorldCarver.carve(carvingContext, chunkAccess, biomeManager1::getBiome, worldgenRandom, NoAquifer.INSTANCE, chunkPos1, carvingMask);
						}
						i++;
					}
				}
			}
		}
	}

	@Override
	public void buildSurface(WorldGenRegion region, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {}

	@Override
	public void spawnOriginalMobs(WorldGenRegion region) {}

	@Override
	@NotNull
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Heightmap heightmap1 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int minY = chunkAccess.getMinBuildHeight();
		int height = chunkAccess.getHeight();
		int cx = chunkAccess.getPos().getMinBlockX();
		int cz = chunkAccess.getPos().getMinBlockZ();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int ax = cx + x;
				int az = cz + z;
				StoneBlockDataKjs config = StoneBlockDataKjs.getConfig(ax, az);

				for (int y = minY; y < height; y++) {
					BlockState state = config.getState(ax, y, az);

					chunkAccess.setBlockState(mutableBlockPos.set(x, y, z), state, false);
					heightmap1.update(x, y, z, state);
					heightmap2.update(x, y, z, state);
				}
			}
		}

		return CompletableFuture.completedFuture(chunkAccess);
	}

	@Override
	public int getGenDepth() {
		return DimensionsMain.HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return -DimensionsMain.SIZE - 1;
	}

	@Override
	public int getMinY() {
		return -DimensionsMain.SIZE;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor level) {
		return DimensionsMain.SIZE - 1;
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level) {
		BlockState[] blockStates = new BlockState[DimensionsMain.HEIGHT];
		StoneBlockDataKjs.getConfig(x, z).fillColumn(x, z, blockStates);
		return new NoiseColumn(level.getMinBuildHeight(), blockStates);
	}

	@Override
	public void addDebugScreenInfo(List<String> list, BlockPos arg) {
	}

	public static class NoAquifer implements Aquifer {
		public static final NoAquifer INSTANCE = new NoAquifer();

		@Nullable
		@Override
		public BlockState computeSubstance(DensityFunction.FunctionContext arg, double d) {
			return Blocks.CAVE_AIR.defaultBlockState();
		}

		@Override
		public boolean shouldScheduleFluidUpdate() {
			return false;
		}
	}
}
