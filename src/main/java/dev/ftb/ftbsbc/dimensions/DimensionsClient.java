package dev.ftb.ftbsbc.dimensions;

import com.mojang.blaze3d.platform.NativeImage;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.net.CreateDimensionForTeam;
import dev.ftb.ftbsbc.dimensions.screen.StartSelectScreen;
import dev.latvian.mods.kubejs.KubeJS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class DimensionsClient {
    public static void init() {
        DimensionSpecialEffects.EFFECTS.put(new ResourceLocation(FTBStoneBlock.MOD_ID, "stoneblock"), new StoneblockDimensionSpecialEffects());
    }

    public static void exportBiomes(ServerLevel level, Path path, int radius) {
        KubeJS.startupScriptManager.unload();
        KubeJS.startupScriptManager.loadFromDirectory();
        KubeJS.startupScriptManager.load();
        Player player = Minecraft.getInstance().player;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        try (NativeImage image = new NativeImage(radius * 2 + 1, radius * 2 + 1, true)) {
            for (int x = 0; x <= radius * 2; x++) {
                for (int z = 0; z <= radius * 2; z++) {
                    pos.set(x - radius + Mth.floor(player.getX()), 0, z - radius + Mth.floor(player.getZ()));
                    image.setPixelRGBA(x, z, StoneBlockDataKjs.getColor(level, pos));
                }
            }

            if (Files.exists(path)) {
                Files.delete(path);
            }

            image.writeToFile(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void openSelectionScreen() {
        Minecraft.getInstance().setScreen(new StartSelectScreen(prebuild -> {
            new CreateDimensionForTeam(prebuild.id).sendToServer();
        }));
    }

    private static class StoneblockDimensionSpecialEffects extends DimensionSpecialEffects {
        public StoneblockDimensionSpecialEffects() {
            super(Float.NaN, false, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 arg, float f) {
            return arg.scale(0.15D);
        }

        @Override
        public boolean isFoggyAt(int i, int j) {
            return false;
        }

        @Override
        @Nullable
        public float[] getSunriseColor(float f, float g) {
            return null;
        }
    }
}
