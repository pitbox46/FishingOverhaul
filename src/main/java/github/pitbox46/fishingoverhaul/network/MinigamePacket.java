package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.FishingOverhaul;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinigamePacket(float catchChance, float critChance, float speedMulti) implements CustomPacketPayload {
    public static final Type<MinigamePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FishingOverhaul.MODID, "minigame"));
    public static final StreamCodec<FriendlyByteBuf, MinigamePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            MinigamePacket::catchChance,
            ByteBufCodecs.FLOAT,
            MinigamePacket::critChance,
            ByteBufCodecs.FLOAT,
            MinigamePacket::speedMulti,
            MinigamePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
