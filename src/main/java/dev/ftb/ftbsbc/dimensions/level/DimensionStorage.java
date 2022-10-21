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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionStorage extends SavedData {
    private static final String SAVE_NAME = new ResourceLocation(FTBStoneBlock.MOD_ID, "dimension_store").toString();

    private final HashMap<UUID, ResourceLocation> teamToDimension = new HashMap<>();
    private final HashMap<String, ResourceLocation> archivedDimensions = new HashMap<>();
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

        CompoundTag teamDimensions = tag.getCompound("team_dimensions");
        for (String key : teamDimensions.getAllKeys()) {
            this.teamToDimension.put(UUID.fromString(key), new ResourceLocation(teamDimensions.getString(key)));
        }

        CompoundTag archivedDimensions = tag.getCompound("archived_dimensions");
        for (String key : archivedDimensions.getAllKeys()) {
            this.archivedDimensions.put(key, new ResourceLocation(archivedDimensions.getString(key)));
        }

        this.lobbySpawned = tag.getBoolean("lobby_spawned");
        if (tag.contains("lobby_spawn_pos")) {
            this.lobbySpawnPos = NbtUtils.readBlockPos(tag.getCompound("lobby_spawn_pos"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag arg) {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<UUID, ResourceLocation> teamToDim : teamToDimension.entrySet()) {
            compound.putString(teamToDim.getKey().toString(), teamToDim.getValue().toString());
        }

        CompoundTag archivedCompound = new CompoundTag();
        for (Map.Entry<String, ResourceLocation> teamToDim : archivedDimensions.entrySet()) {
            archivedCompound.putString(teamToDim.getKey(), teamToDim.getValue().toString());
        }

        arg.put("team_dimensions", compound);
        arg.put("archived_dimensions", archivedCompound);
        arg.putBoolean("lobby_spawned", lobbySpawned);
        arg.put("lobby_spawn_pos", NbtUtils.writeBlockPos(lobbySpawnPos));
        this.setDirty(false);
        return arg;
    }
}
