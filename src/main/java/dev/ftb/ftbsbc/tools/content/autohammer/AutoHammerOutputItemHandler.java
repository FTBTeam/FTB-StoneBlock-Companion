package dev.ftb.ftbsbc.tools.content.autohammer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class AutoHammerOutputItemHandler extends ItemStackHandler {
    private final AutoHammerBlockEntity autoHammerBlockEntity;

    public AutoHammerOutputItemHandler(AutoHammerBlockEntity autoHammerBlockEntity, int size) {
        super(size);
        this.autoHammerBlockEntity = autoHammerBlockEntity;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    public ItemStack internalInsert(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack itemStack = super.insertItem(slot, stack, simulate);
        if (!simulate) {
            autoHammerBlockEntity.setChanged();
        }
        return itemStack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack itemStack = super.extractItem(slot, amount, simulate);
        if (!simulate) {
            autoHammerBlockEntity.setChanged();
        }
        return itemStack;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return 7290;
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                itemTag.putByte("Count", (byte) 1);
                itemTag.putInt("LargeCount", stacks.get(i).getCount());
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("Size", CompoundTag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack of = ItemStack.of(itemTags);
                of.setCount(itemTags.getInt("LargeCount"));
                stacks.set(slot, of);
            }
        }
        onLoad();
    }
}
