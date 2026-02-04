package schrumbo.schrumbohud.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Off-screen texture renderer for inventory items
 */
public class InventoryHudSpecialRenderer extends PictureInPictureRenderer<InventoryHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int ROW_SLOTS = 9;
    private static final int ROWS = 3;
    private static final int PADDING = 4;

    private final TrackingItemStackRenderState itemRenderState = new TrackingItemStackRenderState();

    public InventoryHudSpecialRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<InventoryHudRenderState> getRenderStateClass() {
        return InventoryHudRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "schrumbohud_inventory";
    }

    @Override
    protected void renderToTexture(InventoryHudRenderState state, PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        Inventory inventory = state.inventory();

        int hudWidth = ROW_SLOTS * SLOT_SIZE + PADDING * 2;
        int hudHeight = ROWS * SLOT_SIZE + PADDING * 2;
        float pps = client.getWindow().getGuiScale() * state.config().scale;
        float texCenterX = Math.round(hudWidth * state.config().scale) * client.getWindow().getGuiScale() / 2.0f;
        float texCenterY = Math.round(hudHeight * state.config().scale) * client.getWindow().getGuiScale() / 2.0f;
        float modelScale = pps * 16.0f;

        matrices.scale(1.0f, -1.0f, -1.0f);

        Lighting lighting = client.gameRenderer.getLighting();

        renderPass(client, inventory, matrices, lighting, pps, texCenterX, texCenterY, modelScale, true);
        bufferSource.endBatch();

        renderPass(client, inventory, matrices, lighting, pps, texCenterX, texCenterY, modelScale, false);
        bufferSource.endBatch();
    }

    /** Renders items matching the given usesBlockLight value under the appropriate lighting. */
    private void renderPass(Minecraft client, Inventory inventory, PoseStack matrices,
                            Lighting lighting, float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean blockLight) {
        lighting.setupFor(blockLight ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < ROW_SLOTS; col++) {
                int slot = 9 + row * ROW_SLOTS + col;
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;

                client.getItemModelResolver().updateForLiving(
                        itemRenderState, stack, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.usesBlockLight() != blockLight) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                float itemCenterX = slotX + 8.0f;
                float itemCenterY = slotY + 8.0f;

                float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
                float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

                matrices.pushPose();
                matrices.translate(snappedX, snappedY, 0.0f);

                renderItemLayers(itemRenderState, matrices);

                matrices.popPose();
            }
        }
    }

    /** Renders all layers of a resolved item, dispatching special renderers and regular quads. */
    private void renderItemLayers(TrackingItemStackRenderState itemState, PoseStack matrices) {
        for (int i = 0; i < itemState.activeLayerCount; i++) {
            var layer = itemState.layers[i];

            if (layer.specialRenderer != null) {
                renderSpecialLayer(layer, itemState, matrices);
            } else if (layer.renderType != null) {
                matrices.pushPose();
                layer.transform.apply(itemState.displayContext.leftHand(), matrices.last());
                ItemRenderer.renderItem(
                        itemState.displayContext,
                        matrices,
                        bufferSource,
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        layer.tintLayers,
                        layer.quads,
                        layer.renderType,
                        layer.foilType
                );
                matrices.popPose();
            }
        }
    }

    /** Renders a layer with a {@link net.minecraft.client.renderer.special.SpecialModelRenderer} (skulls, heads, etc.) via {@link DirectRenderCollector}. */
    @SuppressWarnings("unchecked")
    private void renderSpecialLayer(ItemStackRenderState.LayerRenderState layer,
                                     TrackingItemStackRenderState itemState, PoseStack matrices) {
        matrices.pushPose();
        layer.transform.apply(itemState.displayContext.leftHand(), matrices.last());

        var renderer = (net.minecraft.client.renderer.special.SpecialModelRenderer<Object>) layer.specialRenderer;
        Object arg = layer.argumentForSpecialRendering;

        renderer.submit(
                arg,
                itemState.displayContext,
                matrices,
                new DirectRenderCollector(matrices, bufferSource),
                15728880,
                OverlayTexture.NO_OVERLAY,
                layer.foilType != ItemStackRenderState.FoilType.NONE,
                0
        );

        matrices.popPose();
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
