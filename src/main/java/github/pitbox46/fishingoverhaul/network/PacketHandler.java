package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.FishingOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "3.2.1";
    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("fishingoverhaul","main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int ID = 0;

    public static void init() {
        CHANNEL.registerMessage(
                ID++,
                MinigamePacket.class,
                (msg, pb) -> {
                    pb.writeFloat(msg.catchChance());
                    pb.writeDouble(msg.bobberPos().x());
                    pb.writeDouble(msg.bobberPos().y());
                    pb.writeDouble(msg.bobberPos().z());
                },
                pb -> new MinigamePacket(pb.readFloat(), new Vec3(pb.readDouble(), pb.readDouble(), pb.readDouble())),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> FishingOverhaul.PROXY.handleOpenMinigame(ctx.get(), msg.bobberPos(), msg.catchChance()));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                MinigameResultPacket.class,
                (msg, pb) -> {
                    pb.writeBoolean(msg.success());
                    pb.writeDouble(msg.bobberPos().x());
                    pb.writeDouble(msg.bobberPos().y());
                    pb.writeDouble(msg.bobberPos().z());
                },
                pb -> new MinigameResultPacket(pb.readBoolean(), new Vec3(pb.readDouble(), pb.readDouble(), pb.readDouble())),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> FishingOverhaul.PROXY.handleMinigameResult(ctx.get(), msg.bobberPos(), msg.success()));
                    ctx.get().setPacketHandled(true);
                });
    }
}
