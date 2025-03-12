package github.pitbox46.fishingoverhaul.mixin;

import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaSounds;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import github.pitbox46.fishingoverhaul.ItemFishedEventPre;
import github.pitbox46.fishingoverhaul.duck.FishingHookDuck;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(AquaFishingBobberEntity.class)
public abstract class AquaFishingBobberEntityMixin extends FishingHook implements FishingHookDuck {
    @Shadow protected abstract void spawnLoot(Player angler, List<ItemStack> lootEntries);
    @Shadow public abstract boolean hasHook();
    @Shadow private Hook hook;
    @Shadow protected abstract List<ItemStack> getLoot(LootParams lootParams, ServerLevel serverLevel);
    @Shadow private ItemStack fishingRod;

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

    public AquaFishingBobberEntityMixin(EntityType<? extends FishingHook> entityType, Level level) {
        super(entityType, level);
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
                    value = "NEW",
                    target = "net/minecraftforge/event/entity/player/ItemFishedEvent",
                    ordinal = 0,
                    remap = false),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void fishingOverhaulFishEvent(ItemStack stack, CallbackInfoReturnable<Integer> cir, boolean isAdminRod, Player angler, Level level, int rodDamage, ItemFishedEvent event, ServerLevel serverLevel, LootParams lootParams, List<ItemStack> lootEntries) {
        if (!fishingOverhaul$inMinigame) {
            var overhaulEvent = new ItemFishedEventPre(lootEntries, this.onGround() ? 2 : 1, this);
            // Fires an ItemFishedEvent in FishingOverhaul's event bus
            MinecraftForge.EVENT_BUS.post(overhaulEvent);
            if (overhaulEvent.isCanceled()) {
                cir.setReturnValue(overhaulEvent.getRodDamage());
                return;
            }
            fishingOverhaul$inMinigame = true;
            fishingOverhaul$minigameLoot = lootEntries;
            fishingOverhaul$player = angler;
            fishingOverhaul$rod = stack;
            fishingOverhaul$lootParams = lootParams;
            nibble = Integer.MAX_VALUE; // Make sure that this doesn't reach 0
            cir.setReturnValue(overhaulEvent.getRodDamage());
        }
    }

    @Override
    public void fishingOverhaul$completeGame(boolean win) {
        if (win) {
            fishingOverhaul$winGame();
        } else {
            fishingOverhaul$loseGame();
        }
        this.discard();
    }

    // Copy of the code in #retrieve()
    @Unique
    private void fishingOverhaul$winGame() {
        var angler = fishingOverhaul$player;
        var stack = fishingOverhaul$rod;
        var lootEntries = fishingOverhaul$minigameLoot;
        var lootParams = fishingOverhaul$lootParams;
        ServerLevel serverLevel = (ServerLevel) this.level();

        var event = new ItemFishedEvent(fishingOverhaul$minigameLoot, this.onGround() ? 2 : 1, this);
        event = new ItemFishedEvent(lootEntries, this.onGround() ? 2 : 1, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            this.discard();
            return;
        }

        CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)angler, stack, this, lootEntries);
        this.spawnLoot(angler, lootEntries);
        if (this.hasHook() && this.hook.getDoubleCatchChance() > 0.0 && this.random.nextDouble() <= this.hook.getDoubleCatchChance()) {
            List<ItemStack> doubleLoot = this.getLoot(lootParams, serverLevel);
            if (!doubleLoot.isEmpty()) {
                MinecraftForge.EVENT_BUS.post(new ItemFishedEvent(doubleLoot, 0, this));
                this.spawnLoot(angler, doubleLoot);
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            }
        }

        if (!angler.isCreative()) {
            ItemStackHandler rodHandler = AquaFishingRodItem.getHandler(this.fishingRod);
            ItemStack bait = rodHandler.getStackInSlot(1);
            if (!bait.isEmpty()) {
                if (bait.hurt(1, serverLevel.random, null)) {
                    bait.shrink(1);
                    this.playSound(AquaSounds.BOBBER_BAIT_BREAK.get(), 0.7F, 0.2F);
                }

                rodHandler.setStackInSlot(1, bait);
            }
        }
    }

    @Unique
    private void fishingOverhaul$loseGame() {

    }
}