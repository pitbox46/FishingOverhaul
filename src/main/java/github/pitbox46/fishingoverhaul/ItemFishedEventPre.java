package github.pitbox46.fishingoverhaul;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nonnegative;
import java.util.List;

/**
 * Runs before {@link ItemFishedEvent}
 */
@Cancelable
public class ItemFishedEventPre extends PlayerEvent {
    private final NonNullList<ItemStack> stacks = NonNullList.create();
    private final FishingHook hook;
    private int rodDamage;

    public ItemFishedEventPre(List<ItemStack> stacks, int rodDamage, FishingHook hook)
    {
        super(hook.getPlayerOwner());
        this.stacks.addAll(stacks);
        this.rodDamage = rodDamage;
        this.hook = hook;
    }

    /**
     * Get the damage the rod will take.
     * @return The damage the rod will take
     */
    public int getRodDamage()
    {
        return rodDamage;
    }

    /**
     * Specifies the amount of damage that the fishing rod should take.
     * This is not added to the pre-existing damage to be taken.
     * @param rodDamage The damage the rod will take. Must be nonnegative
     */
    public void damageRodBy(@Nonnegative int rodDamage)
    {
        Preconditions.checkArgument(rodDamage >= 0);
        this.rodDamage = rodDamage;
    }

    /**
     * Use this to get the items the player will receive.
     * You cannot use this to modify the drops the player will get.
     * If you want to affect the loot, you should use LootTables.
     */
    public NonNullList<ItemStack> getDrops()
    {
        return stacks;
    }

    /**
     * Use this to stuff related to the hook itself, like the position of the bobber.
     */
    public FishingHook getHookEntity()
    {
        return hook;
    }
}
