package dev.ftb.ftbsbc.dimensions.kubejs;

import dev.ftb.ftbsbc.dimensions.DimensionsMain;
import dev.ftb.ftbsbc.dimensions.prebuilt.PrebuiltStructure;
import dev.latvian.mods.rhino.mod.util.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class StoneBlockDataKjs {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Random RANDOM = new Random();

	public static boolean debugMode = false;
	public static final List<StoneBlockDataKjs> BIOMES = new ArrayList<>();
	public static final Map<String, PrebuiltStructure> PREBUILT_STRUCTURES = new LinkedHashMap<>();
	public static final PositionalRandomFactory RANDOM_FACTORY = new LegacyRandomSource.LegacyPositionalRandomFactory(0L);
	private static int totalDistance = 1;

	public static ResourceLocation lobbyStructure = null;

	public static void reset() {
		PREBUILT_STRUCTURES.clear();
		BIOMES.clear();
		totalDistance = 0;
		lobbyStructure = null;
	}

	public static void setLobbyStructure(ResourceLocation location) {
		lobbyStructure = location;
	}

	public static PrebuiltStructure addStart(ResourceLocation id, Component name, String author) {
		PrebuiltStructure start = new PrebuiltStructure(id, name, author);
		PREBUILT_STRUCTURES.put(id.toString(), start);
		return start;
	}

	public static PrebuiltStructure addStart(ResourceLocation id, Component name) {
		return addStart(id, name, "FTB Team");
	}

	public static StoneBlockDataKjs addBiome(ResourceLocation biome, int size) {
		StoneBlockDataKjs config = new StoneBlockDataKjs(ResourceKey.create(Registry.BIOME_REGISTRY, biome), totalDistance);
		BIOMES.add(config);
		BIOMES.sort(Comparator.comparingInt(StoneBlockDataKjs::getOrder));

		for (int i = 0; i < BIOMES.size(); i++) {
			BIOMES.get(i).index = i;
		}

		totalDistance += size;
		return config;
	}

	public static StoneBlockDataKjs getConfig(int distance) {
		int dist = distance % totalDistance;

		for (StoneBlockDataKjs config : BIOMES) {
			if (dist >= config.startsAt) {
				return config;
			}
		}

		return BIOMES.get(0);
	}

	public static StoneBlockDataKjs getConfig(int x, int z) {
		int distance = (int) Math.sqrt(x * x + z * z);
		StoneBlockDataKjs data = getConfig(distance);

		if (data.blend > 0) {
			int d = RANDOM_FACTORY.at(x, 0, z).nextInt(data.blend);

			if (d > 0) {
				return getConfig(distance + d);
			}
		}

		return data;
	}

	public static int getColor(ServerLevel level, BlockPos pos) {
		if (pos.getX() == 0 && pos.getZ() == 0) {
			return 0xFF00FF00;
		}

		return getConfig(pos.getX(), pos.getZ()).biomeColor;
	}

	public static List<Holder<Biome>> createBiomes(Registry<Biome> r) {
		List<Holder<Biome>> list = new ArrayList<>();

		for (StoneBlockDataKjs config : BIOMES) {
			list.add(r.getHolderOrThrow(config.biome));
		}

		return list;
	}

	public static void finish() {
		HashSet<String> uniqueIds = new HashSet<>();

		for (StoneBlockDataKjs config : BIOMES) {
			config.uniqueId = config.biome.location().getPath();
			int c = 2;

			while (uniqueIds.contains(config.uniqueId)) {
				config.uniqueId = config.biome.location().getPath() + "_" + c;
				c++;
			}

			uniqueIds.add(config.uniqueId);
			config.finishConfig();
		}
	}

	public static int getDefaultMaxDistance() {
		return Math.min(Mth.ceil(totalDistance * 1.1D), 10000);
	}

	public int index;
	public String uniqueId;
	public final ResourceKey<Biome> biome;
	private final int startsAt;
	private final List<StoneBlockLayerKjs> layerList;
	private StoneBlockLayerKjs[] layers;
	public int biomeColor;
	public int blend;
	public boolean carvers;

	public StoneBlockDataKjs(ResourceKey<Biome> biome, int startsAt) {
		this.biome = biome;
		this.startsAt = startsAt;
		this.layerList = new ArrayList<>();
		this.biomeColor = RANDOM.nextInt() | 0xFF000000;
		this.blend = 40;
		this.carvers = false;
	}

	@Override
	public String toString() {
		return uniqueId;
	}

	public void setColor(Color s) {
		int rgb = s.getRgbJS();
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		biomeColor = 0xFF000000 | b << 16 | g << 8 | r;
	}

	public void addLayer(String layer) {
		layerList.add(new StoneBlockLayerKjs(layer));
	}

	public void finishConfig() {
		if (layerList.isEmpty()) {
			layerList.add(new StoneBlockLayerKjs("minecraft:stone"));
			LOGGER.error("No layers added in " + biome.location());
		}

		layers = new StoneBlockLayerKjs[DimensionsMain.SIZE];
		StoneBlockLayerKjs lastLayer = layerList.get(layerList.size() - 1);
		Arrays.fill(layers, lastLayer);

		int index = 0;

		breakAll:
		for (StoneBlockLayerKjs layer : layerList) {
			if (layer == lastLayer) {
				break;
			}

			for (int y = 0; y < layer.size; y++) {
				layers[index] = layer;
				index++;

				if (index == layers.length) {
					break breakAll;
				}
			}
		}

		if (debugMode) {
			LOGGER.info("=== Debug layer output of " + biome.location() + " ===");

			for (int i = 0; i < layers.length; i++) {
				LOGGER.info(String.format("[%03d] %s", i, layers[i].block));
			}
		}
	}

	public StoneBlockLayerKjs getLayer(int y) {
		return layers[Mth.clamp(Math.abs(y), 0, layers.length - 1)];
	}

	public BlockState getState(int x, int y, int z) {
		if (!debugMode && (y == -DimensionsMain.SIZE || y == DimensionsMain.SIZE - 1)) {
			return Blocks.BEDROCK.defaultBlockState();
		} else if (debugMode && ((x >> 6) & 1) == (((z >> 6) & 1))) {
			return Blocks.AIR.defaultBlockState();
		}

		int ay = Math.abs(y);
		RandomSource random = RANDOM_FACTORY.at(x, y, z);
		return getLayer(ay + random.nextInt(7) - 3).getState();
	}

	public void fillColumn(int x, int z, BlockState[] states) {
		for (int i = 0; i < states.length; i++) {
			states[i] = getState(x, i - DimensionsMain.HEIGHT / 2, z);
		}
	}

	private int getOrder() {
		return -startsAt;
	}
}
