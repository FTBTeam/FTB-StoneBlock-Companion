package dev.ftb.ftbsbc.tools.integration.kubejs;

import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import net.minecraft.resources.ResourceLocation;

public class KubeJSIntegration extends KubeJSPlugin {
    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(new ResourceLocation(FTBStoneBlock.MOD_ID, "hammer"), HammerRecipeJS::new);
        event.register(new ResourceLocation(FTBStoneBlock.MOD_ID, "crook"), CrookRecipeJS::new);
    }
}
