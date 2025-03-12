package github.pitbox46.fishingoverhaul;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishIndexManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(FishIndex.class, new FishIndex.Serializer()).create();
    private FishIndex defaultIndex = new FishIndex(Items.AIR, 0.1F, 0.05F, 0.3F, 1.0F);
    private final Map<Item, FishIndex> fishMap = new HashMap<>();

    public FishIndexManager() {
        super(GSON_INSTANCE, "fishing_index");
    }

    public FishIndex getDefaultIndex() {
        return defaultIndex;
    }

    public FishIndex getIndexFromItem(Item item) {
        return fishMap.getOrDefault(item, defaultIndex);
    }

    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        for(Map.Entry<ResourceLocation, JsonElement> resourceLocation: objectIn.entrySet()) {
            JsonObject json = resourceLocation.getValue().getAsJsonObject();
            if(json.has("default")) {
                defaultIndex = GSON_INSTANCE.fromJson(json.get("default"), FishIndex.class);
            }
            if(json.has("entries")) {
                for(JsonElement entry: json.getAsJsonArray("entries")) {
                    FishIndex fish = GSON_INSTANCE.fromJson(entry, FishIndex.class);
                    fishMap.put(fish.item(), fish);
                }
            }
        }
    }
}
