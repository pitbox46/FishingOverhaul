package github.pitbox46.fishingoverhaul.fishindex;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import github.pitbox46.fishingoverhaul.FishingOverhaul;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FishIndexManager extends SimpleJsonResourceReloadListener {
    private DefaultEntry defaultIndex = new DefaultEntry(0.1F, 0.05F);
    private final Map<Item, IndexEntry> fishMap = new HashMap<>();
    private final DynamicOps<JsonElement> dynamicOps;

    public FishIndexManager(HolderLookup.Provider lookup) {
        super(new GsonBuilder().create(), "fishing_index");
        dynamicOps = lookup.createSerializationContext(JsonOps.INSTANCE);
    }

    public DefaultEntry getDefaultIndex() {
        return defaultIndex;
    }

    public IndexEntry getIndexFromItem(Item item) {
        return fishMap.getOrDefault(item, defaultIndex);
    }

    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        for(Map.Entry<ResourceLocation, JsonElement> keyValue: objectIn.entrySet()) {
            FishIndex index = FishIndex.CODEC
                    .parse(dynamicOps, keyValue.getValue())
                    .resultOrPartial(m -> FishingOverhaul.LOGGER.warn("Could not read fish index: {}", m))
                    .orElseGet(() -> new FishIndex(Optional.empty(), List.of()));
            index.defaultEntry().ifPresent(defaultEntry -> defaultIndex = defaultEntry);
            index.entries().forEach(e -> fishMap.put(e.item(), e));
        }
    }
}
