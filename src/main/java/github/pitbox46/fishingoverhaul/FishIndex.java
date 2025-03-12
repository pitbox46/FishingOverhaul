package github.pitbox46.fishingoverhaul;

import com.google.gson.*;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public record FishIndex(Item item, float catchChance, float variability, float critChance, float speedMulti) {

    public static class Serializer implements JsonDeserializer<FishIndex>, JsonSerializer<FishIndex> {
        public FishIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Item item = null;
            if (obj.has("item")) {
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString()));
            }
            return new FishIndex(
                    item,
                    obj.get("catch_chance").getAsFloat(),
                    obj.get("variability").getAsFloat(),
                    getOrDefaultFloat(obj.get("crit_chance"), 0.3F),
                    getOrDefaultFloat(obj.get("speed_multiplier"), 1.0F)
            );
        }

        public JsonElement serialize(FishIndex src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", ForgeRegistries.ITEMS.getKey(src.item()).toString());
            obj.addProperty("catch_chance", src.catchChance());
            obj.addProperty("variability", src.variability());
            obj.addProperty("crit_chance", src.critChance());
            obj.addProperty("speed_multiplier", src.speedMulti());
            return obj;
        }

        public float getOrDefaultFloat(JsonElement element, float defaultValue) {
            if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
                return primitive.getAsFloat();
            }
            return defaultValue;
        }
    }
}
