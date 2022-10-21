package dev.ftb.ftbsbc.tools.integration.jei;


import dev.ftb.ftbsbc.FTBStoneBlock;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * @author LatvianModder
 */
public class CauldronCategory implements IRecipeCategory<CauldronCategory.CauldronRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(FTBStoneBlock.MOD_ID + ":cauldron");

	private final Component title;
	private final IDrawable background;
	private final IDrawable icon;

	public CauldronCategory(IGuiHelper guiHelper) {
		title = new TranslatableComponent("block.minecraft.cauldron");
		background = guiHelper.drawableBuilder(new ResourceLocation(FTBStoneBlock.MOD_ID + ":textures/cauldron.png"), 0, 0, 112, 30).setTextureSize(128, 64).build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Items.CAULDRON));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends CauldronRecipe> getRecipeClass() {
		return CauldronRecipe.class;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CauldronRecipe recipe, List<? extends IFocus<?>> focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 3, 7)
				.addIngredients(VanillaTypes.ITEM, recipe.input())
		;

		builder.addSlot(RecipeIngredientRole.INPUT, 48, 7)
				.addIngredient(VanillaTypes.ITEM, new ItemStack(Items.CAULDRON))
		;

		builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 7)
				.addIngredient(VanillaTypes.FLUID, recipe.output())
		;
	}

	public record CauldronRecipe(List<ItemStack> input, FluidStack output) {}
}
