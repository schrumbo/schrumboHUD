package schrumbo.schrumbohud.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3x2f;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

/**
 * Custom hotbar HUD — wraps vanilla hotbar, delegates when disabled
 */
public class HotbarRenderer implements HudElement {

    private static final int SLOT_SIZE = 18;
    private static final int HOTBAR_SLOTS = 9;
    private static final int PADDING = 4;
    private static final int OFFHAND_GAP = 4;
    private static final int OFFHAND_PANEL = SLOT_SIZE + PADDING * 2;

    private final HudElement original;

    public HotbarRenderer(HudElement original) {
        this.original = original;
    }

    public static void register() {
        HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, original -> new HotbarRenderer(original));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        SchrumboHudConfig config = SchrumboHUDClient.config;

        if (!config.hotbar.hotbarEnabled || client == null || client.player == null || client.player.isSpectator()) {
            original.extractRenderState(context, tickCounter);
            return;
        }

        boolean vertical = config.hotbar.hotbarVertical;
        int rows = vertical ? HOTBAR_SLOTS : 1;
        int cols = vertical ? 1 : HOTBAR_SLOTS;
        int mainW = cols * SLOT_SIZE + PADDING * 2;
        int mainH = rows * SLOT_SIZE + PADDING * 2;

        Inventory inventory = client.player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        boolean showOffhand = config.hotbar.hotbarShowOffhand;
        boolean offhandOnLeft = client.player.getMainArm() == HumanoidArm.RIGHT;

        int totalW, totalH, mainOffX, mainOffY, ohOffX, ohOffY;
        if (vertical) {
            totalW = showOffhand ? mainW : mainW;
            totalH = showOffhand ? mainH + OFFHAND_GAP + OFFHAND_PANEL : mainH;
            mainOffX = 0;
            mainOffY = 0;
            ohOffX = 0;
            ohOffY = mainH + OFFHAND_GAP;
        } else {
            totalW = showOffhand ? mainW + OFFHAND_GAP + OFFHAND_PANEL : mainW;
            totalH = mainH;
            mainOffX = (showOffhand && offhandOnLeft) ? OFFHAND_PANEL + OFFHAND_GAP : 0;
            mainOffY = 0;
            ohOffX = offhandOnLeft ? 0 : mainW + OFFHAND_GAP;
            ohOffY = 0;
        }

        float scale = config.hotbarScale;
        int scaledW = Math.round(totalW * scale);
        int scaledH = Math.round(totalH * scale);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (config.hotbarPosition.x < 0) {
            config.hotbarAnchor.horizontal = SchrumboHudConfig.HorizontalAnchor.LEFT;
            config.hotbarAnchor.vertical = SchrumboHudConfig.VerticalAnchor.BOTTOM;
            config.hotbarPosition.x = (screenWidth - scaledW) / 2;
            config.hotbarPosition.y = 2;
            config.save();
        }

        int x = calcX(config, screenWidth, scaledW);
        int y = calcY(config, screenHeight, scaledH);

