package dev.ftb.ftbsbc.tools.integration.jade;

import dev.ftb.ftbsbc.tools.content.autohammer.AutoHammerBlock;
import dev.ftb.ftbsbc.tools.content.autohammer.AutoHammerBlockEntity;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin
public class SBCJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AutoHammerComponentProvider.INSTANCE, AutoHammerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(AutoHammerComponentProvider.INSTANCE, TooltipPosition.BODY, AutoHammerBlock.class);
    }

    enum AutoHammerComponentProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            CompoundTag serverData = blockAccessor.getServerData();
            if (!serverData.contains("progress")) {
                return;
            }

            var helper = iTooltip.getElementHelper();

            int timeout = serverData.getInt("timeout");
            if (timeout == 0) {
                iTooltip.add(new TranslatableComponent("ftbsbc.jade.processing", serverData.getInt("progress"), serverData.getInt("maxProgress")));
            } else {
                iTooltip.append(new TranslatableComponent("ftbsbc.jade.waiting", timeout));
            }

            var inputStack = LightItem.deserialize(serverData.getCompound("input")).toStack();
            var outputStack = LightItem.deserialize(serverData.getCompound("output")).toStack();

            ITooltip tooltip = helper.tooltip();
            if (!inputStack.isEmpty()) {
                tooltip.append(helper.item(inputStack));
                tooltip.append(helper.spacer(5, 0));
                var nameTip = helper.tooltip();
                nameTip.append(new TranslatableComponent("ftbsbc.jade.input"));
                nameTip.add(inputStack.getHoverName().copy().italic());
                tooltip.append(helper.box(nameTip, helper.borderStyle().width(0)));
            }

            if (!outputStack.isEmpty()) {
                if (!inputStack.isEmpty()) {
                    tooltip.append(helper.spacer(10, 0));
                }
                tooltip.append(helper.item(outputStack));
                tooltip.append(helper.spacer(5, 0));
                var nameTip = helper.tooltip();
                nameTip.append(new TranslatableComponent("ftbsbc.jade.buffer"));
                nameTip.add(outputStack.getHoverName().copy().italic());
                tooltip.append(helper.box(nameTip, helper.borderStyle().width(0)));
            }

            iTooltip.add(helper.box(tooltip, helper.borderStyle().color(0x0FFFFFFF)));
        }

        @Override
        public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
            if (!(blockEntity instanceof AutoHammerBlockEntity autoHammerEntity)) {
                return;
            }

            compoundTag.putInt("progress", autoHammerEntity.getProgress());
            compoundTag.putInt("maxProgress", autoHammerEntity.getMaxProgress());
            compoundTag.putInt("timeout", autoHammerEntity.getTimeOut());

            Direction direction = autoHammerEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            ItemStack inputStack = autoHammerEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, AutoHammerBlockEntity.getInputDirection(direction))
                    .map(h -> h.getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);

            compoundTag.put("input", LightItem.create(inputStack).serialize());

            LazyOptional<IItemHandler> capability = autoHammerEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, AutoHammerBlockEntity.getOutputDirection(direction));
            ItemStack itemStack = capability
                    .map(h -> h.getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);

            // Count the items in the slot, lazyOptional sucks, map them to a sum of all stacks
            int count = capability.map(e -> {
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < e.getSlots(); i++) {
                    stacks.add(e.getStackInSlot(i).copy());
                }

                return stacks;
            }).orElse(new ArrayList<>())
                    .stream().mapToInt(ItemStack::getCount).sum();

            compoundTag.put("output", new LightItem(itemStack.getItem().getRegistryName(), count).serialize());
        }
    }

    record LightItem(
            ResourceLocation location,
            int count
    ) {
        public static LightItem create(ItemStack stack) {
            return new LightItem(stack.getItem().getRegistryName(), stack.getCount());
        }

        public static LightItem deserialize(CompoundTag compound) {
            if (!compound.contains("name")) {
                return new LightItem(new ResourceLocation("minecraft:air"), 0);
            }

            return new LightItem(new ResourceLocation(compound.getString("name")), compound.contains("count") ? compound.getInt("count") : 1);
        }

        public CompoundTag serialize() {
            var tag = new CompoundTag();
            tag.putInt("count", this.count);
            tag.putString("name", this.location.toString());
            return tag;
        }

        public ItemStack toStack() {
            Item value = ForgeRegistries.ITEMS.getValue(this.location);
            if (value == null) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(value, this.count);
        }
    }
}
