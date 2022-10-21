package dev.ftb.ftbsbc.dimensions;

import com.mojang.serialization.Codec;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.level.dungeon.DungeonStructureFeature;
import dev.ftb.ftbsbc.dimensions.level.dungeon.DungeonStructurePiece;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructure;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructurePiece;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StoneBlockBiomeSource;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StoneblockChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public interface DimensionsRegistry {
    DeferredRegister<StructureFeature<?>> STRUCTURE_FEATURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, FTBStoneBlock.MOD_ID);
    DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCE_REGISTRY = DeferredRegister.create(Registry.BIOME_SOURCE_REGISTRY, FTBStoneBlock.MOD_ID);
    DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR_REGISTRY = DeferredRegister.create(Registry.CHUNK_GENERATOR_REGISTRY, FTBStoneBlock.MOD_ID);
    DeferredRegister<StructurePieceType> STRUCTURE_PIECE_REGISTRY = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, FTBStoneBlock.MOD_ID);

    List<DeferredRegister<?>> REGISTERS = List.of(
            BIOME_SOURCE_REGISTRY,
            CHUNK_GENERATOR_REGISTRY,
            STRUCTURE_FEATURES,
            STRUCTURE_PIECE_REGISTRY
    );

    RegistryObject<Codec<StoneBlockBiomeSource>> STONE_BLOCK_BIOME_SOURCE = BIOME_SOURCE_REGISTRY.register("stoneblock_biome_source", () -> StoneBlockBiomeSource.CODEC);
    RegistryObject<Codec<StoneblockChunkGenerator>> STONE_BLOCK_CHUNK_GENERATOR = CHUNK_GENERATOR_REGISTRY.register("stoneblock_chunk_generator", () -> StoneblockChunkGenerator.CODEC);

    RegistryObject<StructureFeature<JigsawConfiguration>> START_STRUCTURE_FEATURE = STRUCTURE_FEATURES.register("start", StartStructure::new);
    RegistryObject<StructurePieceType.StructureTemplateType> START_STRUCTURE_PIECE = STRUCTURE_PIECE_REGISTRY.register("start", () -> StartStructurePiece::new);

    RegistryObject<StructureFeature<DungeonStructureFeature.CustomJigsawConfiguration>> DUNGEON_STRUCTURE_FEATURE = STRUCTURE_FEATURES.register("dungeon_structure_feature", DungeonStructureFeature::new);
    RegistryObject<StructurePieceType.StructureTemplateType> DUNGEON_STRUCTURE_PIECE = STRUCTURE_PIECE_REGISTRY.register("dungeon", () -> DungeonStructurePiece::new);
}
