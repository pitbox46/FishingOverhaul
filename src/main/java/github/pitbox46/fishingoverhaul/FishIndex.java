package github.pitbox46.fishingoverhaul;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public class FishIndex {
    private final Item item;
    private final float catchChance;
    private final float variability;
    public FishIndex(Item item, float catchChance, float variability) {
        this.item = item;
        this.catchChance = catchChance;
        this.variability = variability;
    }

    public float getCatchChance() {
        return catchChance;
    }

    public float getVariability() {
        return variability;
    }

    public Item getItem() {
        return item;
    }

    public static class Serializer implements JsonDeserializer<FishIndex>, JsonSerializer<FishIndex> {
        public FishIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Item item = null;
            if(obj.has("item")) {
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString()));
            }
            float catchChance = obj.get("catch_chance").getAsFloat();
            float variability = obj.get("variability").getAsFloat();
            return new FishIndex(item, catchChance, variability);
        }

        public JsonElement serialize(FishIndex src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", src.getItem().getRegistryName().toString());
            obj.addProperty("catch_chance", src.getCatchChance());
            obj.addProperty("variability", src.getVariability());
            return obj;
        }
    }
}
