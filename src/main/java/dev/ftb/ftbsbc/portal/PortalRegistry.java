package dev.ftb.ftbsbc.portal;

import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.portal.content.StoneBlockPortalBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public interface PortalRegistry {
    DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, FTBStoneBlock.MOD_ID);
    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, FTBStoneBlock.MOD_ID);

    List<DeferredRegister<?>> REGISTERS = List.of(
            BLOCK_REGISTRY, ITEM_REGISTRY
    );

    RegistryObject<Block> SB_PORTAL_BLOCK = BLOCK_REGISTRY.register("stone_block_portal", StoneBlockPortalBlock::new);
}
