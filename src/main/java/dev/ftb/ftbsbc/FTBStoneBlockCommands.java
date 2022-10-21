package dev.ftb.ftbsbc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.ftbsbc.dimensions.DimensionsClient;
import dev.ftb.ftbsbc.dimensions.DimensionsManager;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.level.DimensionStorage;
import dev.ftb.ftbsbc.dimensions.level.DynamicDimensionManager;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructure;
import dev.ftb.ftbsbc.dimensions.level.stoneblock.StartStructurePiece;
import dev.ftb.ftbsbc.dimensions.prebuilt.PrebuiltCommandArgument;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FTBStoneBlockCommands {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final LevelResource EXPORT_PATH = new LevelResource("stoneblock-export.png");

    public static final DynamicCommandExceptionType NOT_PARTY_TEAM = new DynamicCommandExceptionType((object) -> new TextComponent("[%s] is not a party team...".formatted(object)));
    public static final DynamicCommandExceptionType NO_DIM = new DynamicCommandExceptionType((object) -> new TextComponent("No dimension found for %s".formatted(object)));

    public static void setup() {
        ArgumentTypes.register(FTBStoneBlock.MOD_ID + ":prebuilt", PrebuiltCommandArgument.class, new EmptyArgumentSerializer<>(PrebuiltCommandArgument::create));
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LOGGER.info("Registering StoneBlock Commands");

        LiteralCommandNode<CommandSourceStack> commands = commandDispatcher.register(Commands.literal("sbc")
                .then(Commands.literal("reload-and-export-biomes")
                        .requires(source -> source.hasPermission(2) && source.getServer().isSingleplayer())
                        .executes(context -> exportBiomes(context.getSource(), StoneBlockDataKjs.getDefaultMaxDistance()))
                        .then(Commands.argument("max-distance", IntegerArgumentType.integer(1, 20000))
                                .executes(context -> exportBiomes(context.getSource(), IntegerArgumentType.getInteger(context, "max-distance")))
                        )
                )
                .then(Commands.literal("visit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("team", TeamArgument.create()).executes(context -> visitDim(context.getSource(), TeamArgument.get(context, "team"))))
                )
                .then(Commands.literal("list-archived")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> listArchived(context.getSource()))
                )
                .then(Commands.literal("lobby").executes(context -> lobby(context.getSource())))
                .then(Commands.literal("home").executes(context -> home(context.getSource())))
                .then(Commands.literal("ffs").executes(context -> spawnLobby(context.getSource())))
        );

        commandDispatcher.register(Commands.literal("ftbstoneblock").redirect(commands));
    }

    private static int spawnLobby(CommandSourceStack source) {
        var level = source.getLevel();

        StructureTemplate lobby = level.getStructureManager().getOrCreate(StoneBlockDataKjs.lobbyStructure);
        StructurePlaceSettings placeSettings = StartStructurePiece.makeSettings(lobby);
        BlockPos spawnPos = StartStructure.locateSpawn(lobby, placeSettings);
        BlockPos lobbyLoc = BlockPos.ZERO.offset(-(lobby.getSize().getX() / 2), 0, -(lobby.getSize().getZ() / 2));
        BlockPos playerSpawn = spawnPos.offset(lobbyLoc.getX(), lobbyLoc.getY(), lobbyLoc.getZ());

        lobby.placeInWorld(level, lobbyLoc, lobbyLoc, placeSettings, level.random, Block.UPDATE_ALL);

        DimensionStorage.get().setLobbySpawned(true);
        DimensionStorage.get().setLobbySpawnPos(playerSpawn);

        level.removeBlock(playerSpawn, false);
        level.setDefaultSpawnPos(playerSpawn, 0);

        System.out.println(spawnPos);
        System.out.println(playerSpawn);

        level.players().forEach(e -> {
            e.teleportTo(level, playerSpawn.getX(), playerSpawn.getY(), playerSpawn.getZ(), e.getYRot(), e.getXRot());
            e.setRespawnPosition(level.dimension(), spawnPos, -180, true, false);
        });

        return 0;
    }

    private static int listArchived(CommandSourceStack source) {
        HashMap<String, ResourceLocation> archivedDimensions = DimensionStorage.get().getArchivedDimensions();
        if (archivedDimensions.isEmpty()) {
            source.sendFailure(new TextComponent("No archived dimensions available"));
            return -1;
        }

        for (Map.Entry<String, ResourceLocation> dim : archivedDimensions.entrySet()) {
            source.sendSuccess(new TextComponent("[old-team-id=%s] [owner=%s] [dim-name=%s]".formatted(dim.getKey().split("-")[0], dim.getKey().split("-")[1], dim.getValue().toString())), false);
        }

        return 0;
    }

    private static int visitDim(CommandSourceStack source, Team team) throws CommandSyntaxException {
        if (team.getType() != TeamType.PARTY) {
            throw NOT_PARTY_TEAM.create(team.getName().getString());
        }

        ResourceKey<Level> dimension = DimensionsManager.INSTANCE.getDimension(team);
        if (dimension == null) {
            throw NO_DIM.create(team.getName().getString());
        }

        DynamicDimensionManager.teleport(source.getPlayerOrException(), dimension);
        return 0;
    }

    private static int lobby(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (DynamicDimensionManager.teleport(player, Level.OVERWORLD)) {
            return 1;
        }

        return 0;
    }

    private static int home(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Team team = FTBTeamsAPI.getPlayerTeam(player);

        if (team.getType().isParty() && DynamicDimensionManager.teleport(player, DimensionsManager.INSTANCE.getDimension(player))) {
            return 1;
        }

        source.sendFailure(new TextComponent("Go to the lobby and jump through the portal!"));
        return 0;
    }

    private static int exportBiomes(CommandSourceStack source, int radius) {
        if (radius <= 0) {
            source.sendFailure(new TextComponent("Empty image!"));
            return 0;
        }

        source.sendSuccess(new TextComponent("Exporting " + (radius * 2 + 1) + "x" + (radius * 2 + 1) + " image..."), false);
        DimensionsClient.exportBiomes(source.getLevel(), source.getServer().getWorldPath(EXPORT_PATH), radius);
        source.sendSuccess(new TextComponent("Done!"), false);
        return 1;
    }
}
