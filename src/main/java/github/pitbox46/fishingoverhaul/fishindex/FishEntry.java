package github.pitbox46.fishingoverhaul.fishindex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record FishEntry(Item item, float catchChance, float variability, float critChance, float speedMulti) implements IndexEntry {
    public static final Codec<FishEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(FishEntry::item),
                    Codec.floatRange(0, 1).fieldOf("catch_chance").forGetter(FishEntry::catchChance),
                    Codec.FLOAT.fieldOf("variability").forGetter(FishEntry::variability),
                    Codec.floatRange(0, 1).optionalFieldOf("crit_chance", 0.3F).forGetter(FishEntry::critChance),
                    Codec.FLOAT.optionalFieldOf("speed_multiplier", 1F).forGetter(FishEntry::speedMulti)
            ).apply(instance, FishEntry::new)
    );
}
