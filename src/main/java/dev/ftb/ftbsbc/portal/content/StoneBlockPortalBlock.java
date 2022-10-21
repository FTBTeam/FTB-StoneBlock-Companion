package dev.ftb.ftbsbc.portal.content;

import dev.ftb.ftbsbc.dimensions.DimensionsManager;
import dev.ftb.ftbsbc.dimensions.level.DynamicDimensionManager;
import dev.ftb.ftbsbc.dimensions.net.ShowSelectionGui;
import dev.ftb.ftbsbc.portal.ForgeOnlyOverride;
import dev.ftb.ftbsbc.portal.StoneBlockServerPlayer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * A lot of this class extends from the nether portal block
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StoneBlockPortalBlock extends NetherPortalBlock {
    public StoneBlockPortalBlock() {
        super(Properties.copy(Blocks.NETHER_PORTAL));
    }

    @Override
    public void randomTick(BlockState arg, ServerLevel arg2, BlockPos arg3, Random random) {

    }

    @Override
    @ForgeOnlyOverride
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        StoneBlockServerPlayer sbPlayer = (StoneBlockServerPlayer) player;
        sbPlayer.handleStoneBlockPortal(() -> {
            // Push the player out of the portal
            BlockPos relative = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X, (player.getDirection() == Direction.SOUTH || player.getDirection() == Direction.WEST ? -2 : 2));
            player.teleportTo(relative.getX(), player.getY(), relative.getZ());

            // Instantly jump
            ResourceKey<Level> dimension = DimensionsManager.INSTANCE.getDimension(player);
            if (dimension != null) {
                DynamicDimensionManager.teleport(player, dimension);
            } else {
                new ShowSelectionGui().sendTo(player);
            }
        });
    }

    @Override
    public BlockState updateShape(BlockState arg, Direction arg2, BlockState arg3, LevelAccessor arg4, BlockPos arg5, BlockPos arg6) {
        return arg;
    }
}
