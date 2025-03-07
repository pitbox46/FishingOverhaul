package github.pitbox46.fishingoverhaul.fishindex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record FishEntry(Item item, float catchChance, float variability) implements IndexEntry {
    public static final Codec<FishEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(FishEntry::item),
                    Codec.floatRange(0, 1).fieldOf("catch_chance").forGetter(FishEntry::catchChance),
                    Codec.FLOAT.fieldOf("variability").forGetter(FishEntry::variability)
            ).apply(instance, FishEntry::new)
    );
}
