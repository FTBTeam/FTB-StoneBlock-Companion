package dev.ftb.ftbsbc.dimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.DimensionsClient;
import net.minecraft.network.FriendlyByteBuf;

public class ShowSelectionGui extends BaseS2CMessage {
	public ShowSelectionGui() {}

	public ShowSelectionGui(FriendlyByteBuf buf) {}

	@Override
	public MessageType getType() {
		return FTBStoneBlock.SHOW_SELECTION_GUI;
	}

	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(DimensionsClient::openSelectionScreen);
	}
}
