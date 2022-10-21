package dev.ftb.ftbsbc.dimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.ftbsbc.FTBStoneBlock;
import dev.ftb.ftbsbc.dimensions.DimensionsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CreateDimensionForTeam extends BaseC2SMessage {
	private final ResourceLocation prebuiltLocation;

	public CreateDimensionForTeam(ResourceLocation location) {
		prebuiltLocation = location;
	}

	public CreateDimensionForTeam(FriendlyByteBuf buf) {
		this.prebuiltLocation = buf.readResourceLocation();
	}

	@Override
	public MessageType getType() {
		return FTBStoneBlock.CREATE_DIMENSION_FOR_TEAM;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(prebuiltLocation);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> DimensionsManager.INSTANCE.createDimForTeam((ServerPlayer) context.getPlayer(), prebuiltLocation));
	}
}
