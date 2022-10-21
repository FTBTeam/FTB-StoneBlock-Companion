package dev.ftb.ftbsbc.tools.content;

import dev.ftb.ftbsbc.tools.ToolsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class FirePlowItem extends Item {
	public FirePlowItem() {
		super(new Properties().tab(ToolsRegistry.CREATIVE_GROUP).durability(4));
	}

	@Override
	public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
		list.add(new TranslatableComponent("ftbsbc.tooltip.fireplow").gray());
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 60;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int tick) {
		if (tick % 3 == 0 && level.isClientSide()) {
			HitResult hitResult = entity.pick(ForgeMod.REACH_DISTANCE.get().getDefaultValue(), 1.0f, true);

			if (hitResult instanceof BlockHitResult hit) {
				BlockPos pos = hit.getBlockPos();
				BlockState state = level.getBlockState(pos);

				if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
					Random random = level.random;
					level.addParticle(ParticleTypes.LAVA, pos.getX() + random.nextFloat(), pos.getY() + 1D, pos.getZ() + random.nextFloat(), 0D, 0D, 0D);
				}
			}
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		HitResult hitResult = entity.pick(ForgeMod.REACH_DISTANCE.get().getDefaultValue(), 1.0f, true);
		if (hitResult instanceof BlockHitResult hit) {
			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);

			Random random = level.random;
			if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
				level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);

				if (level.isClientSide()) {
					level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);

					for (int i = 0; i < 20; i++) {
						level.addParticle(ParticleTypes.LAVA, pos.getX() + random.nextFloat(), pos.getY() + 1D, pos.getZ() + random.nextFloat(), 0D, 0D, 0D);
					}
				} else {
					stack.hurtAndBreak(1, entity, (arg2x) -> arg2x.broadcastBreakEvent(arg2x.getUsedItemHand()));
				}
			}
		}

		return stack;
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
