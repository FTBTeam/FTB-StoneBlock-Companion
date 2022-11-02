package dev.ftb.ftbsbc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.ftbsbc.dimensions.DimensionsClient;
import dev.ftb.ftbsbc.dimensions.DimensionsManager;
import dev.ftb.ftbsbc.dimensions.arguments.DimensionCommandArgument;
import dev.ftb.ftbsbc.dimensions.arguments.PrebuiltCommandArgument;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.level.ArchivedDimension;
import dev.ftb.ftbsbc.dimensions.level.DimensionStorage;
import dev.ftb.ftbsbc.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class FTBStoneBlockCommands {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final LevelResource EXPORT_PATH = new LevelResource("stoneblock-export.png");

    public static final DynamicCommandExceptionType NOT_PARTY_TEAM = new DynamicCommandExceptionType((object) -> new TextComponent("[%s] is not a party team...".formatted(object)));
    public static final DynamicCommandExceptionType DIM_MISSING = new DynamicCommandExceptionType((object) -> new TextComponent("[%s] can not be found".formatted(object)));
    public static final DynamicCommandExceptionType NO_DIM = new DynamicCommandExceptionType((object) -> new TextComponent("No dimension found for %s".formatted(object)));

    public static void setup() {
        ArgumentTypes.register(FTBStoneBlock.MOD_ID + ":prebuilt", PrebuiltCommandArgument.class, new EmptyArgumentSerializer<>(PrebuiltCommandArgument::create));
        ArgumentTypes.register(FTBStoneBlock.MOD_ID + ":archived", DimensionCommandArgument.class, new EmptyArgumentSerializer<>(DimensionCommandArgument::create));
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
                .then(Commands.literal("prune-all").executes(context -> prune(context.getSource())))
                .then(Commands.literal("prune").then(Commands.argument("dimension", DimensionCommandArgument.create()).executes(context -> prune(context.getSource(), DimensionCommandArgument.get(context, "dimension")))))
                .then(Commands.literal("restore")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("dimension", DimensionCommandArgument.create())
                                        .executes(context -> restore(context.getSource(), DimensionCommandArgument.get(context, "dimension"), EntityArgument.getPlayer(context, "player")))
                                )
                        )
                )
                .then(Commands.literal("lobby").executes(context -> lobby(context.getSource())))
                .then(Commands.literal("home").executes(context -> home(context.getSource())))
        );

        commandDispatcher.register(Commands.literal("ftbstoneblock").redirect(commands));
    }

    private static int restore(CommandSourceStack source, ArchivedDimension dimension, ServerPlayer player) throws CommandSyntaxException {
        PartyTeam party = FTBTeamsAPI.getManager().createParty(player, player.getName().getString() + " Party", false).getValue();
        ResourceKey<Level> levelResourceKey = DimensionStorage.get(source.getServer()).putDimension(party, dimension.dimensionName());

        // Remove the dimension from the archived dims
        DimensionStorage.get(source.getServer()).getArchivedDimensions().remove(dimension);
        DimensionStorage.get(source.getServer()).setDirty();

        DynamicDimensionManager.teleport(player, levelResourceKey);

        source.sendSuccess(new TextComponent("Successfully restored dimension").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source, ArchivedDimension dimension) throws CommandSyntaxException {
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        if (!archivedDimensions.contains(dimension)) {
            throw DIM_MISSING.create(dimension.dimensionName());
        }

        DynamicDimensionManager.destroy(source.getServer(), ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension.dimensionName()));

        archivedDimensions.remove(dimension);
        DimensionStorage.get(source.getServer()).setDirty();

        source.sendSuccess(new TextComponent("Successfully pruned %s".formatted(dimension.dimensionName())).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        int size = archivedDimensions.size();

        for (ArchivedDimension e : archivedDimensions) {
            DynamicDimensionManager.destroy(server, ResourceKey.create(Registry.DIMENSION_REGISTRY, e.dimensionName()));
        }

        DimensionStorage.get(source.getServer()).getArchivedDimensions().clear();
        DimensionStorage.get(source.getServer()).setDirty();

        source.sendSuccess(new TextComponent("Successfully pruned %s dimensions".formatted(size)).withStyle(ChatFormatting.GREEN), false);

        return 0;
    }

    private static int listArchived(CommandSourceStack source) {
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        if (archivedDimensions.isEmpty()) {
            source.sendFailure(new TextComponent("No archived dimensions available"));
            return -1;
        }

        for (ArchivedDimension archivedDimension : archivedDimensions) {
            source.sendSuccess(new TextComponent("%s: [team=%s] [owner=%s]".formatted(
                    archivedDimension.dimensionName(),
                    archivedDimension.teamName(),
                    archivedDimension.teamOwner()
            )), false);
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
