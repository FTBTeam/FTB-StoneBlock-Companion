package dev.ftb.ftbsbc.mixin.imsorrywellremoveitonceitsfixed;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

import java.util.List;

/**
 * TODO: remove this as soon as possible
 * again, sorry, plz merge the PR
 */
@Mixin(targets = "team.chisel.ctm.client.util.CTMPackReloadListener$CachingLayerCheck")
public class CTMCMEFixMixin {
    @Shadow @Mutable private Object2BooleanMap<RenderType> cache;

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;Z)V", at = @At("RETURN"), remap = false)
    public void useDifferentCache(BlockState state, List<AbstractCTMBakedModel> models, boolean useFallback, CallbackInfo info) {
        this.cache = Object2BooleanMaps.synchronize(this.cache);
    }
}
