package github.pitbox46.fishingoverhaul.network;

import net.minecraft.util.math.vector.Vector3d;

public class MinigamePacket {
    public final float catchChance;
    public final Vector3d bobberPos;
    public MinigamePacket(float catchChance, Vector3d bobberPos) {
        this.catchChance = catchChance;
        this.bobberPos = bobberPos;
    }
}
