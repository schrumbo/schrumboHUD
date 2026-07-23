package schrumbo.schrumbohud.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Off-screen texture renderer for hotbar items
 */
public class HotbarHudSpecialRenderer extends PictureInPictureRenderer<HotbarHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private final List<ItemStackRenderState> renderStatePool = new ArrayList<>();
    private ItemStackRenderState offhandRenderState;

    public HotbarHudSpecialRenderer() {
        super();
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
    protected void renderToTexture(HotbarHudRenderState state, PoseStack matrices, SubmitNodeCollector collector) {
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

        int totalSlots = rows * cols;
        ensurePoolSize(totalSlots);
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            ItemStackRenderState rs = renderStatePool.get(i);
            rs.clear();
            if (!stack.isEmpty()) {
                client.getItemModelResolver().updateForLiving(rs, stack, ItemDisplayContext.GUI, client.player);
            }
        }

        if (offhandRenderState == null) {
            offhandRenderState = new ItemStackRenderState();
        }
        offhandRenderState.clear();
        if (state.showOffhand()) {
            ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
            if (!offhand.isEmpty()) {
                client.getItemModelResolver().updateForLiving(offhandRenderState, offhand, ItemDisplayContext.GUI, client.player);
            }
        }

        matrices.scale(1.0f, -1.0f, -1.0f);

        Lighting lighting = client.gameRenderer.lighting();

        renderPass(state, rows, cols, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, true);
        renderPass(state, rows, cols, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, false);
    }

    private void renderPass(HotbarHudRenderState state, int rows, int cols,
                            PoseStack matrices, Lighting lighting, SubmitNodeCollector collector,
                            float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean blockLight) {
        lighting.setupFor(blockLight ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

        int mainOffX = state.mainOffsetX();
        int mainOffY = state.mainOffsetY();

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStackRenderState rs = renderStatePool.get(index);
                index++;
                if (rs.isEmpty()) continue;
                if (rs.usesBlockLight() != blockLight) continue;

                int slotX = mainOffX + PADDING + col * SLOT_SIZE + 1;
                int slotY = mainOffY + PADDING + row * SLOT_SIZE + 1;
                renderItemAt(rs, matrices, collector, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
            }
        }

        if (state.showOffhand() && !offhandRenderState.isEmpty() && offhandRenderState.usesBlockLight() == blockLight) {
            int slotX = state.offhandOffsetX() + PADDING + 1;
            int slotY = state.offhandOffsetY() + PADDING + 1;
            renderItemAt(offhandRenderState, matrices, collector, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
        }
    }

    private void renderItemAt(ItemStackRenderState rs, PoseStack matrices, SubmitNodeCollector collector,
                              float pps, float texCenterX, float texCenterY,
                              float modelScale, int slotX, int slotY) {
        float itemCenterX = slotX + 8.0f;
        float itemCenterY = slotY + 8.0f;

        float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
        float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

        matrices.pushPose();
        matrices.translate(snappedX, snappedY, 0.0f);

        rs.submit(matrices, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

        matrices.popPose();
    }

    private void ensurePoolSize(int size) {
        while (renderStatePool.size() < size) {
            renderStatePool.add(new ItemStackRenderState());
        }
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
