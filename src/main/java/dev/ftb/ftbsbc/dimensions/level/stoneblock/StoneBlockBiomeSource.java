package dev.ftb.ftbsbc.dimensions.level.stoneblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;

public class StoneBlockBiomeSource extends BiomeSource {
	public static final Codec<StoneBlockBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(arg -> arg.biomeRegistry)).
			apply(instance, instance.stable(StoneBlockBiomeSource::new)));

	private final Registry<Biome> biomeRegistry;
	private final List<Holder<Biome>> biomes;

	private StoneBlockBiomeSource(Registry<Biome> r, List<Holder<Biome>> b) {
		super(b);
		biomeRegistry = r;
		biomes = b;
	}

	public StoneBlockBiomeSource(Registry<Biome> r) {
		this(r, StoneBlockDataKjs.createBiomes(r));
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return this;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int qx, int qy, int qz, Climate.Sampler sampler) {
		int x = QuartPos.fromSection(qx);
		int z = QuartPos.fromSection(qz);
		StoneBlockDataKjs config = StoneBlockDataKjs.getConfig(x, z);
		return biomes.get(config.index);
	}
}
