package schrumbo.schrumbohud.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Off-screen texture renderer for armor items
 */
public class ArmorHudSpecialRenderer extends PictureInPictureRenderer<ArmorHudRenderState> {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private final List<ItemStackRenderState> renderStatePool = new ArrayList<>();

    public ArmorHudSpecialRenderer() {
        super();
    }

    @Override
    public Class<ArmorHudRenderState> getRenderStateClass() {
        return ArmorHudRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "schrumbohud_armor";
    }

    @Override
    protected void renderToTexture(ArmorHudRenderState state, PoseStack matrices, SubmitNodeCollector collector) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        List<ItemStack> armorStacks = state.armorStacks();
        int rows = state.rows();
        int rowSlots = state.rowSlots();

        int hudWidth = rowSlots * SLOT_SIZE + PADDING * 2;
        int hudHeight = rows * SLOT_SIZE + PADDING * 2;
        float pps = client.getWindow().getGuiScale() * state.config().armorScale;
        float texCenterX = Math.round(hudWidth * state.config().armorScale) * client.getWindow().getGuiScale() / 2.0f;
        float texCenterY = Math.round(hudHeight * state.config().armorScale) * client.getWindow().getGuiScale() / 2.0f;
        float modelScale = pps * 16.0f;

        int totalSlots = rows * rowSlots;
        ensurePoolSize(totalSlots);
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = armorStacks.get(i);
            ItemStackRenderState rs = renderStatePool.get(i);
            rs.clear();
            if (!stack.isEmpty()) {
                client.getItemModelResolver().updateForLiving(rs, stack, ItemDisplayContext.GUI, client.player);
            }
        }

        matrices.scale(1.0f, -1.0f, -1.0f);

        Lighting lighting = client.gameRenderer.lighting();

        renderPass(rows, rowSlots, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, true);
        renderPass(rows, rowSlots, matrices, lighting, collector, pps, texCenterX, texCenterY, modelScale, false);
    }

    private void renderPass(int rows, int rowSlots,
                            PoseStack matrices, Lighting lighting, SubmitNodeCollector collector,
                            float pps, float texCenterX, float texCenterY,
                            float modelScale, boolean blockLight) {
        lighting.setupFor(blockLight ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < rowSlots; col++) {
                ItemStackRenderState rs = renderStatePool.get(index);
                index++;

                if (rs.isEmpty()) continue;
                if (rs.usesBlockLight() != blockLight) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                float itemCenterX = slotX + 8.0f;
                float itemCenterY = slotY + 8.0f;

                float snappedX = (Math.round(itemCenterX * pps) - texCenterX) / modelScale;
                float snappedY = (texCenterY - Math.round(itemCenterY * pps)) / modelScale;

                matrices.pushPose();
                matrices.translate(snappedX, snappedY, 0.0f);

                rs.submit(matrices, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

                matrices.popPose();
            }
        }
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
