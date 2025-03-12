package github.pitbox46.fishingoverhaul.fishindex;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FishIndexManager extends SimpleJsonResourceReloadListener<FishIndex> {
    private DefaultEntry defaultIndex = new DefaultEntry(0.1F, 0.05F, 0.3F, 1F);
    private final Map<Item, IndexEntry> fishMap = new HashMap<>();

    public FishIndexManager(HolderLookup.Provider lookup) {
        super(FishIndex.CODEC, FileToIdConverter.json("fishing_index"));
    }

    public DefaultEntry getDefaultIndex() {
        return defaultIndex;
    }

    public IndexEntry getIndexFromItem(Item item) {
        return fishMap.getOrDefault(item, defaultIndex);
    }

    protected void apply(Map<ResourceLocation, FishIndex> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        for(Map.Entry<ResourceLocation, FishIndex> keyValue: objectIn.entrySet()) {
            FishIndex index = keyValue.getValue();
            index.defaultEntry().ifPresent(defaultEntry -> defaultIndex = defaultEntry);
            index.entries().forEach(e -> fishMap.put(e.item(), e));
        }
    }
}
