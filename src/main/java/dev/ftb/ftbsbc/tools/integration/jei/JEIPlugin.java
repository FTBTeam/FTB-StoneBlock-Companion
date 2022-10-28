package dev.ftb.ftbsbc.tools.integration.jei;

import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.tools.ToolsRegistry;
import dev.ftb.ftbsbc.tools.recipies.NoInventory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation FTBSBTOOLS_JEI = new ResourceLocation(FTBStoneBlock.MOD_ID, "jei");
    public static HashSet<RegistryObject<? extends Item>> HAMMERS = new LinkedHashSet<>() {{
        this.add(ToolsRegistry.STONE_HAMMER);
        this.add(ToolsRegistry.IRON_HAMMER);
        this.add(ToolsRegistry.GOLD_HAMMER);
        this.add(ToolsRegistry.DIAMOND_HAMMER);
        this.add(ToolsRegistry.NETHERITE_HAMMER);
        this.add(ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.NETHERITE_AUTO_HAMMER_BLOCK_ITEM);
    }};

    public static HashSet<RegistryObject<Item>> CROOKS = new HashSet<>() {{
        this.add(ToolsRegistry.CROOK);
    }};

    @Override
    public ResourceLocation getPluginUid() {
        return FTBSBTOOLS_JEI;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        r.addRecipeCategories(new HammerCategory(r.getJeiHelpers().getGuiHelper()));
        r.addRecipeCategories(new CrookCategory(r.getJeiHelpers().getGuiHelper()));
        r.addRecipeCategories(new CauldronCategory(r.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        Level level = Minecraft.getInstance().level;
        r.addRecipes(level.getRecipeManager().getRecipesFor(ToolsRegistry.HAMMER_RECIPE_TYPE.get(), NoInventory.INSTANCE, level), HammerCategory.ID);
        r.addRecipes(level.getRecipeManager().getRecipesFor(ToolsRegistry.CROOK_RECIPE_TYPE.get(), NoInventory.INSTANCE, level), CrookCategory.ID);

        // CauldronRecipe crap
        FluidStack out = new FluidStack(Fluids.WATER, 333);
        List<ItemStack> leaves = new ArrayList<>();
        List<ItemStack> saplings = new ArrayList<>();

        for (Block block : ForgeRegistries.BLOCKS) {
            if (block instanceof LeavesBlock) {
                Item item = block.asItem();

                if (item != Items.AIR) {
                    leaves.add(item.getDefaultInstance());
                }
            } else if (block instanceof SaplingBlock) {
                Item item = block.asItem();

                if (item != Items.AIR) {
                    saplings.add(item.getDefaultInstance());
                }
            }
        }

        r.addRecipes(Arrays.asList(new CauldronCategory.CauldronRecipe(leaves, out), new CauldronCategory.CauldronRecipe(saplings, out)), CauldronCategory.UID);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(Items.CAULDRON), CauldronCategory.UID);
        HAMMERS.forEach(hammer -> r.addRecipeCatalyst(new ItemStack(hammer.get()), HammerCategory.ID));
        CROOKS.forEach(crook -> r.addRecipeCatalyst(new ItemStack(crook.get()), CrookCategory.ID));
    }
}
