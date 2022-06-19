package github.pitbox46.fishingoverhaul.network;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static koala.fishingreal.FishingReal.FISHING_MANAGER;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;

public class CommonProxy {
    private static final Logger LOGGER = LogManager.getLogger();
    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static final Map<UUID,List<ItemStack>> CURRENTLY_PLAYING = new HashMap<>();

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleMinigameResult(NetworkEvent.Context ctx, Vec3 bobberPos, boolean success) {
        ServerPlayer player = ctx.getSender();
        List<ItemStack> itemStacks = CURRENTLY_PLAYING.get(player.getUUID());

        if(success && itemStacks != null) {
            if(ModList.get().isLoaded("fishingreal")) {
                //Code copied from fishingreal because it would not be easy to use the existing event method
                for (ItemStack stack : itemStacks) {
                    CompoundTag nbt = FISHING_MANAGER.matchWithStack(stack);
                    if (nbt != null) {
                        EntityType.loadEntityRecursive(nbt, player.getCommandSenderWorld(), (entity) -> {
                            Level w = player.getCommandSenderWorld();
                            if (w instanceof ServerLevel) {
                                ServerLevel world = (ServerLevel) w;
                                entity.moveTo(bobberPos.x(),bobberPos.y(),bobberPos.z(), player.getRandom().nextFloat() * 360, player.getRandom().nextFloat() * 360);
                                double dX = player.position().x() -bobberPos.x();
                                double dY = player.position().y() -bobberPos.y();
                                double dZ = player.position().z() -bobberPos.z();
                                double mult = 0.12D;
                                entity.setDeltaMovement(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);
                                world.addFreshEntity(new ExperienceOrb(player.level, player.position().x(), player.position().y() + 0.5D, player.position().z() + 0.5D, player.level.getRandom().nextInt(6) + 1));
                                if (stack.is(ItemTags.FISHES)) {
                                    player.awardStat(Stats.FISH_CAUGHT, 1);
                                }

                                if (FISHING_MANAGER.getConversionFromStack(stack).isRandomizeNBT() && entity instanceof Mob) {
                                    ((Mob) entity).finalizeSpawn(world, world.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, (SpawnGroupData) null, (CompoundTag) null);
                                }

                                return !world.addWithUUID(entity) ? null : entity;
                            } else {
                                return null;
                            }
                        });
                    }
                }
            }
            else {
                for (ItemStack itemStack : itemStacks) {
                    ItemEntity entity = new ItemEntity(player.level, bobberPos.x(), bobberPos.y(), bobberPos.z(), itemStack);
                    entity.setPos(bobberPos.x(), bobberPos.y(), bobberPos.z());
                    double d0 = player.getX() - bobberPos.x();
                    double d1 = player.getY() - bobberPos.y();
                    double d2 = player.getZ() - bobberPos.z();
                    entity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    player.level.addFreshEntity(entity);
                    player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, player.getRandom().nextInt(6) + 1));
                    if (entity.getItem().is(ItemTags.FISHES)) {
                        player.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }
            }
        }
        CURRENTLY_PLAYING.remove(player.getUUID());
    }

    //Client
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vec3 bobberPos, float catchChance) {
    }
}
