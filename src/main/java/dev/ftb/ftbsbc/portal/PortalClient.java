package dev.ftb.ftbsbc.portal;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class PortalClient {
    public static void init() {
        ItemBlockRenderTypes.setRenderLayer(PortalRegistry.SB_PORTAL_BLOCK.get(), RenderType.translucent());
    }
}
