package github.pitbox46.fishingoverhaul.network;

import github.pitbox46.fishingoverhaul.duck.FishingHookDuck;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class ModServerPayloadHandler {
    public static final Map<UUID, UUID> CURRENTLY_PLAYING = new HashMap<>();

    public static void handleMinigameResult(MinigameResultPacket packet, IPayloadContext ctx) {
        Player player = ctx.player();
        Entity e = ((ServerLevel) player.level()).getEntities().get(CURRENTLY_PLAYING.get(player.getUUID()));
        if (e instanceof FishingHook hook) {
            ((FishingHookDuck) hook).fishingOverhaul$completeGame(packet.result());
        }
        CURRENTLY_PLAYING.remove(player.getUUID());
    }
}
