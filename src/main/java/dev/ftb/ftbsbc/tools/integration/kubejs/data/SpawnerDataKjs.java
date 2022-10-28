package dev.ftb.ftbsbc.tools.integration.kubejs.data;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SpawnerDataKjs {
    public static List<SpawnableEntity> entitiesToSpawn = new ArrayList<>();

    public static int minSpawnAmount = 5;
    public static int maxSpawnAmount = 20;

    public static void setMinSpawnAmount(int minSpawnAmount) {
        SpawnerDataKjs.minSpawnAmount = minSpawnAmount;
    }

    public static void setMaxSpawnAmount(int maxSpawnAmount) {
        SpawnerDataKjs.maxSpawnAmount = maxSpawnAmount;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static final class Builder {
        public List<SpawnableEntity> entities = new ArrayList<>();

        public Builder() {}

        public Builder addEntity(ResourceLocation entityType, ResourceLocation biome) {
            TagKey<Biome> biomeTagKey = TagKey.create(Registry.BIOME_REGISTRY, biome);
            entities.add(new SpawnableEntity(entityType, biomeTagKey));
            return this;
        }

        public void build() {
            SpawnerDataKjs.entitiesToSpawn.clear();
            SpawnerDataKjs.entitiesToSpawn.addAll(entities);
        }
    }

    public record SpawnableEntity(
            ResourceLocation entityId,
            @Nullable TagKey<Biome> allowedBiome
    ) {}
}
