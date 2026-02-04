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
 * Off-screen texture renderer for inventory items
 */
public class InventoryHudSpecialRenderer extends SpecialGuiElementRenderer<InventoryHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int ROW_SLOTS = 9;
    private static final int ROWS = 3;
    private static final int PADDING = 4;

    private final KeyedItemRenderState itemRenderState = new KeyedItemRenderState();

    public InventoryHudSpecialRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public Class<InventoryHudRenderState> getElementClass() {
        return InventoryHudRenderState.class;
    }

    @Override
    protected String getName() {
        return "schrumbohud_inventory";
    }

    @Override
    protected void render(InventoryHudRenderState state, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        PlayerInventory inventory = state.inventory();

        int hudWidth = ROW_SLOTS * SLOT_SIZE + PADDING * 2;
        int hudHeight = ROWS * SLOT_SIZE + PADDING * 2;
        float pps = client.getWindow().getScaleFactor() * state.config().scale;
        float texCenterX = Math.round(hudWidth * state.config().scale) * client.getWindow().getScaleFactor() / 2.0f;
        float texCenterY = Math.round(hudHeight * state.config().scale) * client.getWindow().getScaleFactor() / 2.0f;
        float modelScale = pps * 16.0f;

        matrices.scale(1.0f, -1.0f, -1.0f);

        DiffuseLighting lighting = client.gameRenderer.getDiffuseLighting();
        RenderDispatcher dispatcher = client.gameRenderer.getEntityRenderDispatcher();
        var queue = dispatcher.getQueue();

        renderPass(client, inventory, matrices, lighting, queue, pps, texCenterX, texCenterY, modelScale, true);
        this.vertexConsumers.draw();

        renderPass(client, inventory, matrices, lighting, queue, pps, texCenterX, texCenterY, modelScale, false);
        this.vertexConsumers.draw();

        dispatcher.render();
    }

    /** Renders items matching the given sideLit value under the appropriate lighting. */
    private void renderPass(MinecraftClient client, PlayerInventory inventory, MatrixStack matrices,
                            DiffuseLighting lighting, net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
                            float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean sideLit) {
        lighting.setShaderLights(sideLit ? DiffuseLighting.Type.ITEMS_3D : DiffuseLighting.Type.ITEMS_FLAT);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < ROW_SLOTS; col++) {
                int slot = 9 + row * ROW_SLOTS + col;
                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty()) continue;

                client.getItemModelManager().updateForLivingEntity(
                        itemRenderState, stack, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.isSideLit() != sideLit) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                float itemCenterX = slotX + 8.0f;
                float itemCenterY = slotY + 8.0f;

                float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
                float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

                matrices.push();
                matrices.translate(snappedX, snappedY, 0.0f);

                renderItemLayers(itemRenderState, matrices, queue);

                matrices.pop();
            }
        }
    }

    /**
     * Per-layer item rendering — regular layers via vertex consumers, special models via dispatcher
     */
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
    protected boolean shouldBypassScaling(InventoryHudRenderState state) {
        return false;
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
