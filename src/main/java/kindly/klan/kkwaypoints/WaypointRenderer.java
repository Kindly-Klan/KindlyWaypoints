package kindly.klan.kkwaypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = KindlyWaypoints.MODID, value = Dist.CLIENT)
public class WaypointRenderer {

    private static final ResourceLocation RED_POINT = new ResourceLocation("kindlywaypoints", "textures/hud/red_point.png");
    private static final ResourceLocation BLUE_POINT = new ResourceLocation("kindlywaypoints", "textures/hud/blue_point.png");
    private static final int ICON_SIZE = 16;
    // Use a higher constant scale so the icon and text remain clearly visible regardless of distance.
    private static final float CONSTANT_SCALE = 1f;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        Font font = mc.font;

        WaypointManager.getWaypoints().stream()
                .filter(Waypoint::isVisible)
                .forEach(waypoint -> renderWaypoint(poseStack, bufferSource, font, player, waypoint));

        bufferSource.endBatch();
    }

    private static void renderWaypoint(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Font font, Player player, Waypoint waypoint) {
        Vec3 playerPos = player.position();
        Vec3 waypointPos = new Vec3(waypoint.getX(), waypoint.getY(), waypoint.getZ());
        double distance = playerPos.distanceTo(waypointPos);
        if (distance > 10000) return;

        // Draw the waypoint with constant size.
        poseStack.pushPose();
        // Translate relative to the player's position.
        poseStack.translate(
            waypoint.getX() - playerPos.x,
            waypoint.getY() - playerPos.y,
            waypoint.getZ() - playerPos.z
        );

        // Orient the waypoint to always face the camera.
        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        poseStack.mulPose(renderDispatcher.cameraOrientation());
        // Apply a constant scale so size does not vary with distance.
        poseStack.scale(CONSTANT_SCALE, CONSTANT_SCALE, CONSTANT_SCALE);

        ResourceLocation texture = waypoint.getTexture().equals("red_point") ? RED_POINT : BLUE_POINT;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.textSeeThrough(texture));
        Matrix4f currentPose = poseStack.last().pose();
        float halfSize = ICON_SIZE / 2f;

        // Draw the icon quad.
        buffer.vertex(currentPose, -halfSize, -halfSize, 0)
              .color(1.0f, 1.0f, 1.0f, 1.0f)
              .uv(0, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(15728880)
              .endVertex();
        buffer.vertex(currentPose, -halfSize, halfSize, 0)
              .color(1.0f, 1.0f, 1.0f, 1.0f)
              .uv(0, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(15728880)
              .endVertex();
        buffer.vertex(currentPose, halfSize, halfSize, 0)
              .color(1.0f, 1.0f, 1.0f, 1.0f)
              .uv(1, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(15728880)
              .endVertex();
        buffer.vertex(currentPose, halfSize, -halfSize, 0)
              .color(1.0f, 1.0f, 1.0f, 1.0f)
              .uv(1, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(15728880)
              .endVertex();

        // Render text with constant size.
        poseStack.pushPose();
        // Cancel out the Y inversion so text is drawn upright.
        poseStack.scale(1, -1, 1);
        // Position text below the icon.
        poseStack.translate(0, - (halfSize + 2), 0);

        String name = waypoint.getName();
        String distanceText = String.format("%.1fm", distance);

        // Draw the waypoint name.
        float nameX = -font.width(name) / 2f;
        // Disable depth test so text is always visible.
        RenderSystem.disableDepthTest();
        font.drawInBatch(
            name,
            nameX,
            0,
            0xFFFFFF,
            true,
            poseStack.last().pose(),
            bufferSource,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
        // Only draw distance text if distance > 30 meters.
        if (distance > 30) {
            float distX = -font.width(distanceText) / 2f;
            font.drawInBatch(
                distanceText,
                distX,
                font.lineHeight,
                0xFFFFFF,
                true,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                15728880
            );
        }
        RenderSystem.enableDepthTest();

        poseStack.popPose(); // End text transformation.
        poseStack.popPose(); // End overall waypoint transformation.
    }
}