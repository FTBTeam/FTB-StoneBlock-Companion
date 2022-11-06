package dev.ftb.ftbsbc;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.ftb.ftbsbc.config.FTBSBConfig;
import dev.ftb.ftbsbc.dimensions.DimensionsClient;
import dev.ftb.ftbsbc.dimensions.DimensionsMain;
import dev.ftb.ftbsbc.dimensions.DimensionsRegistry;
import dev.ftb.ftbsbc.dimensions.net.CreateDimensionForTeam;
import dev.ftb.ftbsbc.dimensions.net.ShowSelectionGui;
import dev.ftb.ftbsbc.dimensions.net.SyncArchivedDimensions;
import dev.ftb.ftbsbc.dimensions.net.UpdateDimensionsList;
import dev.ftb.ftbsbc.portal.PortalClient;
import dev.ftb.ftbsbc.portal.PortalRegistry;
import dev.ftb.ftbsbc.tools.ToolsClient;
import dev.ftb.ftbsbc.tools.ToolsMain;
import dev.ftb.ftbsbc.tools.ToolsRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Stream;

@Mod(FTBStoneBlock.MOD_ID)
public class FTBStoneBlock {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "ftbsbc";

    public static final SimpleNetworkManager NET = SimpleNetworkManager.create(FTBStoneBlock.MOD_ID);
    public static final MessageType UPDATE_DIMENSION_LIST = NET.registerS2C("update_dimensions_list", UpdateDimensionsList::new);
    public static final MessageType CREATE_DIMENSION_FOR_TEAM = NET.registerC2S("create_dimension_for_team", CreateDimensionForTeam::new);
    public static final MessageType SHOW_SELECTION_GUI = NET.registerS2C("show_start_selection", ShowSelectionGui::new);
    public static final MessageType SYNC_ARCHIVED_DIMENSIONS = NET.registerS2C("sync_archived_dimensions", SyncArchivedDimensions::new);

    public FTBStoneBlock() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FTBSBConfig.COMMON_CONFIG);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register everything that's part of the tools system
        Stream.of(ToolsRegistry.REGISTERS, DimensionsRegistry.REGISTERS, PortalRegistry.REGISTERS)
                .flatMap(List::stream)
                .forEach(e -> e.register(modBus));

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::postSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        FTBStoneBlockCommands.setup();
        DimensionsMain.setup();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ToolsClient.init();
        PortalClient.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DimensionsClient::init);
    }

    public void postSetup(FMLLoadCompleteEvent event) {
        ToolsMain.setup();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        FTBStoneBlockCommands.register(event.getDispatcher());
    }
}
