package github.pitbox46.fishingoverhaul.fishindex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record FishIndex(Optional<DefaultEntry> defaultEntry, List<FishEntry> entries) {
    public static final Codec<FishIndex> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DefaultEntry.CODEC.optionalFieldOf("default").forGetter(FishIndex::defaultEntry),
                    FishEntry.CODEC.listOf().fieldOf("entries").forGetter(FishIndex::entries)
            ).apply(instance, FishIndex::new)
    );
}
