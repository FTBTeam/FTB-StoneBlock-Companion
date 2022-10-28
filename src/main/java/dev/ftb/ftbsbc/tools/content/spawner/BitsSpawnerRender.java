package dev.ftb.ftbsbc.tools.content.spawner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class BitsSpawnerRender implements BlockEntityRenderer<BitsSpawnerBlockEntity> {
    public BitsSpawnerRender(BlockEntityRendererProvider.Context var1) {
    }

    @Override
    public void render(BitsSpawnerBlockEntity blockEntity, float f, PoseStack arg2, MultiBufferSource arg3, int i, int j) {
        arg2.pushPose();
        arg2.translate(0.5, 0.0, 0.5);
        Entity entity = blockEntity.getOrCreateDisplayEntity();
        if (entity != null) {
            float g = 0.53125f;
            float h = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if ((double)h > 1.0) {
                g /= h;
            }
            arg2.translate(0.0, 0.4f, 0.0);
            arg2.mulPose(Vector3f.YP.rotationDegrees((float) Mth.lerp((double)f, blockEntity.oSpin, blockEntity.spin) * 10.0f));
            arg2.translate(0.0, -0.2f, 0.0);
            arg2.mulPose(Vector3f.XP.rotationDegrees(-30.0f));
            arg2.scale(g, g, g);
            Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0f, f, arg2, arg3, i);
        }
        arg2.popPose();
    }
}
