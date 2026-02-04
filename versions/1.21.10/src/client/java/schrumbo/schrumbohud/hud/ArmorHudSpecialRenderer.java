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
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Off-screen texture renderer for armor items
 */
public class ArmorHudSpecialRenderer extends SpecialGuiElementRenderer<ArmorHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private final KeyedItemRenderState itemRenderState = new KeyedItemRenderState();

    public ArmorHudSpecialRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public Class<ArmorHudRenderState> getElementClass() {
        return ArmorHudRenderState.class;
    }

    @Override
    protected String getName() {
        return "schrumbohud_armor";
    }

    @Override
    protected void render(ArmorHudRenderState state, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        List<ItemStack> armorStacks = state.armorStacks();
        int rows = state.rows();
        int rowSlots = state.rowSlots();

        int hudWidth = rowSlots * SLOT_SIZE + PADDING * 2;
        int hudHeight = rows * SLOT_SIZE + PADDING * 2;
        float hudCenterX = hudWidth / 2.0f;
        float hudCenterY = hudHeight / 2.0f;

        matrices.scale(1.0f, -1.0f, -1.0f);

        DiffuseLighting lighting = client.gameRenderer.getDiffuseLighting();
        RenderDispatcher dispatcher = client.gameRenderer.getEntityRenderDispatcher();
        var queue = dispatcher.getQueue();

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < rowSlots; col++) {
                ItemStack stack = armorStacks.get(index);
                index++;

                if (stack.isEmpty()) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;

                client.getItemModelManager().updateForLivingEntity(
                        itemRenderState, stack, ItemDisplayContext.GUI,
                        client.player
                );

                if (itemRenderState.isSideLit()) {
                    lighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
                } else {
                    lighting.setShaderLights(DiffuseLighting.Type.ITEMS_3D);
                }

                float itemCenterX = slotX + 8.0f;
                float itemCenterY = slotY + 8.0f;

                matrices.push();
                matrices.translate(
                        (itemCenterX - hudCenterX) / 16.0f,
                        (hudCenterY - itemCenterY) / 16.0f,
                        0.0f
                );

                renderItemLayers(itemRenderState, matrices, queue);

                matrices.pop();
            }
        }

        dispatcher.render();
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
    protected boolean shouldBypassScaling(ArmorHudRenderState state) {
        return false;
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return height / 2.0f;
    }
}
