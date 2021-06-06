package github.pitbox46.fishingoverhaul;

public class FishIndex {
    private final float catchChance;
    private final float variability;
    public FishIndex(float catchChance, float variability) {
        this.catchChance = catchChance;
        this.variability = variability;
    }

    public float getCatchChance() {
        return catchChance;
    }

    public float getVariability() {
        return variability;
    }
}
