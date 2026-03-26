package schrumbo.schrumbohud.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

/**
 * Off-screen texture renderer for hotbar items
 */
public class HotbarHudSpecialRenderer extends SpecialGuiElementRenderer<HotbarHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private final KeyedItemRenderState itemRenderState = new KeyedItemRenderState();

    public HotbarHudSpecialRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public Class<HotbarHudRenderState> getElementClass() {
        return HotbarHudRenderState.class;
    }

    @Override
    protected String getName() {
        return "schrumbohud_hotbar";
    }

    @Override
    protected void render(HotbarHudRenderState state, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        PlayerInventory inventory = state.inventory();
        int rows = state.rows();
        int cols = state.rowSlots();

        int totalWidth = state.x2() - state.x1();
        int totalHeight = state.y2() - state.y1();
        float scale = state.config().hotbarScale;
        int unscaledW = Math.round(totalWidth / scale);
        int unscaledH = Math.round(totalHeight / scale);

        float pps = client.getWindow().getScaleFactor() * scale;
        float texCenterX = Math.round(unscaledW * scale) * client.getWindow().getScaleFactor() / 2.0f;
        float texCenterY = Math.round(unscaledH * scale) * client.getWindow().getScaleFactor() / 2.0f;
        float modelScale = pps * 16.0f;

        matrices.scale(1.0f, -1.0f, -1.0f);

        DiffuseLighting lighting = client.gameRenderer.getDiffuseLighting();
        RenderDispatcher dispatcher = client.gameRenderer.getEntityRenderDispatcher();
        var queue = dispatcher.getQueue();

        renderPass(client, state, rows, cols, matrices, lighting, queue, pps, texCenterX, texCenterY, modelScale, true);
        this.vertexConsumers.draw();

        renderPass(client, state, rows, cols, matrices, lighting, queue, pps, texCenterX, texCenterY, modelScale, false);
        this.vertexConsumers.draw();

        dispatcher.render();
    }

    private void renderPass(MinecraftClient client, HotbarHudRenderState state, int rows, int cols,
                            MatrixStack matrices, DiffuseLighting lighting,
                            net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
                            float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean sideLit) {
        lighting.setShaderLights(sideLit ? DiffuseLighting.Type.ITEMS_3D : DiffuseLighting.Type.ITEMS_FLAT);

        PlayerInventory inventory = state.inventory();
        int mainOffX = state.mainOffsetX();
        int mainOffY = state.mainOffsetY();

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStack stack = inventory.getStack(index);
                index++;
                if (stack.isEmpty()) continue;

                client.getItemModelManager().updateForLivingEntity(
                        itemRenderState, stack, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.isSideLit() != sideLit) continue;

                int slotX = mainOffX + PADDING + col * SLOT_SIZE + 1;
                int slotY = mainOffY + PADDING + row * SLOT_SIZE + 1;
                renderItemAt(matrices, queue, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
            }
        }

        if (state.showOffhand()) {
            ItemStack offhand = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
            if (!offhand.isEmpty()) {
                client.getItemModelManager().updateForLivingEntity(
                        itemRenderState, offhand, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.isSideLit() == sideLit) {
                    int slotX = state.offhandOffsetX() + PADDING + 1;
                    int slotY = state.offhandOffsetY() + PADDING + 1;
                    renderItemAt(matrices, queue, pps, texCenterX, texCenterY, modelScale, slotX, slotY);
                }
            }
        }
    }

    private void renderItemAt(MatrixStack matrices, net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
                              float pps, float texCenterX, float texCenterY, float modelScale,
                              int slotX, int slotY) {
        float itemCenterX = slotX + 8.0f;
        float itemCenterY = slotY + 8.0f;

        float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
        float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

        matrices.push();
        matrices.translate(snappedX, snappedY, 0.0f);
        renderItemLayers(itemRenderState, matrices, queue);
        matrices.pop();
    }

    private void renderItemLayers(KeyedItemRenderState itemState, MatrixStack matrices,
                                   net.minecraft.client.render.command.OrderedRenderCommandQueue queue) {
        for (int i = 0; i < itemState.layerCount; i++) {
            ItemRenderState.LayerRenderState layer = itemState.layers[i];

            if (layer.specialModelType != null) {
                layer.render(matrices, queue, 15728880, OverlayTexture.DEFAULT_UV, 0);
            } else if (layer.renderLayer != null) {
                matrices.push();
                layer.transform.apply(itemState.displayContext.isLeftHand(), matrices.peek());
                ItemRenderer.renderItem(
                        itemState.displayContext,
                        matrices,
                        this.vertexConsumers,
                        15728880,
                        OverlayTexture.DEFAULT_UV,
                        layer.tints,
                        layer.getQuads(),
                        layer.renderLayer,
                        layer.glint
                );
                matrices.pop();
            }
        }
    }

    @Override
    protected boolean shouldBypassScaling(HotbarHudRenderState state) {
        return false;
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
