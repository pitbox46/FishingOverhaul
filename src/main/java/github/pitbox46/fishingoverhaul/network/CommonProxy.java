package github.pitbox46.fishingoverhaul.network;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommonProxy {
    private static final Logger LOGGER = LogManager.getLogger();
    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static final Map<UUID,List<ItemStack>> CURRENTLY_PLAYING = new HashMap<>();

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleMinigameResult(NetworkEvent.Context ctx, Vector3d bobberPos, boolean success) {
        ServerPlayerEntity player = ctx.getSender();
        List<ItemStack> itemStacks = CURRENTLY_PLAYING.get(player.getUniqueID());
        if(success && itemStacks != null) {
            for(ItemStack itemstack : itemStacks) {
                ItemEntity itementity = new ItemEntity(player.world, bobberPos.getX(), bobberPos.getY(), bobberPos.getZ(), itemstack);
                double d0 = player.getPosX() - bobberPos.getX();
                double d1 = player.getPosY() - bobberPos.getY();
                double d2 = player.getPosZ() - bobberPos.getZ();
                itementity.setMotion(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                player.world.addEntity(itementity);
                player.world.addEntity(new ExperienceOrbEntity(player.world, player.getPosX(), player.getPosY() + 0.5D, player.getPosZ() + 0.5D, player.getRNG().nextInt(6) + 1));
                if (itemstack.getItem().isIn(ItemTags.FISHES)) {
                    player.addStat(Stats.FISH_CAUGHT, 1);
                }
            }
        }
        CURRENTLY_PLAYING.remove(player.getUniqueID());
    }

    //Client
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vector3d bobberPos, float catchChance) {
    }
}
