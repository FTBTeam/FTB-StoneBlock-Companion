package dev.ftb.ftbsbc.tools.content.spawner;

import dev.ftb.ftbsbc.tools.ToolsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class BitsSpawner extends SpawnerBlock {
    public BitsSpawner() {
        super(Properties.copy(Blocks.SPAWNER));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos arg, BlockState arg2) {
        return new BitsSpawnerBlockEntity(arg, arg2);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level arg, BlockState arg2, BlockEntityType<T> arg3) {
        return BitsSpawner.createTickerHelper(arg3, ToolsRegistry.BITS_BLOCK_ENTITY.get(), arg.isClientSide ? BitsSpawnerBlockEntity::clientTick : BitsSpawnerBlockEntity::serverTick);
    }
}
