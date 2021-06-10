package github.pitbox46.fishingoverhaul;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import github.pitbox46.fishingoverhaul.network.ClientProxy;
import github.pitbox46.fishingoverhaul.network.CommonProxy;
import github.pitbox46.fishingoverhaul.network.MinigamePacket;
import github.pitbox46.fishingoverhaul.network.PacketHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod("fishingoverhaul")
public class FishingOverhaul {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Map<Item, FishIndex> FISH_INDEX = new HashMap<>();
    public static FishIndex DEFAULT_INDEX = new FishIndex(0.1f, 0.05f);
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
            if(FISH_INDEX.get(itemStack.getItem()) != null && FISH_INDEX.get(itemStack.getItem()).getCatchChance() < catchChance) {
                catchChance = FISH_INDEX.get(itemStack.getItem()).getCatchChance();
                variability = FISH_INDEX.get(itemStack.getItem()).getVariability();
            } else if(FISH_INDEX.get(itemStack.getItem()) == null && DEFAULT_INDEX.getCatchChance() < catchChance) {
                catchChance = DEFAULT_INDEX.getCatchChance();
                variability = DEFAULT_INDEX.getVariability();
            }
        }
        catchChance += (variability * 2 * (event.getPlayer().getRNG().nextFloat() - 0.5));

        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new MinigamePacket(catchChance,  event.getHookEntity().getPositionVec()));
        CommonProxy.CURRENTLY_PLAYING.put(event.getPlayer().getUniqueID(), lootList);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartingEvent event) {
        readFishingIndex();
    }

    private void readFishingIndex() {
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        for(IResourcePack pack: resourceManager.getResourcePackStream().toArray(IResourcePack[]::new)) {
            try{
                try(
                        InputStream inputStream = pack.getResourceStream(ResourcePackType.SERVER_DATA, new ResourceLocation("fishingoverhaul", "fishing_index.json"));
                        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                ) {
                    JsonElement json = new JsonParser().parse(reader);
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        Item item = entry.getKey().equals("default") ? null : ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
                        float catchChance = entry.getValue().getAsJsonObject().getAsJsonPrimitive("catch_chance").getAsFloat();
                        float variability = entry.getValue().getAsJsonObject().getAsJsonPrimitive("variability").getAsFloat();
                        if(item == null) {
                            DEFAULT_INDEX = new FishIndex(catchChance, variability);
                        } else {
                            FISH_INDEX.put(item, new FishIndex(catchChance, variability));
                        }
                    });
                }
            } catch (IOException e) {
                System.out.println("Something went wrong while parsing fishing_index.json");
                e.printStackTrace();
            }
        }
    }
}
