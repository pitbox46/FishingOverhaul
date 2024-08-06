package github.pitbox46.fishingoverhaul;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;

public record FishIndex(Item item, float catchChance, float variability) {

    public static class Serializer implements JsonDeserializer<FishIndex>, JsonSerializer<FishIndex> {
        public FishIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Item item = Items.AIR;
            if (obj.has("item")) {
                item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(obj.get("item").getAsString()));
            }
            float catchChance = obj.get("catch_chance").getAsFloat();
            float variability = obj.get("variability").getAsFloat();
            return new FishIndex(item, catchChance, variability);
        }

        public JsonElement serialize(FishIndex src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", BuiltInRegistries.ITEM.getKey(src.item()).toString());
            obj.addProperty("catch_chance", src.catchChance());
            obj.addProperty("variability", src.variability());
            return obj;
        }
    }
}
