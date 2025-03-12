package github.pitbox46.fishingoverhaul.mixin;

import github.pitbox46.fishingoverhaul.ItemFishedEventPre;
import github.pitbox46.fishingoverhaul.duck.FishingHookDuck;
import github.pitbox46.fishingoverhaul.network.MinigameResultPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile implements FishingHookDuck {
    @Shadow private int nibble;

    @Unique
    private boolean fishingOverhaul$inMinigame = false;
    @Unique
    private List<ItemStack> fishingOverhaul$minigameLoot = NonNullList.of(ItemStack.EMPTY);
    @Unique
    private Player fishingOverhaul$player = null;
    @Unique
    private ItemStack fishingOverhaul$rod = ItemStack.EMPTY;
    @Unique
    private LootParams fishingOverhaul$lootParams = null;
    @Unique
    private LootTable fishingOverhaul$lootTable = null;

    protected FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "retrieve",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void stopRetrieveDuringMinigame(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        if (fishingOverhaul$inMinigame) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/player/ItemFishedEvent;<init>(Ljava/util/List;ILnet/minecraft/world/entity/projectile/FishingHook;)V",
                    remap = false),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void fishingOverhaulFishEvent(ItemStack pStack, CallbackInfoReturnable<Integer> cir, Player player, int i, @Nullable ItemFishedEvent event, LootParams lootparams, LootTable loottable, List<ItemStack> list) {
        if (!fishingOverhaul$inMinigame) {
            var overhaulEvent = new ItemFishedEventPre(list, this.onGround() ? 2 : 1, (FishingHook) (Object) this);
            // Fires an ItemFishedEvent in FishingOverhaul's event bus
            MinecraftForge.EVENT_BUS.post(overhaulEvent);
            if (overhaulEvent.isCanceled()) {
                cir.setReturnValue(overhaulEvent.getRodDamage());
                return;
            }
            fishingOverhaul$inMinigame = true;
            fishingOverhaul$minigameLoot = list;
            fishingOverhaul$player = player;
            fishingOverhaul$rod = pStack;
            fishingOverhaul$lootParams = lootparams;
            fishingOverhaul$lootTable = loottable;
            nibble = Integer.MAX_VALUE; // Make sure that this doesn't reach 0
            cir.setReturnValue(overhaulEvent.getRodDamage());
        }
    }

    @Override
    public void fishingOverhaul$completeGame(MinigameResultPacket.Result result) {
        switch (result) {
            case FAIL -> fishingOverhaul$loseGame();
            case SUCCESS -> fishingOverhaul$winGame(false);
            case CRIT -> fishingOverhaul$winGame(true);
        }
        this.discard();
    }

    // Copy of the code in #retrieve()
    @Unique
    private void fishingOverhaul$winGame(boolean crit) {
        Player player = fishingOverhaul$player;
        List<ItemStack> list = fishingOverhaul$minigameLoot;

        if(crit) {
            list = Stream.concat(list.stream(), fishingOverhaul$lootTable.getRandomItems(fishingOverhaul$lootParams).stream()).toList();
        }

        var event = new net.minecraftforge.event.entity.player.ItemFishedEvent(list, this.onGround() ? 2 : 1, (FishingHook)(Object)this);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            this.discard();
            return;
        }
        CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)fishingOverhaul$player, fishingOverhaul$rod, (FishingHook)(Object)this, list);

        for(ItemStack itemstack : list) {
            ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemstack);
            double d0 = player.getX() - this.getX();
            double d1 = player.getY() - this.getY();
            double d2 = player.getZ() - this.getZ();
            double d3 = 0.1D;
            itementity.setDeltaMovement(d0 * d3, d1 * d3 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * d3);
            this.level().addFreshEntity(itementity);
            player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, this.random.nextInt(6) + 1));
            if (itemstack.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT, 1);
            }
        }
    }

    @Unique
    private void fishingOverhaul$loseGame() {

    }
}
