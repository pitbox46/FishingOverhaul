package github.pitbox46.fishingoverhaul.fishindex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DefaultEntry(float catchChance, float variability) implements IndexEntry {
    public static final Codec<DefaultEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.floatRange(0, 1).fieldOf("catch_chance").forGetter(DefaultEntry::catchChance),
                    Codec.FLOAT.fieldOf("variability").forGetter(DefaultEntry::variability)
            ).apply(instance, DefaultEntry::new)
    );
}
