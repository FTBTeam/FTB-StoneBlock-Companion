package dev.ftb.ftbsbc.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.level.DimensionCreatedEvent;
import dev.ftb.ftbsbc.dimensions.level.DimensionStorage;
import dev.ftb.ftbsbc.dimensions.level.DynamicDimensionManager;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructure;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructurePiece;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public enum DimensionsManager {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    @Nullable
    public ResourceKey<Level> getDimension(Player player) {
        Team playerTeam = FTBTeamsAPI.getPlayerTeam((ServerPlayer) player);
        return getDimension(playerTeam);
    }

    @Nullable
    public ResourceKey<Level> getDimension(Team team) {
        if (team.getType() != TeamType.PARTY) {
            return null;
        }

        return DimensionStorage.get().getDimensionId(team);
    }

    public void createDimForTeam(ServerPlayer player, ResourceLocation prebuiltLocation) {
        // Validate the player is in the right team, they very likely aren't so let's sort that for them.
        Team playerTeam = FTBTeamsAPI.getPlayerTeam(player);
        if (playerTeam.getType() != TeamType.PARTY) {
            try {
                playerTeam = FTBTeamsAPI.getManager().createParty(player, player.getName().getString() + " Party", false).getValue();
            } catch (CommandSyntaxException e) {
                // This likely can't happen
                LOGGER.error("Unable to create player team due to " + e);
                return;
            }
        }

        // I don't know how this would happen but make sure they don't have a dim at this point...
        ResourceKey<Level> dimensionId = DimensionStorage.get().getDimensionId(playerTeam);
        if (dimensionId != null) {
            DynamicDimensionManager.teleport(player, dimensionId);
            return;
        }

        // Create the dim and store the key
        String dimensionName = generateDimensionName();
        ResourceKey<Level> key = DimensionStorage.get().putDimension(playerTeam, dimensionName);

        ServerLevel serverLevel = DynamicDimensionManager.create(player.server, key, prebuiltLocation);
        // Attempt to load the structure and get the spawn location of the island / structure
        var spawnPoint = player.server.getStructureManager().get(prebuiltLocation).map(structure -> {
            BlockPos blockPos = StartStructure.locateSpawn(structure, StartStructurePiece.makeSettings(structure))
                    .offset(-(structure.getSize().getX() / 2), 1, -(structure.getSize().getZ() / 2));

            BlockPos dimensionSpawnLocations = DimensionStorage.get().getDimensionSpawnLocations(serverLevel.getLevel().dimension().location());
            if (dimensionSpawnLocations == null) {
                DimensionStorage.get().addDimensionSpawn(serverLevel.getLevel().dimension().location(), blockPos);
                FTBStoneBlock.LOGGER.info("Adding spawn to dim storage");
            }

            return blockPos;
        }).orElse(BlockPos.ZERO);

        DynamicDimensionManager.teleport(player, key);
        player.setRespawnPosition(key, spawnPoint, 0, true, false);

        player.getInventory().clearContent();
        player.heal(player.getMaxHealth());
        FoodData foodData = player.getFoodData();
        foodData.setExhaustion(0);
        foodData.setFoodLevel(20);
        foodData.setSaturation(5.0f);

        MinecraftForge.EVENT_BUS.post(new DimensionCreatedEvent(dimensionName, playerTeam, player, player.server.getLevel(key)));
    }

    /**
     * Generates a random set of 3 words based on a dictionary. If the dictionary can't be loaded, we default to random
     * strings. We use names here as it's likely something funny might be generated and they're user friendly
     *
     * @return the dimension name seperated by hyphens
     */
    private String generateDimensionName() {
        String[] words = new String[3];

        String[] dictionary = getDictionary();
        List<String> shuffledDictionary = new ArrayList<>();
        if (dictionary != null) {
            shuffledDictionary = Arrays.stream(dictionary).collect(Collectors.toList());
            Collections.shuffle(shuffledDictionary);
        }

        for (int i = 0; i < words.length; i++) {
            if (!shuffledDictionary.isEmpty()) {
                words[i] = shuffledDictionary.get(RANDOM.nextInt(shuffledDictionary.size() - 1));
            } else {
                words[i] = RandomStringUtils.randomAlphabetic(3, 8).toLowerCase();
            }
        }

        return String.join("-", words);
    }

    /**
     * Attempts to the load the dictionary from the resourses folder but is careful not to crash the game in doing so
     * as we fall back in the method above.
     *
     * @return the dictionary
     */
    @Nullable
    private String[] getDictionary() {
        try (var dictionarySteam = DimensionsManager.class.getResourceAsStream("/assets/%s/dictionary.txt".formatted(FTBStoneBlock.MOD_ID))) {
            if (dictionarySteam == null) {
                return null;
            }

            return new String(dictionarySteam.readAllBytes(), StandardCharsets.UTF_8).split("\n");
        } catch (IOException e) {
            LOGGER.error("Unable to read dictionary file, defaulting to random strings", e);
        }

        return null;
    }
}