        var matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) matrices.scale(scale, scale);

        drawPanel(context, mainOffX, mainOffY, mainW, mainH, config);
        if (showOffhand) drawPanel(context, ohOffX, ohOffY, OFFHAND_PANEL, OFFHAND_PANEL, config);
        renderSlotBackgrounds(context, rows, cols, mainOffX, mainOffY, selectedSlot, config);
        if (showOffhand) renderSingleSlotBg(context, ohOffX, ohOffY, config.colors.slots(), config);

        matrices.popMatrix();

        ScreenRectangle scissor = context.scissorStack.peek();
        Matrix3x2f pose = new Matrix3x2f(context.pose());

        HotbarHudRenderState renderState = new HotbarHudRenderState(
                inventory, config, selectedSlot, rows, cols,
                showOffhand, mainOffX, mainOffY, ohOffX, ohOffY,
                pose, scissor,
                x, y, x + scaledW, y + scaledH
        );
        context.guiRenderState.addPicturesInPictureState(renderState);

        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) matrices.scale(scale, scale);

        renderOverlays(context, inventory, rows, cols, mainOffX, mainOffY, config);
        if (showOffhand) renderOffhandOverlay(context, inventory, ohOffX, ohOffY, config);

        matrices.popMatrix();
    }

    private int calcX(SchrumboHudConfig config, int screenWidth, int hudWidth) {
        return switch (config.hotbarAnchor.horizontal) {
            case LEFT -> config.hotbarPosition.x;
            case RIGHT -> screenWidth - hudWidth - config.hotbarPosition.x;
        };
    }

    private int calcY(SchrumboHudConfig config, int screenHeight, int hudHeight) {
        return switch (config.hotbarAnchor.vertical) {
            case TOP -> config.hotbarPosition.y;
            case BOTTOM -> screenHeight - hudHeight - config.hotbarPosition.y;
        };
    }

    private int applyTransparency(int color, float transparency) {
        int alpha = (int) (((color >> 24) & 0xFF) * transparency);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private void drawPanel(GuiGraphicsExtractor context, int ox, int oy, int w, int h, SchrumboHudConfig config) {
        float t = config.hotbar.hotbarTransparency;
        if (config.appearance.backgroundEnabled) {
            int bgColor = applyTransparency(config.colors.background(), t);
            if (config.general.roundedCorners) {
                RenderUtils.fillRoundedRect(context, ox, oy, w, h, 0.5f, bgColor);
            } else {
                context.fill(ox, oy, ox + w, oy + h, bgColor);
            }
        }
        if (config.appearance.outlineEnabled) {
            int borderColor = applyTransparency(config.colors.border(), t);
            if (config.general.roundedCorners) {
                RenderUtils.drawRoundedRectWithOutline(context, ox, oy, w, h, 0.5f, 1, borderColor);
            } else {
                RenderUtils.drawBorder(context, ox, oy, w, h, borderColor);
            }
        }
    }

    private void renderSlotBackgrounds(GuiGraphicsExtractor context, int rows, int cols, int mainOffX, int mainOffY, int selectedSlot, SchrumboHudConfig config) {
        float t = config.hotbar.hotbarTransparency;
        int slotColor = applyTransparency(config.colors.slots(), t);
        int activeColor = applyTransparency(config.hotbar.hotbarActiveSlotColor, t);
        int slotW = SLOT_SIZE - 2;
        int slotH = SLOT_SIZE - 2;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int slotX = mainOffX + PADDING + col * SLOT_SIZE + 1;
                int slotY = mainOffY + PADDING + row * SLOT_SIZE + 1;
                boolean active = index == selectedSlot;

                if (config.appearance.slotBackgroundEnabled) {
                    if (active && !config.hotbar.hotbarActiveSlotOutline) {
                        if (config.general.roundedCorners) {
                            RenderUtils.drawRectWithCutCorners(context, slotX, slotY, slotW, slotH, 1, activeColor);
                        } else {
                            context.fill(slotX, slotY, slotX + slotW, slotY + slotH, activeColor);
                        }
                    } else if (active && config.hotbar.hotbarActiveSlotOutline && config.general.roundedCorners) {
                        RenderUtils.drawRectWithCutCorners(context, slotX, slotY, slotW, slotH, 1, slotColor);
                        context.fill(slotX, slotY, slotX + 1, slotY + 1, activeColor);
                        context.fill(slotX + slotW - 1, slotY, slotX + slotW, slotY + 1, activeColor);
                        context.fill(slotX, slotY + slotH - 1, slotX + 1, slotY + slotH, activeColor);
                        context.fill(slotX + slotW - 1, slotY + slotH - 1, slotX + slotW, slotY + slotH, activeColor);
                    } else {
                        if (config.general.roundedCorners) {
                            RenderUtils.drawRectWithCutCorners(context, slotX, slotY, slotW, slotH, 1, slotColor);
                        } else {
                            context.fill(slotX, slotY, slotX + slotW, slotY + slotH, slotColor);
                        }
                    }
                }

                if (active && config.hotbar.hotbarActiveSlotOutline) {
                    if (config.general.roundedCorners) {
                        RenderUtils.drawRoundedRectWithOutline(context, slotX - 1, slotY - 1, slotW + 2, slotH + 2, 0.5f, 1, activeColor);
                    } else {
                        RenderUtils.drawBorder(context, slotX - 1, slotY - 1, slotW + 2, slotH + 2, activeColor);
                    }
                }

                index++;
            }
        }
    }

    private void renderSingleSlotBg(GuiGraphicsExtractor context, int panelX, int panelY, int rawColor, SchrumboHudConfig config) {
        if (!config.appearance.slotBackgroundEnabled) return;

        float t = config.hotbar.hotbarTransparency;
        int slotColor = applyTransparency(rawColor, t);
        int slotX = panelX + PADDING + 1;
        int slotY = panelY + PADDING + 1;
        if (config.general.roundedCorners) {
            RenderUtils.drawRectWithCutCorners(context, slotX, slotY, SLOT_SIZE - 2, SLOT_SIZE - 2, 1, slotColor);
        } else {
            context.fill(slotX, slotY, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2, slotColor);
        }
    }

    private void renderOverlays(GuiGraphicsExtractor context, Inventory inventory, int rows, int cols, int mainOffX, int mainOffY, SchrumboHudConfig config) {
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStack stack = inventory.getItem(index);
                index++;
                if (stack.isEmpty()) continue;

                int slotX = mainOffX + PADDING + col * SLOT_SIZE + 1;
                int slotY = mainOffY + PADDING + row * SLOT_SIZE + 1;

                if (stack.getCount() > 1) renderStackCount(context, stack, slotX, slotY, config);
                if (stack.isDamaged()) renderDurabilityBar(context, stack, slotX, slotY);
            }
        }
    }

    private void renderOffhandOverlay(GuiGraphicsExtractor context, Inventory inventory, int ohOffX, int ohOffY, SchrumboHudConfig config) {
        ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
        if (offhand.isEmpty()) return;

        int slotX = ohOffX + PADDING + 1;
        int slotY = ohOffY + PADDING + 1;

        if (offhand.getCount() > 1) renderStackCount(context, offhand, slotX, slotY, config);
        if (offhand.isDamaged()) renderDurabilityBar(context, offhand, slotX, slotY);
    }

    private void renderStackCount(GuiGraphicsExtractor context, ItemStack stack, int x, int y, SchrumboHudConfig config) {
        String count = String.valueOf(stack.getCount());
        var font = Minecraft.getInstance().font;
        context.text(font, count,
                x + SLOT_SIZE - 2 - font.width(count),
                y + SLOT_SIZE - 9, config.colors.text(), config.appearance.textShadowEnabled);
    }

    private void renderDurabilityBar(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
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
