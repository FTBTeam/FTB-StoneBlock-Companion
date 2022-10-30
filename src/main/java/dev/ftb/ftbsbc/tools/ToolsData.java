package dev.ftb.ftbsbc.tools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.portal.PortalRegistry;
import dev.ftb.ftbsbc.tools.content.autohammer.AutoHammerBlock;
import dev.ftb.ftbsbc.tools.loot.CrookModifier;
import dev.ftb.ftbsbc.tools.loot.HammerModifier;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@Mod.EventBusSubscriber(modid = FTBStoneBlock.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ToolsData {
    public static final String MODID = FTBStoneBlock.MOD_ID;

    @SubscribeEvent
    public static void dataGenEvent(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        if (event.includeClient()) {
            SMBlockModels blockModels = new SMBlockModels(gen, MODID, event.getExistingFileHelper());

            gen.addProvider(blockModels);
            gen.addProvider(new SMLang(gen, MODID, "en_us"));
            gen.addProvider(new SMItemModels(gen, MODID, event.getExistingFileHelper()));
            gen.addProvider(new SMBlockStateModels(gen, MODID, event.getExistingFileHelper(), blockModels));
        }

        if (event.includeServer()) {
            SMBlockTags blockTags = new SMBlockTags(gen, event.getExistingFileHelper());

            gen.addProvider(blockTags);
            gen.addProvider(new SMItemTags(gen, blockTags, event.getExistingFileHelper()));
            gen.addProvider(new SMRecipes(gen));
            gen.addProvider(new SMLootTableProvider(gen));
            gen.addProvider(new SMLootModifiers(gen));
        }
    }

    private static class SMLootModifiers extends GlobalLootModifierProvider {
        public SMLootModifiers(DataGenerator gen) {
            super(gen, FTBStoneBlock.MOD_ID);
        }

        @Override
        protected void start() {
            this.add("crook_loot_modifier", ToolsRegistry.CROOK_LOOT_MODIFIER.get(), new CrookModifier(new LootItemCondition[]{
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(ToolsTags.Items.CROOKS)).build()
            }));

            this.add("hammer_loot_modifier", ToolsRegistry.HAMMER_LOOT_MODIFIER.get(), new HammerModifier(new LootItemCondition[]{
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(ToolsTags.Items.HAMMERS)).build()
            }));
        }
    }

    private static class SMLang extends LanguageProvider {
        public SMLang(DataGenerator gen, String modid, String locale) {
            super(gen, modid, locale);
        }

        @Override
        protected void addTranslations() {
            this.add("itemGroup." + MODID, "StoneBlock Tools");
            this.addItem(ToolsRegistry.STONE_HAMMER, "Stone Hammer");
            this.addItem(ToolsRegistry.IRON_HAMMER, "Iron Hammer");
            this.addItem(ToolsRegistry.GOLD_HAMMER, "Gold Hammer");
            this.addItem(ToolsRegistry.DIAMOND_HAMMER, "Diamond Hammer");
            this.addItem(ToolsRegistry.NETHERITE_HAMMER, "Netherite Hammer");
            this.addItem(ToolsRegistry.CROOK, "Stone Crook");
            this.addItem(ToolsRegistry.STONE_ROD, "Stone Rod");

            this.addBlock(ToolsRegistry.IRON_AUTO_HAMMER, "Iron Auto-hammer");
            this.addBlock(ToolsRegistry.GOLD_AUTO_HAMMER, "Gold Auto-hammer");
            this.addBlock(ToolsRegistry.DIAMOND_AUTO_HAMMER, "Diamond Auto-hammer");
            this.addBlock(ToolsRegistry.NETHERITE_AUTO_HAMMER, "Netherite Auto-hammer");

            this.addItem(ToolsRegistry.FIRE_PLOW, "Fire Plow");
            this.addBlock(PortalRegistry.SB_PORTAL_BLOCK, "StoneBlock Portal");
            this.addBlock(ToolsRegistry.BITS_SPAWNER, "Bits Spawner");

            this.add("screens.ftbsbc.select_start_group", "Select a group");
            this.add("screens.ftbsbc.select_start", "Select a start");
            this.add("screens.ftbsbc.selected_start", "Selected start");
            this.add("screens.ftbsbc.by", "By: %s");
            this.add("screens.ftbsbc.back", "Back");
            this.add("screens.ftbsbc.create", "Create");
            this.add("screens.ftbsbc.select", "Select");
            this.add("screens.ftbsbc.close", "Close");

            this.add("ftbsbc.tooltip.fireplow", "Hold right click whilst looking at Stone to create lava");
            this.add("ftbsbc.tooltip.hammers", "Crushes materials down to their core components");
            this.add("ftbsbc.tooltip.auto-hammers", "Automatically crushes materials down using the hammer based on the tier of hammer");

            this.add("ftbsbc.jade.waiting", "Waiting for input: %s ticks");
            this.add("ftbsbc.jade.processing", "Processing: %s/%s");
            this.add("ftbsbc.jade.input", "Input");
            this.add("ftbsbc.jade.buffer", "Buffer");
        }
    }

    private static class SMBlockStateModels extends BlockStateProvider {
        private final SMBlockModels blockModels;

        public SMBlockStateModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper, SMBlockModels bm) {
            super(generator, modid, existingFileHelper);
            this.blockModels = bm;
        }

        @Override
        public BlockModelProvider models() {
            return this.blockModels;
        }

        @Override
        protected void registerStatesAndModels() {
            Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            int[] dirsRot = {0, 180, 270, 90};

            List<org.apache.commons.lang3.tuple.Pair<String, RegistryObject<Block>>> hammerTypes = new ArrayList<>() {{
                add(org.apache.commons.lang3.tuple.Pair.of("iron", ToolsRegistry.IRON_AUTO_HAMMER));
                add(org.apache.commons.lang3.tuple.Pair.of("gold", ToolsRegistry.GOLD_AUTO_HAMMER));
                add(org.apache.commons.lang3.tuple.Pair.of("diamond", ToolsRegistry.DIAMOND_AUTO_HAMMER));
                add(org.apache.commons.lang3.tuple.Pair.of("netherite", ToolsRegistry.NETHERITE_AUTO_HAMMER));
            }};

            for (org.apache.commons.lang3.tuple.Pair<String, RegistryObject<Block>> hammerType : hammerTypes) {
                MultiPartBlockStateBuilder b = this.getMultipartBuilder(hammerType.getRight().get());
                String path = hammerType.getRight().get().getRegistryName().getPath();
                for (int d = 0; d < 4; d++) {
                    b.part().modelFile(this.models().getExistingFile(this.modLoc("block/" + path))).rotationY(dirsRot[d]).addModel().condition(AutoHammerBlock.ACTIVE, false).condition(HORIZONTAL_FACING, dirs[d]);
                    b.part().modelFile(this.models().getExistingFile(this.modLoc("block/" + path + "_active"))).rotationY(dirsRot[d]).addModel().condition(AutoHammerBlock.ACTIVE, true).condition(HORIZONTAL_FACING, dirs[d]);
                }
            }
        }
    }

    private static class SMBlockModels extends BlockModelProvider {
        public SMBlockModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
        }
    }

    private static class SMItemModels extends ItemModelProvider {
        public SMItemModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            this.registerBlockModel(ToolsRegistry.IRON_AUTO_HAMMER.get());
            this.registerBlockModel(ToolsRegistry.GOLD_AUTO_HAMMER.get());
            this.registerBlockModel(ToolsRegistry.DIAMOND_AUTO_HAMMER.get());
            this.registerBlockModel(ToolsRegistry.NETHERITE_AUTO_HAMMER.get());

            this.simpleItem(ToolsRegistry.STONE_HAMMER);
            this.simpleItem(ToolsRegistry.IRON_HAMMER);
            this.simpleItem(ToolsRegistry.GOLD_HAMMER);
            this.simpleItem(ToolsRegistry.DIAMOND_HAMMER);
            this.simpleItem(ToolsRegistry.NETHERITE_HAMMER);
            this.simpleItem(ToolsRegistry.CROOK);
            this.simpleItem(ToolsRegistry.STONE_ROD);
            this.simpleItem(ToolsRegistry.FIRE_PLOW);
        }

        private void simpleItem(Supplier<Item> item) {
            String path = item.get().getRegistryName().getPath();
            this.singleTexture(path, this.mcLoc("item/handheld"), "layer0", this.modLoc("item/" + path));
        }

        private void registerBlockModel(Block block) {
            String path = block.getRegistryName().getPath();
            this.getBuilder(path).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + path)));
        }
    }

    private static class SMBlockTags extends BlockTagsProvider {
        public SMBlockTags(DataGenerator generatorIn, ExistingFileHelper helper) {
            super(generatorIn, FTBStoneBlock.MOD_ID, helper);
        }

        @Override
        protected void addTags() {
            Block[] blocks = Set.of(
                    ToolsRegistry.IRON_AUTO_HAMMER.get(),
                    ToolsRegistry.GOLD_AUTO_HAMMER.get(),
                    ToolsRegistry.DIAMOND_AUTO_HAMMER.get(),
                    ToolsRegistry.NETHERITE_AUTO_HAMMER.get()
            ).toArray(Block[]::new);

            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(blocks).add(ToolsRegistry.BITS_SPAWNER.get());
            this.tag(Tags.Blocks.NEEDS_WOOD_TOOL).add(blocks);
            this.tag(ToolsTags.Blocks.AUTO_HAMMERS).add(blocks);
        }
    }

    private static class SMItemTags extends ItemTagsProvider {
        public SMItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper helper) {
            super(dataGenerator, blockTagProvider, FTBStoneBlock.MOD_ID, helper);
        }

        @Override
        protected void addTags() {
            this.tag(ToolsTags.Items.HAMMERS).add(
                    ToolsRegistry.STONE_HAMMER.get(),
                    ToolsRegistry.IRON_HAMMER.get(),
                    ToolsRegistry.GOLD_HAMMER.get(),
                    ToolsRegistry.DIAMOND_HAMMER.get(),
                    ToolsRegistry.NETHERITE_HAMMER.get()
            );

            this.tag(ToolsTags.Items.CROOKS).add(
                    ToolsRegistry.CROOK.get()
            );
        }
    }

    private static class SMRecipes extends RecipeProvider {
        public SMRecipes(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
            //Hammer
            this.hammer(ToolsRegistry.STONE_HAMMER.get(), Items.COBBLESTONE, consumer);
            this.hammer(ToolsRegistry.IRON_HAMMER.get(), Items.IRON_INGOT, consumer);
            this.hammer(ToolsRegistry.GOLD_HAMMER.get(), Items.GOLD_INGOT, consumer);
            this.hammer(ToolsRegistry.DIAMOND_HAMMER.get(), Items.DIAMOND, consumer);
            this.hammer(ToolsRegistry.NETHERITE_HAMMER.get(), Items.NETHERITE_INGOT, consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.FIRE_PLOW.get())
                    .unlockedBy("has_item", has(Items.STICK))
                    .pattern("S  ")
                    .pattern(" S ")
                    .define('S', Items.STICK)
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.STONE_ROD.get(), 2)
                    .unlockedBy("has_item", has(Items.COBBLESTONE))
                    .pattern("S")
                    .pattern("S")
                    .define('S', Items.COBBLESTONE)
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.CROOK.get(), 1)
                    .unlockedBy("has_item", has(ToolsRegistry.STONE_ROD.get()))
                    .pattern("RR ")
                    .pattern(" R ")
                    .pattern(" R ")
                    .define('R', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.IRON_AUTO_HAMMER.get())
                    .unlockedBy("has_item", has(ToolsRegistry.IRON_HAMMER.get()))
                    .pattern("IGI")
                    .pattern("XHX")
                    .pattern("RGR")
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('X', Tags.Items.GLASS)
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('H', ToolsRegistry.IRON_HAMMER.get())
                    .save(consumer);

            autoHammer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.GOLD_HAMMER.get(), consumer);
            autoHammer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.DIAMOND_HAMMER.get(), consumer);
            autoHammer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.NETHERITE_HAMMER.get(), consumer);
        }

        private void autoHammer(ItemLike output, Item center, Item top, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(center))
                    .pattern("ITI")
                    .pattern("XCX")
                    .pattern("RGR")
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('X', Tags.Items.GLASS)
                    .define('T', top)
                    .define('C', center)
                    .save(consumer);
        }

        private void hammer(ItemLike output, TagKey<Item> head, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(head))
                    .pattern("hrh")
                    .pattern(" r ")
                    .pattern(" r ")
                    .define('h', head)
                    .define('r', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);
        }

        private void hammer(ItemLike output, ItemLike head, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(head))
                    .pattern("hrh")
                    .pattern(" r ")
                    .pattern(" r ")
                    .define('h', head)
                    .define('r', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);
        }
    }

    private static class SMLootTableProvider extends LootTableProvider {
        public SMLootTableProvider(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
        }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
            return Lists.newArrayList(Pair.of(SMBlockLootProvider::new, LootContextParamSets.BLOCK));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext tracker) {
            map.forEach((k, v) -> LootTables.validate(tracker, k, v));
        }
    }

    public static class SMBlockLootProvider extends BlockLoot {
        Set<Block> blocks = new HashSet<>();

        @Override
        protected void addTables() {
            dropSelf(ToolsRegistry.IRON_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.GOLD_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.DIAMOND_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.NETHERITE_AUTO_HAMMER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return blocks;
        }

        @Override
        protected void add(Block blockIn, LootTable.Builder table) {
            blocks.add(blockIn);
            super.add(blockIn, table);
        }
    }
}
