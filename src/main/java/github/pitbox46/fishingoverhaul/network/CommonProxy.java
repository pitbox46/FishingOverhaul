package github.pitbox46.fishingoverhaul.network;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static koala.fishingreal.FishingReal.FISHING_MANAGER;

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

    public void handleMinigameResult(NetworkEvent.Context ctx, Vector3d bobberPos, boolean success) {
        ServerPlayerEntity player = ctx.getSender();
        List<ItemStack> itemStacks = CURRENTLY_PLAYING.get(player.getUniqueID());

        if(success && itemStacks != null) {
            if(ModList.get().isLoaded("fishingreal")) {
                //Code copied from fishingreal because it would not be easy to use the existing event method
                for (ItemStack stack : itemStacks) {
                    CompoundNBT nbt = FISHING_MANAGER.matchWithStack(stack);
                    if (nbt != null) {
                        EntityType.loadEntityAndExecute(nbt, player.getEntityWorld(), (entity) -> {
                            World w = player.getEntityWorld();
                            if (w instanceof ServerWorld) {
                                ServerWorld world = (ServerWorld) w;
                                entity.setLocationAndAngles(bobberPos.getX(),bobberPos.getY(),bobberPos.getZ(), player.getRNG().nextFloat() * 360, player.getRNG().nextFloat() * 360);
                                double dX = player.getPositionVec().getX() -bobberPos.getX();
                                double dY = player.getPositionVec().getY() -bobberPos.getY();
                                double dZ = player.getPositionVec().getZ() -bobberPos.getZ();
                                double mult = 0.12D;
                                entity.setMotion(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);
                                world.addEntity(new ExperienceOrbEntity(player.world, player.getPositionVec().getX(), player.getPositionVec().getY() + 0.5D, player.getPositionVec().getZ() + 0.5D, player.world.getRandom().nextInt(6) + 1));
                                if (stack.getItem().isIn(ItemTags.FISHES)) {
                                    player.addStat(Stats.FISH_CAUGHT, 1);
                                }

                                if (FISHING_MANAGER.getConversionFromStack(stack).isRandomizeNBT() && entity instanceof MobEntity) {
                                    ((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(player.getPosition()), SpawnReason.NATURAL, (ILivingEntityData) null, (CompoundNBT) null);
                                }

                                return !world.summonEntity(entity) ? null : entity;
                            } else {
                                return null;
                            }
                        });
                    }
                }
            } else {
                for (ItemStack itemStack : itemStacks) {
                    ItemEntity entity = new ItemEntity(player.world, bobberPos.getX(), bobberPos.getY(), bobberPos.getZ(), itemStack);
                    entity.setPosition(bobberPos.getX(), bobberPos.getY(), bobberPos.getZ());
                    double d0 = player.getPosX() - bobberPos.getX();
                    double d1 = player.getPosY() - bobberPos.getY();
                    double d2 = player.getPosZ() - bobberPos.getZ();
                    entity.setMotion(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    player.world.addEntity(entity);
                    player.world.addEntity(new ExperienceOrbEntity(player.world, player.getPosX(), player.getPosY() + 0.5D, player.getPosZ() + 0.5D, player.getRNG().nextInt(6) + 1));
                    if (entity.getItem().getItem().isIn(ItemTags.FISHES)) {
                        player.addStat(Stats.FISH_CAUGHT, 1);
                    }
                }
            }
        }
        CURRENTLY_PLAYING.remove(player.getUniqueID());
    }

    //Client
    public void handleOpenMinigame(NetworkEvent.Context ctx, Vector3d bobberPos, float catchChance) {
    }
}
