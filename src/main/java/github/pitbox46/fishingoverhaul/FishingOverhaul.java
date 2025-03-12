package github.pitbox46.fishingoverhaul;

import github.pitbox46.fishingoverhaul.fishindex.FishIndexManager;
import github.pitbox46.fishingoverhaul.fishindex.IndexEntry;
import github.pitbox46.fishingoverhaul.network.MinigameResultPacket;
import github.pitbox46.fishingoverhaul.network.ModClientPayloadHandler;
import github.pitbox46.fishingoverhaul.network.ModServerPayloadHandler;
import github.pitbox46.fishingoverhaul.network.MinigamePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod("fishingoverhaul")
public class FishingOverhaul {
    public static final Logger LOGGER = LogManager.getLogger();
    public static FishIndexManager FISH_INDEX_MANAGER;
    public static final String MODID = "fishingoverhaul";

    public FishingOverhaul(ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        container.getEventBus().addListener(this::registerPackets);
    }

    public void registerPackets(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                MinigamePacket.TYPE,
                MinigamePacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> ModClientPayloadHandler.handleOpenMinigame(payload, context))
        );
        registrar.playToServer(
                MinigameResultPacket.TYPE,
                MinigameResultPacket.CODEC,
                new MainThreadPayloadHandler<>(ModServerPayloadHandler::handleMinigameResult)
        );
    }

    @SubscribeEvent
    public void onFished(ItemFishedEventPre event) {
        List<ItemStack> lootList = event.getDrops();
        IndexEntry entry = FISH_INDEX_MANAGER.getDefaultIndex();
        for(ItemStack itemStack: lootList) {
            IndexEntry newEntry = FISH_INDEX_MANAGER.getIndexFromItem(itemStack.getItem());
            //Find the rarest item
            if (newEntry.catchChance() <= entry.catchChance()) {
                entry = newEntry;
            }
        }
        float catchChance = entry.catchChance() + (entry.variability() * 2 * (event.getEntity().getRandom().nextFloat() - 0.5F));

        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new MinigamePacket(
                catchChance,
                entry.critChance(),
                entry.speedMulti()
        ));
        ModServerPayloadHandler.CURRENTLY_PLAYING.put(event.getEntity().getUUID(), event.getHookEntity().getUUID());
    }

    @SubscribeEvent
    public void addReloadListener(AddServerReloadListenersEvent event) {
        FISH_INDEX_MANAGER = new FishIndexManager(event.getRegistryAccess());
        event.addListener(ResourceLocation.fromNamespaceAndPath(MODID, "fish_index"), FISH_INDEX_MANAGER);
    }
}
