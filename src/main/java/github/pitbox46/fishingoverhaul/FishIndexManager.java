package github.pitbox46.fishingoverhaul;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import koala.fishingreal.FishingConversion;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishIndexManager extends JsonReloadListener {
    private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(FishIndex.class, new FishIndex.Serializer()).create();
    private FishIndex defaultIndex = null;
    private List<FishIndex> fishIndices = ImmutableList.of();

    public FishIndexManager() {
        super(GSON_INSTANCE, "fishing_index");
    }

    public FishIndex getDefaultIndex() {
        return defaultIndex;
    }

    public FishIndex getIndexFromItem(Item item) {
        for(FishIndex index: fishIndices) {
            if(index.getItem().equals(item)) {
                return index;
            }
        }
        return defaultIndex;
    }

    private boolean checkMatchingItem(List<FishIndex> fishIndices, FishIndex fishIndex) {
        for(FishIndex index: fishIndices) {
            if(index.getItem().equals(fishIndex.getItem()))
                return true;
        }
        return false;
    }

    protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        List<FishIndex> outputIndicies = new ArrayList<>();

        for(Map.Entry<ResourceLocation, JsonElement> resourceLocation: objectIn.entrySet()) {
            JsonObject json = resourceLocation.getValue().getAsJsonObject();
            if(json.has("default")) {
                defaultIndex = GSON_INSTANCE.fromJson(json.get("default"), FishIndex.class);
            } else if(json.has("entries")) {
                for(JsonElement entry: json.getAsJsonArray("entries")) {
                    outputIndicies.removeIf(fishIndex -> checkMatchingItem(fishIndices, fishIndex));
                    outputIndicies.add(GSON_INSTANCE.fromJson(entry, FishIndex.class));
                }
            }
        }
        this.fishIndices = ImmutableList.copyOf(outputIndicies);
    }
}
