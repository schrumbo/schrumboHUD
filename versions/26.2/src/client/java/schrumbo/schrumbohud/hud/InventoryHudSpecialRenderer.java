package schrumbo.schrumbohud.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

    public InventoryHudSpecialRenderer() {
        super();
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
    protected void renderToTexture(InventoryHudRenderState state, PoseStack matrices, SubmitNodeCollector collector) {
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

        Lighting lighting = client.gameRenderer.lighting();

        renderPass(client, inventory, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, true);
        renderPass(client, inventory, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, false);
    }

    private void renderPass(Minecraft client, Inventory inventory, PoseStack matrices,
                            Lighting lighting, SubmitNodeCollector collector,
                            float pps, float texCenterX, float texCenterY,
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

                itemRenderState.submit(matrices, collector,
                        15728880, OverlayTexture.NO_OVERLAY, 0);

                matrices.popPose();
            }
        }
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
