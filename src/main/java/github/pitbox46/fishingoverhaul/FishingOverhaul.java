package github.pitbox46.fishingoverhaul;

import github.pitbox46.fishingoverhaul.network.ClientProxy;
import github.pitbox46.fishingoverhaul.network.CommonProxy;
import github.pitbox46.fishingoverhaul.network.MinigamePacket;
import github.pitbox46.fishingoverhaul.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod("fishingoverhaul")
public class FishingOverhaul {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final FishIndexManager FISH_INDEX_MANAGER = new FishIndexManager();
    public static CommonProxy PROXY;

    public FishingOverhaul() {
        MinecraftForge.EVENT_BUS.register(this);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFished(ItemFishedEvent event) {
        event.setCanceled(true);
        List<ItemStack> lootList = event.getDrops();
        float catchChance = 1f;
        float variability = 0f;
        for(ItemStack itemStack: lootList) {
            if(FISH_INDEX_MANAGER.getIndexFromItem(itemStack.getItem()).catchChance() < catchChance) {
                catchChance = FISH_INDEX_MANAGER.getIndexFromItem(itemStack.getItem()).catchChance();
                variability = FISH_INDEX_MANAGER.getIndexFromItem(itemStack.getItem()).variability();
            }
        }
        catchChance += (variability * 2 * (event.getPlayer().getRandom().nextFloat() - 0.5));

        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()), new MinigamePacket(catchChance,  event.getHookEntity().position()));
        CommonProxy.CURRENTLY_PLAYING.put(event.getPlayer().getUUID(), lootList);
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(FISH_INDEX_MANAGER);
    }
}
