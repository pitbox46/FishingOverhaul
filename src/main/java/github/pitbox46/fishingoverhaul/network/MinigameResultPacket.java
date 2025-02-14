package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.FishingOverhaul;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinigameResultPacket(boolean success) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MinigameResultPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FishingOverhaul.MODID, "minigame_result"));
    public static final StreamCodec<FriendlyByteBuf, MinigameResultPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MinigameResultPacket::success,
            MinigameResultPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
