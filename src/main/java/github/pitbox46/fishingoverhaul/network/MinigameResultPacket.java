package github.pitbox46.fishingoverhaul.network;

public record MinigameResultPacket(Result result) {
    public enum Result {
        FAIL,
        SUCCESS,
        CRIT
    }
}
