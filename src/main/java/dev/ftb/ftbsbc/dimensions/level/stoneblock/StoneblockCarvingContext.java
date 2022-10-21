package dev.ftb.ftbsbc.dimensions.level.stoneblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.carver.CarvingContext;

import java.util.Optional;
import java.util.function.Function;

public class StoneblockCarvingContext extends CarvingContext {
	public StoneblockCarvingContext(NoiseBasedChunkGenerator arg, RegistryAccess arg2, LevelHeightAccessor arg3) {
		super(arg, arg2, arg3, null);
	}

	@Override
	@Deprecated
	public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> function, ChunkAccess arg, BlockPos arg2, boolean bl) {
		return Optional.of(Blocks.BEDROCK.defaultBlockState());
	}
}
