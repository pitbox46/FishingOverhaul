package github.pitbox46.fishingoverhaul.network;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class ModServerPayloadHandler {
    public static final Map<UUID,List<ItemStack>> CURRENTLY_PLAYING = new HashMap<>();

    public static void handleMinigameResult(MinigameResultPacket packet, IPayloadContext ctx) {
        Player player = ctx.player();
        List<ItemStack> itemStacks = CURRENTLY_PLAYING.get(player.getUUID());

        if(packet.success() && itemStacks != null) {
            for (ItemStack stack : itemStacks) {
                spawnItem(player, packet.bobberPos(), stack);
            }
        }
        CURRENTLY_PLAYING.remove(player.getUUID());
    }

    public static void spawnItem(Player player, Vec3 bobberPos, ItemStack itemStack) {
        ItemEntity entity = new ItemEntity(player.level(), bobberPos.x(), bobberPos.y(), bobberPos.z(), itemStack);
        entity.setPos(bobberPos.x(), bobberPos.y(), bobberPos.z());
        double d0 = player.getX() - bobberPos.x();
        double d1 = player.getY() - bobberPos.y();
        double d2 = player.getZ() - bobberPos.z();
        entity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
        player.level().addFreshEntity(entity);
        player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, player.getRandom().nextInt(6) + 1));
        if (entity.getItem().is(ItemTags.FISHES)) {
            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
    }
}
