package github.pitbox46.fishingoverhaul.fishindex;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public interface IndexEntry {
    float catchChance();
    float variability();

    default Item item() {
        return Items.AIR;
    }
}
