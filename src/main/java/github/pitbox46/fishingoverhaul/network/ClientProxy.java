package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.MinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vector3d bobberPos, float catchChance) {
        Minecraft.getInstance().displayGuiScreen(new MinigameScreen(new TranslationTextComponent("screen.fishingoverhaul.minigame"), bobberPos, catchChance));
    }
}
