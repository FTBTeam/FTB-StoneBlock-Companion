package dev.ftb.ftbsbc.tools;

import dev.ftb.ftbsbc.tools.content.spawner.BitsSpawnerRender;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ToolsClient {
    public static void init() {
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.IRON_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.BITS_SPAWNER.get(), RenderType.cutout());

        BlockEntityRenderers.register(ToolsRegistry.BITS_BLOCK_ENTITY.get(), BitsSpawnerRender::new);
    }
}
