package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.MinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ModClientPayloadHandler {
    public static void handleOpenMinigame(MinigamePacket packet, IPayloadContext ctx) {
        Minecraft.getInstance().setScreen(new MinigameScreen(
                Component.translatable("screen.fishingoverhaul.minigame"),
                packet.catchChance(),
                packet.critChance(),
                packet.speedMulti()
        ));
    }
}
