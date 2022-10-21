package dev.ftb.ftbsbc.dimensions.kubejs;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class StoneBlockLayerKjs {
    public final String block;
    public final int size;
    private BlockState state;

    public StoneBlockLayerKjs(String layer) {
        String[] s = layer.split("x ", 2);

        if (s.length == 2) {
            block = s[1];
            size = Integer.parseInt(s[0]);
        } else {
            block = s[0];
            size = 1;
        }
    }

    public BlockState getState() {
        if (state == null) {
            try {
                state = new BlockStateArgument().parse(new StringReader(block)).getState();
            } catch (Exception ex) {
                ex.printStackTrace();
                state = Blocks.RED_WOOL.defaultBlockState();
            }
        }

        return state;
    }
}
