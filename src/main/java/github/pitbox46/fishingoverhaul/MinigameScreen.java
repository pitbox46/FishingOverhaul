package github.pitbox46.fishingoverhaul;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import github.pitbox46.fishingoverhaul.network.MinigameResultPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class MinigameScreen extends Screen {
    public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("fishingoverhaul", "textures/minigame.png");
    protected final float FISH_SPEED = 5;

    protected final float catchChance;
    protected final float critChance;
    protected float fishDeg = 0;
    protected final float maxFishSpeed;
    protected float fishSpeed;
    protected long tickCounter = 0;
    protected float previousFrame = 0;
    protected float partialTickCounter = 360;
    public MinigameScreen(Component titleIn, float catchChance, float critChance, float speedMulti) {
        super(titleIn);
        this.catchChance = catchChance;
        this.critChance = catchChance * critChance;
        this.fishSpeed = this.maxFishSpeed = FISH_SPEED * speedMulti;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        float ticksSinceLastFrame = tickCounter + partialTicks - previousFrame;

        renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        blitCircle(pGuiGraphics, this.width / 2, this.height / 2, 4, 90, partialTickCounter, 0, 0, 167);
        blitCircle(pGuiGraphics, this.width / 2, this.height / 2, 4, 0, 360, 172, 0, 151);
        blitCircle(pGuiGraphics, this.width / 2 - 1, this.height / 2 - 1, 6, normalizeDegrees(90 - 180 * catchChance), 360 * catchChance, 356, 0, 151);
        blitCircle(pGuiGraphics, this.width / 2 - 1, this.height / 2 - 1, 6, normalizeDegrees(270 - 180 * catchChance), 360 * catchChance, 356, 0, 151);
        blitCircle(pGuiGraphics, this.width / 2 - 1, this.height / 2 - 1, 6, normalizeDegrees(90 - 180 * critChance), 360 * critChance, 356, 156, 151);
        blitCircle(pGuiGraphics, this.width / 2 - 1, this.height / 2 - 1, 6, normalizeDegrees(270 - 180 * critChance), 360 * critChance, 356, 156, 151);
        drawFish(pGuiGraphics, (this.width / 2) + 2, (this.height / 2) + 2, 73f);
        fishDeg += fishSpeed * partialTicks;

        previousFrame = tickCounter + partialTicks;

        if((partialTickCounter -= ticksSinceLastFrame * 2) < 0) {
            partialTickCounter = 360;
            PacketDistributor.sendToServer(new MinigameResultPacket(MinigameResultPacket.Result.FAIL));
            onClose();
        }

        renderables.forEach(renderable -> renderable.render(pGuiGraphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void tick() {
        if(tickCounter++ % 20 == 0) {
            if (getMinecraft().player != null)
                fishSpeed = getMinecraft().player.getRandom().nextBoolean() ? maxFishSpeed * 3 : fishSpeed;
            else
                fishSpeed = Math.random() > 0.5 ? maxFishSpeed * 2: fishSpeed;
        }
        if(fishSpeed > maxFishSpeed) {
            fishSpeed -= maxFishSpeed / 20f;
        } else {
            fishSpeed = maxFishSpeed;
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float cappedFishDeg = normalizeDegrees(fishDeg);
        if (isFishCaught(cappedFishDeg, critChance, 270) || isFishCaught(cappedFishDeg, critChance, 90)) {
            PacketDistributor.sendToServer(new MinigameResultPacket(MinigameResultPacket.Result.CRIT));
            onClose();
            return true;
        }
        else if(isFishCaught(cappedFishDeg, catchChance, 270) || isFishCaught(cappedFishDeg, catchChance, 90)) {
            PacketDistributor.sendToServer(new MinigameResultPacket(MinigameResultPacket.Result.SUCCESS));
            onClose();
            return true;
        } else {
            partialTickCounter -= 36;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawFish(GuiGraphics guiGraphics, int centerX, int centerY, float radius) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        double x = radius * Math.cos(-(fishDeg / 180d) * Math.PI);
        double y = radius * Math.sin(-(fishDeg / 180d) * Math.PI);
        poseStack.translate(centerX + x, centerY + y, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90 - fishDeg));
        guiGraphics.blit(TEX, -2, -5, 326, fishSpeed < 0 ? 0 : 6, 11, 6, 512, 512);
        poseStack.popPose();
    }

    private float normalizeDegrees(float degreesIn) {
        return degreesIn % 360 >= 0 ? degreesIn % 360 : (degreesIn % 360) + 360;
    }

    private boolean isInRange(float degreesIn, float lower, float upper) {
        return ((lower <= upper && degreesIn >= lower && degreesIn <= upper) || (lower > upper && !(degreesIn <= lower && degreesIn >= upper)));
    }

    protected boolean isFishCaught(float cappedFishDeg, float catchChance, float offset) {
        return isInRange(
                cappedFishDeg,
                normalizeDegrees(offset - 180 * catchChance),
                normalizeDegrees(offset + 180 * catchChance)
        );
    }

    private void blitCircle(GuiGraphics guiGraphics, int centerX, int centerY, int stroke, float degreesStart, float degreesForward, int uOffset, int vOffset, int diameter) {
        int radius = diameter / 2;
        int textureCenterX = uOffset + radius;
        int textureCenterY = vOffset + radius;

        int x, y;
        for(float i = degreesStart; i < degreesStart + degreesForward; i++) {
            x = (int) Math.round(radius * Math.cos(-(i / 180d) * Math.PI));
            y = (int) Math.round(radius * Math.sin(-(i / 180d) * Math.PI));
            guiGraphics.blit(TEX, x + centerX, y + centerY, x + textureCenterX, y + textureCenterY, stroke, stroke, 512, 512);
        }
    }
}
