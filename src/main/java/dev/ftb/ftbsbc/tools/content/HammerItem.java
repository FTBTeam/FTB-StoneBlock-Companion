package dev.ftb.ftbsbc.tools.content;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;


public class HammerItem extends DiggerItem {

    public HammerItem(Tiers tier, float attackBase, float attackSpeed, Properties arg2) {
        super(attackBase, attackSpeed, tier, BlockTags.MINEABLE_WITH_PICKAXE, arg2);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            int i = this.getTier().getLevel();
            if (i < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
                return false;
            }

            if (i < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
                return false;
            }

            return i >= 1 || !state.is(BlockTags.NEEDS_STONE_TOOL);
        }

        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
    }
}
