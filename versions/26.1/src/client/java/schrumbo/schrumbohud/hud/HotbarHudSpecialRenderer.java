package schrumbo.schrumbohud.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Off-screen texture renderer for hotbar items
 */
public class HotbarHudSpecialRenderer extends PictureInPictureRenderer<HotbarHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private final TrackingItemStackRenderState itemRenderState = new TrackingItemStackRenderState();

    public HotbarHudSpecialRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<HotbarHudRenderState> getRenderStateClass() {
        return HotbarHudRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "schrumbohud_hotbar";
    }

    @Override
    protected void renderToTexture(HotbarHudRenderState state, PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        Inventory inventory = state.inventory();
        int rows = state.rows();
        int cols = state.rowSlots();

        int totalWidth = state.x1() - state.x0();
        int totalHeight = state.y1() - state.y0();
        float scale = state.config().hotbarScale;
        int unscaledW = Math.round(totalWidth / scale);
        int unscaledH = Math.round(totalHeight / scale);

        float pps = client.getWindow().getGuiScale() * scale;
        float texCenterX = Math.round(unscaledW * scale) * client.getWindow().getGuiScale() / 2.0f;
        float texCenterY = Math.round(unscaledH * scale) * client.getWindow().getGuiScale() / 2.0f;
        float modelScale = pps * 16.0f;

        matrices.scale(1.0f, -1.0f, -1.0f);

        Lighting lighting = client.gameRenderer.getLighting();

        renderPass(client, state, rows, cols, matrices, lighting, pps, texCenterX, texCenterY, modelScale, true);
        bufferSource.endBatch();

        renderPass(client, state, rows, cols, matrices, lighting, pps, texCenterX, texCenterY, modelScale, false);
        bufferSource.endBatch();
    }

    private void renderPass(Minecraft client, HotbarHudRenderState state, int rows, int cols,
                            PoseStack matrices, Lighting lighting,
                            float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean blockLight) {
        lighting.setupFor(blockLight ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

        Inventory inventory = state.inventory();
        int mainOffX = state.mainOffsetX();
        int mainOffY = state.mainOffsetY();

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStack stack = inventory.getItem(index);
                index++;
                if (stack.isEmpty()) continue;

                client.getItemModelResolver().updateForLiving(
                        itemRenderState, stack, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.usesBlockLight() != blockLight) continue;

                int slotX = mainOffX + PADDING + col * SLOT_SIZE + 1;
                int slotY = mainOffY + PADDING + row * SLOT_SIZE + 1;
                renderItemAt(matrices, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
            }
        }

        if (state.showOffhand()) {
            ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
            if (!offhand.isEmpty()) {
                client.getItemModelResolver().updateForLiving(
                        itemRenderState, offhand, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.usesBlockLight() == blockLight) {
                    int slotX = state.offhandOffsetX() + PADDING + 1;
                    int slotY = state.offhandOffsetY() + PADDING + 1;
                    renderItemAt(matrices, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
                }
            }
        }
    }

    private void renderItemAt(PoseStack matrices, float pps, float texCenterX, float texCenterY,
                              float modelScale, int slotX, int slotY) {
        float itemCenterX = slotX + 8.0f;
        float itemCenterY = slotY + 8.0f;

        float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
        float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

        matrices.pushPose();
        matrices.translate(snappedX, snappedY, 0.0f);

        itemRenderState.submit(matrices, new DirectRenderCollector(matrices, bufferSource),
                15728880, OverlayTexture.NO_OVERLAY, 0);

        matrices.popPose();
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
