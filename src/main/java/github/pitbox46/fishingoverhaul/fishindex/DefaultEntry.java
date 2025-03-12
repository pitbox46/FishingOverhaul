package github.pitbox46.fishingoverhaul.fishindex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DefaultEntry(float catchChance, float variability, float critChance, float speedMulti) implements IndexEntry {
    public static final Codec<DefaultEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.floatRange(0, 1).fieldOf("catch_chance").forGetter(DefaultEntry::catchChance),
                    Codec.FLOAT.fieldOf("variability").forGetter(DefaultEntry::variability),
                    Codec.floatRange(0, 1).fieldOf("crit_chance").forGetter(DefaultEntry::critChance),
                    Codec.FLOAT.optionalFieldOf("speed_multiplier", 1F).forGetter(DefaultEntry::speedMulti)
            ).apply(instance, DefaultEntry::new)
    );
}
