package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.MinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vec3 bobberPos, float catchChance) {
        Minecraft.getInstance().setScreen(new MinigameScreen(Component.translatable("screen.fishingoverhaul.minigame"), bobberPos, catchChance));
    }
}
