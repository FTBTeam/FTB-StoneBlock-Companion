package dev.ftb.ftbsbc.dimensions.screen;

import com.mojang.blaze3d.platform.Window;
import dev.ftb.ftbsbc.dimensions.DimensionsClient;
import dev.ftb.ftbsbc.dimensions.level.dungeon.DungeonStructureFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class OverlayAdditions {
    @SubscribeEvent
    public static void overlay(RenderGameOverlayEvent event) {
        if (!DimensionsClient.debugMode) return;
        if (event.getType() != RenderGameOverlayEvent.ElementType.DEBUG) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = player.chunkPosition().getMinBlockX();
        int z = player.chunkPosition().getMinBlockZ();

        double v = DungeonStructureFeature.circularDistance(BlockPos.ZERO, new Vec3i(x, 0, z));
        Window window = Minecraft.getInstance().getWindow();
        Screen.drawCenteredString(event.getMatrixStack(), Minecraft.getInstance().font, "" + v, window.getGuiScaledWidth() / 2, window.getGuiScaledHeight() / 2, 0xFFFFFF);
    }
}
