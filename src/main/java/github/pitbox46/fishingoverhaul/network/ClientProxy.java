package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.MinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vec3 bobberPos, float catchChance) {
        Minecraft.getInstance().setScreen(new MinigameScreen(new TranslatableComponent("screen.fishingoverhaul.minigame"), bobberPos, catchChance));
    }
}
