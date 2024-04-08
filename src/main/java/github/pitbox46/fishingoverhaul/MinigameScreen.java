package github.pitbox46.fishingoverhaul;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import github.pitbox46.fishingoverhaul.network.MinigameResultPacket;
import github.pitbox46.fishingoverhaul.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinigameScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation TEX = new ResourceLocation("fishingoverhaul", "textures/minigame.png");
    private final Vec3 bobberPos;
    private final float catchChance;
    private float fishDeg = 0;
    private static final int FISH_SPEED = 5;
    private float fishSpeed = FISH_SPEED;
    private long tickCounter = 0;
    private float previousFrame = 0;
    private float partialTickCounter = 360;
    public MinigameScreen(Component titleIn, Vec3 bobberPos, float catchChance) {
        super(titleIn);
        this.catchChance = catchChance;
        this.bobberPos = bobberPos;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        float ticksSinceLastFrame = tickCounter + partialTicks - previousFrame;

        renderBackground(pGuiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        blitCircle(pGuiGraphics, this.width / 2, this.height / 2, 4, 90, partialTickCounter, 0, 0, 167);
        blitCircle(pGuiGraphics, this.width / 2, this.height / 2, 4, 0, 360, 172, 0, 151);
        blitCircle(pGuiGraphics, this.width / 2 - 1, this.height / 2 - 1, 6, normalizeDegrees(270 - 180 * catchChance), 360 * catchChance, 356, 0, 151);
        drawFish(pGuiGraphics, (this.width / 2) + 2, (this.height / 2) + 2, 73f);
        fishDeg += fishSpeed * partialTicks;

        previousFrame = tickCounter + partialTicks;

        if((partialTickCounter -= ticksSinceLastFrame * 2) < 0) {
            partialTickCounter = 360;
            PacketHandler.CHANNEL.sendToServer(new MinigameResultPacket(false, bobberPos));
            onClose();
        }
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        if(tickCounter++ % 20 == 0) {
            if (getMinecraft().player != null)
                fishSpeed = getMinecraft().player.getRandom().nextBoolean() ? fishSpeed = FISH_SPEED * 3: fishSpeed;
            else
                fishSpeed = Math.random() > 0.5 ? FISH_SPEED * 3: fishSpeed;
        }
        if(fishSpeed > FISH_SPEED) {
            fishSpeed -= FISH_SPEED / 20f;
        } else {
            fishSpeed = FISH_SPEED;
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float cappedFishDeg = normalizeDegrees(fishDeg);
        if(isInRange(cappedFishDeg, normalizeDegrees(270 - 180 * catchChance), normalizeDegrees(270 + 180 * catchChance))) {
            PacketHandler.CHANNEL.sendToServer(new MinigameResultPacket(true, bobberPos));
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
