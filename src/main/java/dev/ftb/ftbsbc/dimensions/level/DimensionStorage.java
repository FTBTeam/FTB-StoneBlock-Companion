package dev.ftb.ftbsbc.dimensions.level;

import com.google.common.collect.ImmutableMap;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DimensionStorage extends SavedData {
    private static final String SAVE_NAME = new ResourceLocation(FTBStoneBlock.MOD_ID, "dimension_store").toString();

    private final HashMap<UUID, ResourceLocation> teamToDimension = new HashMap<>();
    private final HashMap<String, ResourceLocation> archivedDimensions = new HashMap<>();
    private final HashMap<ResourceLocation, BlockPos> dimensionSpawnLocations = new HashMap<>();

    private boolean lobbySpawned = false;
    private BlockPos lobbySpawnPos = BlockPos.ZERO;

    public static DimensionStorage get() {
        return ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(DimensionStorage::load, DimensionStorage::new, SAVE_NAME);
    }

    @Nullable
    public ResourceKey<Level> getDimensionId(Team team) {
        ResourceLocation dimLocation = teamToDimension.get(team.getId());
        if (dimLocation == null) {
            return null;
        }

        return ResourceKey.create(Registry.DIMENSION_REGISTRY, dimLocation);
    }

    public ResourceKey<Level> putDimension(Team playerTeam, String generateDimensionName) {
        ResourceLocation value = new ResourceLocation(FTBStoneBlock.MOD_ID, "team/%s".formatted(generateDimensionName));
        teamToDimension.put(playerTeam.getId(), value);
        this.setDirty();

        return getDimensionId(playerTeam);
    }

    public void archiveDimension(Team oldTeam) {
        ResourceKey<Level> dimensionId = getDimensionId(oldTeam);
        if (dimensionId == null) {
            return;
        }

        teamToDimension.remove(oldTeam.getId());
        archivedDimensions.put(oldTeam.getId().toString() + "-" + oldTeam.getOwner(), dimensionId.location());
    }

    public HashMap<String, ResourceLocation> getArchivedDimensions() {
        return archivedDimensions;
    }

    public ImmutableMap<UUID, ResourceLocation> getTeamToDimension() {
        return ImmutableMap.copyOf(teamToDimension);
    }

    @Nullable
    public BlockPos getDimensionSpawnLocations(ResourceLocation dimKeyLocation) {
        return dimensionSpawnLocations.get(dimKeyLocation);
    }

    public void addDimensionSpawn(ResourceLocation location, BlockPos pos) {
        dimensionSpawnLocations.put(location, pos);
        this.setDirty();
    }

    public boolean isLobbySpawned() {
        return lobbySpawned;
    }

    public void setLobbySpawned(boolean lobbySpawned) {
        this.lobbySpawned = lobbySpawned;
        this.setDirty();
    }

    public BlockPos getLobbySpawnPos() {
        return lobbySpawnPos;
    }

    public void setLobbySpawnPos(BlockPos lobbySpawnPos) {
        this.lobbySpawnPos = lobbySpawnPos;
        this.setDirty();
    }

    private static DimensionStorage load(CompoundTag compoundTag) {
        var storage = new DimensionStorage();
        storage.read(compoundTag);
        return storage;
    }

    private void read(CompoundTag tag) {
        if (!tag.contains("team_dimensions")) {
            return;
        }

        this.teamToDimension.putAll(hashMapReader(UUID::fromString, (tag1, key) -> new ResourceLocation(tag1.getString(key)), tag.getCompound("team_dimensions")));
        this.archivedDimensions.putAll(hashMapReader(Function.identity(), (tag1, key) -> new ResourceLocation(tag1.getString(key)), tag.getCompound("archived_dimensions")));
        this.dimensionSpawnLocations.putAll(hashMapReader(ResourceLocation::new, (tag1, key) -> BlockPos.of(tag1.getLong(key)), tag.getCompound("dimension_spawns")));

        this.lobbySpawned = tag.getBoolean("lobby_spawned");
        if (tag.contains("lobby_spawn_pos")) {
            this.lobbySpawnPos = NbtUtils.readBlockPos(tag.getCompound("lobby_spawn_pos"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag arg) {
        arg.put("team_dimensions", hashMapWriter(teamToDimension, (tag, key, value) -> tag.putString(key.toString(), value.toString())));
        arg.put("archived_dimensions", hashMapWriter(archivedDimensions, (tag, key, value) -> tag.putString(key, value.toString())));
        arg.put("dimension_spawns", hashMapWriter(dimensionSpawnLocations, (tag, key, value) -> tag.putLong(key.toString(), value.asLong())));

        arg.putBoolean("lobby_spawned", lobbySpawned);
        arg.put("lobby_spawn_pos", NbtUtils.writeBlockPos(lobbySpawnPos));
        this.setDirty(false);
        return arg;
    }

    private <K, V> HashMap<K, V> hashMapReader(Function<String, K> keyReader, BiFunction<CompoundTag, String, V> valueReader, CompoundTag tag) {
        HashMap<K, V> hashMap = new HashMap<>();

        for (String key : tag.getAllKeys()) {
            hashMap.put(keyReader.apply(key), valueReader.apply(tag, key));
        }

        return hashMap;
    }

    private <K, V> CompoundTag hashMapWriter(HashMap<K, V> map, TriConsumer<CompoundTag, K, V> writer) {
        var tag = new CompoundTag();
        for (Map.Entry<K, V> kvEntry : map.entrySet()) {
            writer.accept(tag, kvEntry.getKey(), kvEntry.getValue());
        }

        return tag;
    }

}
