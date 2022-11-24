package dev.ftb.ftbsbc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Monster.class)
public class MonsterDarkEnoughMixin {
    @Inject(
            method = "Lnet/minecraft/world/entity/monster/Monster;isDarkEnoughToSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void isDarkEnoughToSpawnInBiome(ServerLevelAccessor accessor, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> callback) {
        // Only modify the logic if it's the forest biome
        if (accessor.getBiome(pos).is(BiomeTags.IS_FOREST)) {
            callback.setReturnValue(accessor.getBrightness(LightLayer.BLOCK, pos) < 9);
            return;
        }

        callback.setReturnValue(callback.getReturnValue());
    }
}
