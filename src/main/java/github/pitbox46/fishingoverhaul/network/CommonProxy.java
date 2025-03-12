package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.duck.FishingHookDuck;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CommonProxy {
    private static final Logger LOGGER = LogManager.getLogger();
    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Maps player UUID to FishingHook UUID
    public static final Map<UUID, UUID> CURRENTLY_PLAYING = new HashMap<>();

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleMinigameResult(NetworkEvent.Context ctx, MinigameResultPacket.Result result) {
        ServerPlayer player = ctx.getSender();
        Entity e = ((ServerLevel) player.level()).getEntities().get(CURRENTLY_PLAYING.get(player.getUUID()));
        if (e instanceof FishingHook hook) {
            ((FishingHookDuck) hook).fishingOverhaul$completeGame(result);
        }
        CURRENTLY_PLAYING.remove(player.getUUID());
    }

    //Client
    public void handleOpenMinigame(NetworkEvent.Context ctx, float catchChance, float critChance, float speedMulti) {
    }
}
