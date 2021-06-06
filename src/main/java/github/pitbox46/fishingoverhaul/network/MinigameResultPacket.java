package github.pitbox46.fishingoverhaul.network;

import net.minecraft.util.math.vector.Vector3d;

public class MinigameResultPacket {
    public final Vector3d bobberPos;
    public final boolean success;
    public MinigameResultPacket(boolean success, Vector3d bobberPos) {
        this.bobberPos = bobberPos;
        this.success = success;
    }
}
