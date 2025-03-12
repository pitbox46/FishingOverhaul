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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    @SubscribeEvent
    //Listens for ItemFishedEventPre
    public void onFished(ItemFishedEventPre event) {
        List<ItemStack> lootList = event.getDrops();
        FishIndex entry = FISH_INDEX_MANAGER.getDefaultIndex();
        for(ItemStack itemStack: lootList) {
            FishIndex newEntry = FISH_INDEX_MANAGER.getIndexFromItem(itemStack.getItem());
            //Find the rarest item
            if (newEntry.catchChance() <= entry.catchChance()) {
                entry = newEntry;
            }
        }
        float catchChance = entry.catchChance() + (float) (entry.variability() * 2 * (event.getEntity().getRandom().nextFloat() - 0.5));

        PacketHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()),
                new MinigamePacket(catchChance, entry.critChance(), entry.speedMulti())
        );
        CommonProxy.CURRENTLY_PLAYING.put(event.getEntity().getUUID(), event.getHookEntity().getUUID());
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(FISH_INDEX_MANAGER);
    }
}
