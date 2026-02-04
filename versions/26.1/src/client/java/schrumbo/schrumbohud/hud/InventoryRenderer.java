package schrumbo.schrumbohud.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

/**
 * Inventory HUD overlay element
 */
public class InventoryRenderer implements HudElement {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("schrumbomods", "inventory_hud");
    private static final int SLOT_SIZE = 18;
    private static final int ROW_SLOTS = 9;
    private static final int ROWS = 3;
    private static final int PADDING = 4;

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.MISC_OVERLAYS,
                ID,
                new InventoryRenderer()
        );
    }

    @Override
    public void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        SchrumboHudConfig config = SchrumboHUDClient.config;

        if (!config.enabled || client == null || client.player == null) return;

        Inventory inventory = client.player.getInventory();

        int hudWidth = ROW_SLOTS * SLOT_SIZE + PADDING * 2;
        int hudHeight = ROWS * SLOT_SIZE + PADDING * 2;

        float scale = config.scale;
        int scaledWidth = Math.round(hudWidth * scale);
        int scaledHeight = Math.round(hudHeight * scale);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int x = calcX(config, screenWidth, scaledWidth);
        int y = calcY(config, screenHeight, scaledHeight);

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        drawBackground(context, hudWidth, hudHeight, config);
        renderSlotBackgrounds(context, config);

        matrices.popMatrix();

        ScreenRectangle scissor = context.scissorStack.peek();
        Matrix3x2f pose = new Matrix3x2f(context.pose());

        InventoryHudRenderState renderState = new InventoryHudRenderState(
                inventory, config, pose, scissor,
                x, y, x + scaledWidth, y + scaledHeight
        );
        context.guiRenderState.submitPicturesInPictureState(renderState);

        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        renderOverlays(context, inventory, config);

        matrices.popMatrix();
    }

    private int calcX(SchrumboHudConfig config, int screenWidth, int hudWidth) {
        return switch (config.anchor.horizontal) {
            case LEFT -> config.position.x;
            case RIGHT -> screenWidth - hudWidth - config.position.x;
        };
    }

    private int calcY(SchrumboHudConfig config, int screenHeight, int hudHeight) {
        return switch (config.anchor.vertical) {
            case TOP -> config.position.y;
            case BOTTOM -> screenHeight - hudHeight - config.position.y;
        };
    }

    private void drawBackground(GuiGraphics context, int width, int height, SchrumboHudConfig config) {
        if (config.backgroundEnabled) {
            int bgColor = config.colors.background();
            if (config.roundedCorners) {
                RenderUtils.fillRoundedRect(context, 0, 0, width, height, 0.2f, bgColor);
            } else {
                context.fill(0, 0, width, height, bgColor);
            }
        }

        if (config.outlineEnabled) {
            int borderColor = config.colors.border();
            if (config.roundedCorners) {
                RenderUtils.drawRoundedRectWithOutline(context, 0, 0, width, height, 0.2f, 1, borderColor);
            } else {
                RenderUtils.drawBorder(context, 0, 0, width, height, borderColor);
            }
        }
    }

    private void renderSlotBackgrounds(GuiGraphics context, SchrumboHudConfig config) {
        if (!config.slotBackgroundEnabled) return;

        int slotColor = config.colors.slots();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < ROW_SLOTS; col++) {
                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                if (config.roundedCorners) {
                    RenderUtils.drawRectWithCutCorners(context, slotX, slotY, SLOT_SIZE - 2, SLOT_SIZE - 2, 1, slotColor);
                } else {
                    context.fill(slotX, slotY, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2, slotColor);
                }
            }
        }
    }

    private void renderOverlays(GuiGraphics context, Inventory inventory, SchrumboHudConfig config) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < ROW_SLOTS; col++) {
                int slot = 9 + row * ROW_SLOTS + col;
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;

                if (stack.getCount() > 1) {
                    renderStackCount(context, stack, slotX, slotY, config);
                }
                if (stack.isDamaged()) {
                    renderDurabilityBar(context, stack, slotX, slotY);
                }
            }
        }
    }

    private void renderStackCount(GuiGraphics context, ItemStack stack, int x, int y, SchrumboHudConfig config) {
        String count = String.valueOf(stack.getCount());
        var font = Minecraft.getInstance().font;
        context.drawString(font, count,
                x + SLOT_SIZE - 2 - font.width(count),
                y + SLOT_SIZE - 9, config.colors.text(), config.textShadowEnabled);
    }

    private void renderDurabilityBar(GuiGraphics context, ItemStack stack, int x, int y) {
        int maxDurability = stack.getMaxDamage();
        int currDurability = maxDurability - stack.getDamageValue();
        float percentDurability = (float) currDurability / maxDurability;

        int barWidth = SLOT_SIZE - 6;
        int filledWidth = (int) (barWidth * percentDurability);
        int barY = y + SLOT_SIZE - 4;

        context.fill(x + 2, barY, x + 2 + barWidth, barY + 1, 0xFF000000);
        context.fill(x + 2, barY, x + 2 + filledWidth, barY + 1, getDurabilityColor(percentDurability));
    }

    private int getDurabilityColor(float percent) {
        if (percent > 0.5f) return 0xFF00FF00;
        else if (percent > 0.25f) return 0xFFFFFF00;
        else return 0xFFFF0000;
    }
}
