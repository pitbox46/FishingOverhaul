package github.pitbox46.fishingoverhaul.network;

import net.minecraft.world.phys.Vec3;

public record MinigamePacket(float catchChance, Vec3 bobberPos) {
}
