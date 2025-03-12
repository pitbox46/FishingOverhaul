package github.pitbox46.fishingoverhaul.duck;

import github.pitbox46.fishingoverhaul.network.MinigameResultPacket;

public interface FishingHookDuck {
    void fishingOverhaul$completeGame(MinigameResultPacket.Result result);
}
