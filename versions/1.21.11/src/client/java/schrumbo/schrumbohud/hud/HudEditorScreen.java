package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD position editor — drag, nudge, scale, auto-anchor
 */
public class HudEditorScreen extends Screen {

    private final Screen parent;

    private static final int SLOT_SIZE = 18;
    private static final int INV_COLS = 9;
    private static final int INV_ROWS = 3;
    private static final int PADDING = 4;
    private static final int INV_WIDTH = INV_COLS * SLOT_SIZE + PADDING * 2;
    private static final int INV_HEIGHT = INV_ROWS * SLOT_SIZE + PADDING * 2;
    private static final int HOTBAR_SLOTS = 9;
    private static final int OFFHAND_GAP = 4;
    private static final int OFFHAND_PANEL_WIDTH = SLOT_SIZE + PADDING * 2;

    private static final int OUTLINE_COLOR = 0xFF007ACC;
    private static final int OUTLINE_HOVER_COLOR = 0x80007ACC;
    private static final int HIT_PADDING = 4;
    private static final int NUDGE_AMOUNT = 1;

    private static final float SCALE_STEP = 0.1f;
    private static final float SCALE_MIN = 0.5f;
    private static final float SCALE_MAX = 3.0f;

    private static final int LEGEND_BG = 0xFF2D2D30;
    private static final int LEGEND_BORDER = 0xFF3E3E42;
    private static final int LEGEND_TEXT = 0xFFD4D4D4;
    private static final int LEGEND_MUTED = 0xFF858585;
    private static final int LEGEND_MARGIN = 8;
    private static final int LEGEND_PAD = 8;
    private static final int LEGEND_LINE_HEIGHT = 12;

    private enum Element { INVENTORY, ARMOR, HOTBAR }

    private Element selected = null;
    private boolean dragging = false;
    private int dragAbsoluteX = 0;
    private int dragAbsoluteY = 0;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x80000000);

        drawLegend(context);
        renderPreview(context, mouseX, mouseY, Element.INVENTORY);
        renderPreview(context, mouseX, mouseY, Element.ARMOR);
        renderPreview(context, mouseX, mouseY, Element.HOTBAR);

        if (selected != null) {
            drawAnchorIndicator(context, selected);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderPreview(DrawContext context, int mouseX, int mouseY, Element element) {
        var config = SchrumboHUDClient.config;

        if (element == Element.ARMOR && !config.armorEnabled) return;
        if (element == Element.HOTBAR && !config.hotbarEnabled) return;

        int w = getElementWidth(element, config);
        int h = getElementHeight(element, config);
        int x = getElementX(element, config, w);
        int y = getElementY(element, config, h);

        float scale = getElementScale(element, config);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        int unscaledW = getUnscaledWidth(element, config);
        int unscaledH = getUnscaledHeight(element, config);

        int bgColor = config.colors.background();
        float radius = element == Element.ARMOR ? 0.5f : 0.2f;
        if (config.roundedCorners) {
            RenderUtils.fillRoundedRect(context, 0, 0, unscaledW, unscaledH, radius, bgColor);
        } else {
            context.fill(0, 0, unscaledW, unscaledH, bgColor);
        }

        int borderColor = config.colors.border();
        if (config.roundedCorners) {
            RenderUtils.drawRoundedRectWithOutline(context, 0, 0, unscaledW, unscaledH, radius, 1, borderColor);
        } else {
            RenderUtils.drawBorder(context, 0, 0, unscaledW, unscaledH, borderColor);
        }

        matrices.popMatrix();

        boolean isSelected = selected == element;
        boolean isHovered = !dragging && isInside(mouseX, mouseY, x, y, w, h);

        if (isSelected) {
            RenderUtils.drawBorder(context,
                    x - HIT_PADDING, y - HIT_PADDING,
                    w + HIT_PADDING * 2, h + HIT_PADDING * 2,
                    OUTLINE_COLOR);
            String name = getElementName(element);
            int nameWidth = textRenderer.getWidth(name);
            context.drawText(textRenderer, name,
                    x + (w - nameWidth) / 2, y - HIT_PADDING - 12,
                    OUTLINE_COLOR, true);
        } else if (isHovered) {
            RenderUtils.drawBorder(context,
                    x - HIT_PADDING, y - HIT_PADDING,
                    w + HIT_PADDING * 2, h + HIT_PADDING * 2,
                    OUTLINE_HOVER_COLOR);
        }
    }

    private void drawLegend(DrawContext context) {
        List<String> lines = new ArrayList<>();
        lines.add("HUD Editor");
        lines.add("");
        if (selected != null) {
            SchrumboHudConfig.Anchor anchor = getAnchor(selected);
            lines.add("Selected: " + getElementName(selected));
            lines.add("Anchor: " + anchor.vertical.name() + "-" + anchor.horizontal.name());
            float displayScale = getElementScale(selected, SchrumboHUDClient.config);
            lines.add("Scale: " + String.format("%.0f%%", displayScale * 100));
            lines.add("");
            lines.add("Drag to move");
            lines.add("Arrow keys to nudge");
            lines.add("Scroll to resize");
            lines.add("R - Reset position");
        } else {
            lines.add("Click to select");
        }
        lines.add("Esc - Close");

        int maxTextWidth = 0;
        for (String line : lines) {
            int w = textRenderer.getWidth(line);
            if (w > maxTextWidth) maxTextWidth = w;
        }

        int panelWidth = maxTextWidth + LEGEND_PAD * 2;
        int panelHeight = lines.size() * LEGEND_LINE_HEIGHT + LEGEND_PAD * 2 - (LEGEND_LINE_HEIGHT - textRenderer.fontHeight);
        int panelX = width - panelWidth - LEGEND_MARGIN;
        int panelY = height - panelHeight - LEGEND_MARGIN;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, LEGEND_BG);
        RenderUtils.drawBorder(context, panelX, panelY, panelWidth, panelHeight, LEGEND_BORDER);

        int textX = panelX + LEGEND_PAD;
        int textY = panelY + LEGEND_PAD;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) {
                textY += LEGEND_LINE_HEIGHT;
                continue;
            }
            int color = (i == 0) ? OUTLINE_COLOR : LEGEND_TEXT;
            if (line.startsWith("R - ") || line.startsWith("Esc - ") || line.startsWith("Drag ")
                    || line.startsWith("Click ") || line.startsWith("Scroll ")
                    || line.startsWith("Arrow ")) {
                color = LEGEND_MUTED;
            }
            context.drawText(textRenderer, line, textX, textY, color, false);
            textY += LEGEND_LINE_HEIGHT;
        }
    }

    private void drawAnchorIndicator(DrawContext context, Element element) {
        SchrumboHudConfig.Anchor anchor = getAnchor(element);
        int dotSize = 6;
        int anchorX = anchor.horizontal == SchrumboHudConfig.HorizontalAnchor.LEFT ? 0 : width;
        int anchorY = anchor.vertical == SchrumboHudConfig.VerticalAnchor.TOP ? 0 : height;
        context.fill(anchorX - dotSize / 2, anchorY - dotSize / 2,
                anchorX + dotSize / 2, anchorY + dotSize / 2, 0xFF5DADE2);
    }

    private void applyAutoAnchor(Element element, int absX, int absY) {
        var config = SchrumboHUDClient.config;
        int pw = getElementWidth(element, config);
        int ph = getElementHeight(element, config);

        int centerX = absX + pw / 2;
        int centerY = absY + ph / 2;

        SchrumboHudConfig.HorizontalAnchor hAnchor;
        int offsetX;
        if (centerX <= width / 2) {
            hAnchor = SchrumboHudConfig.HorizontalAnchor.LEFT;
            offsetX = absX;
        } else {
            hAnchor = SchrumboHudConfig.HorizontalAnchor.RIGHT;
            offsetX = width - absX - pw;
        }

        SchrumboHudConfig.VerticalAnchor vAnchor;
        int offsetY;
        if (centerY <= height / 2) {
            vAnchor = SchrumboHudConfig.VerticalAnchor.TOP;
            offsetY = absY;
        } else {
            vAnchor = SchrumboHudConfig.VerticalAnchor.BOTTOM;
            offsetY = height - absY - ph;
        }

        offsetX = Math.max(-pw / 2, offsetX);
        offsetY = Math.max(-ph / 2, offsetY);

        SchrumboHudConfig.Anchor anchor = getAnchor(element);
        SchrumboHudConfig.Position position = getPosition(element);
        anchor.horizontal = hAnchor;
        anchor.vertical = vAnchor;
        position.x = offsetX;
        position.y = offsetY;
    }

    /** Nudges the selected element by the given pixel delta and re-anchors */
    private void nudgeSelected(int dx, int dy) {
        if (selected == null) return;

        var config = SchrumboHUDClient.config;
        int w = getElementWidth(selected, config);
        int h = getElementHeight(selected, config);
        int absX = getElementX(selected, config, w) + dx;
        int absY = getElementY(selected, config, h) + dy;

        absX = Math.max(-w / 2, Math.min(absX, width - w / 2));
        absY = Math.max(-h / 2, Math.min(absY, height - h / 2));

        applyAutoAnchor(selected, absX, absY);
        config.save();
    }

    private Element elementAt(double mouseX, double mouseY) {
        var config = SchrumboHUDClient.config;

        if (config.hotbarEnabled) {
            int hw = getElementWidth(Element.HOTBAR, config);
            int hh = getElementHeight(Element.HOTBAR, config);
            int hx = getElementX(Element.HOTBAR, config, hw);
            int hy = getElementY(Element.HOTBAR, config, hh);
            if (isInside(mouseX, mouseY, hx, hy, hw, hh)) {
                return Element.HOTBAR;
            }
        }

        if (config.armorEnabled) {
            int aw = getElementWidth(Element.ARMOR, config);
            int ah = getElementHeight(Element.ARMOR, config);
            int ax = getElementX(Element.ARMOR, config, aw);
            int ay = getElementY(Element.ARMOR, config, ah);
            if (isInside(mouseX, mouseY, ax, ay, aw, ah)) {
                return Element.ARMOR;
            }
        }

        int iw = getElementWidth(Element.INVENTORY, config);
        int ih = getElementHeight(Element.INVENTORY, config);
        int ix = getElementX(Element.INVENTORY, config, iw);
        int iy = getElementY(Element.INVENTORY, config, ih);
        if (isInside(mouseX, mouseY, ix, iy, iw, ih)) {
            return Element.INVENTORY;
        }

        return null;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x - HIT_PADDING && mouseX <= x + w + HIT_PADDING
                && mouseY >= y - HIT_PADDING && mouseY <= y + h + HIT_PADDING;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Element clicked = elementAt(click.x(), click.y());
            if (clicked != null) {
                var config = SchrumboHUDClient.config;
                int w = getElementWidth(clicked, config);
                int h = getElementHeight(clicked, config);
                int ex = getElementX(clicked, config, w);
                int ey = getElementY(clicked, config, h);

                selected = clicked;
                dragging = true;

                dragOffsetX = (int) click.x() - ex;
                dragOffsetY = (int) click.y() - ey;
                dragAbsoluteX = ex;
                dragAbsoluteY = ey;
                return true;
            } else {
                selected = null;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging && selected != null) {
            var config = SchrumboHUDClient.config;
            int sw = getElementWidth(selected, config);
            int sh = getElementHeight(selected, config);
            dragAbsoluteX = Math.max(-sw / 2, Math.min((int) click.x() - dragOffsetX, width - sw / 2));
            dragAbsoluteY = Math.max(-sh / 2, Math.min((int) click.y() - dragOffsetY, height - sh / 2));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging && selected != null) {
            dragging = false;
            applyAutoAnchor(selected, dragAbsoluteX, dragAbsoluteY);
            SchrumboHUDClient.config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selected != null) {
            var config = SchrumboHUDClient.config;
            float current = getElementScale(selected, config);
            float newScale = current + (float) verticalAmount * SCALE_STEP;
            newScale = Math.round(newScale * 10f) / 10f;
            newScale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, newScale));
            switch (selected) {
                case INVENTORY -> config.scale = newScale;
                case ARMOR -> config.armorScale = newScale;
                case HOTBAR -> config.hotbarScale = newScale;
            }
            config.save();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (selected != null) {
            switch (input.key()) {
                case GLFW.GLFW_KEY_R -> {
                    resetElement(selected);
                    return true;
                }
                case GLFW.GLFW_KEY_UP -> { nudgeSelected(0, -NUDGE_AMOUNT); return true; }
                case GLFW.GLFW_KEY_DOWN -> { nudgeSelected(0, NUDGE_AMOUNT); return true; }
                case GLFW.GLFW_KEY_LEFT -> { nudgeSelected(-NUDGE_AMOUNT, 0); return true; }
                case GLFW.GLFW_KEY_RIGHT -> { nudgeSelected(NUDGE_AMOUNT, 0); return true; }
            }
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            SchrumboHUDClient.config.save();
            this.close();
            return true;
        }

        return super.keyPressed(input);
    }

    private void resetElement(Element element) {
        var config = SchrumboHUDClient.config;
        SchrumboHudConfig.Anchor anchor = getAnchor(element);
        SchrumboHudConfig.Position position = getPosition(element);
        if (element == Element.HOTBAR) {
            anchor.horizontal = SchrumboHudConfig.HorizontalAnchor.LEFT;
            anchor.vertical = SchrumboHudConfig.VerticalAnchor.BOTTOM;
            position.x = -1;
            position.y = 2;
            config.hotbarScale = 1.0f;
        } else {
            anchor.horizontal = SchrumboHudConfig.HorizontalAnchor.LEFT;
            anchor.vertical = SchrumboHudConfig.VerticalAnchor.TOP;
            position.x = 10;
            position.y = 10;
            if (element == Element.INVENTORY) {
                config.scale = 1.0f;
            } else {
                config.armorScale = 1.0f;
            }
        }
        config.save();
    }

    @Override
    public void close() {
        SchrumboHUDClient.config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private String getElementName(Element element) {
        return switch (element) {
            case INVENTORY -> "Inventory HUD";
            case ARMOR -> "Armor HUD";
            case HOTBAR -> "Hotbar";
        };
    }

    private SchrumboHudConfig.Anchor getAnchor(Element element) {
        var config = SchrumboHUDClient.config;
        return switch (element) {
            case INVENTORY -> config.anchor;
            case ARMOR -> config.armorAnchor;
            case HOTBAR -> config.hotbarAnchor;
        };
    }

    private SchrumboHudConfig.Position getPosition(Element element) {
        var config = SchrumboHUDClient.config;
        return switch (element) {
            case INVENTORY -> config.position;
            case ARMOR -> config.armorPosition;
            case HOTBAR -> config.hotbarPosition;
        };
    }

    private int getUnscaledWidth(Element element, SchrumboHudConfig config) {
        return switch (element) {
            case INVENTORY -> INV_WIDTH;
            case ARMOR -> {
                int cols = config.armorVertical ? 1 : 4;
                yield cols * SLOT_SIZE + PADDING * 2;
            }
            case HOTBAR -> {
                int cols = config.hotbarVertical ? 1 : HOTBAR_SLOTS;
                int mainWidth = cols * SLOT_SIZE + PADDING * 2;
                if (config.hotbarShowOffhand && !config.hotbarVertical) {
                    yield mainWidth + OFFHAND_GAP + OFFHAND_PANEL_WIDTH;
                }
                yield mainWidth;
            }
        };
    }

    private int getUnscaledHeight(Element element, SchrumboHudConfig config) {
        return switch (element) {
            case INVENTORY -> INV_HEIGHT;
            case ARMOR -> {
                int rows = config.armorVertical ? 4 : 1;
                yield rows * SLOT_SIZE + PADDING * 2;
            }
            case HOTBAR -> {
                int rows = config.hotbarVertical ? HOTBAR_SLOTS : 1;
                int mainHeight = rows * SLOT_SIZE + PADDING * 2;
                if (config.hotbarShowOffhand && config.hotbarVertical) {
                    yield mainHeight + OFFHAND_GAP + OFFHAND_PANEL_WIDTH;
                }
                yield mainHeight;
            }
        };
    }

    private float getElementScale(Element element, SchrumboHudConfig config) {
        return switch (element) {
            case INVENTORY -> config.scale;
            case ARMOR -> config.armorScale;
            case HOTBAR -> config.hotbarScale;
        };
    }

    private int getElementWidth(Element element, SchrumboHudConfig config) {
        return (int) (getUnscaledWidth(element, config) * getElementScale(element, config));
    }

    private int getElementHeight(Element element, SchrumboHudConfig config) {
        return (int) (getUnscaledHeight(element, config) * getElementScale(element, config));
    }

    private int getElementX(Element element, SchrumboHudConfig config, int w) {
        if (dragging && element == selected) return dragAbsoluteX;
        SchrumboHudConfig.Position pos = getPosition(element);
        SchrumboHudConfig.Anchor anc = getAnchor(element);
        return switch (anc.horizontal) {
            case LEFT -> pos.x;
            case RIGHT -> width - w - pos.x;
        };
    }

    private int getElementY(Element element, SchrumboHudConfig config, int h) {
        if (dragging && element == selected) return dragAbsoluteY;
        SchrumboHudConfig.Position pos = getPosition(element);
        SchrumboHudConfig.Anchor anc = getAnchor(element);
        return switch (anc.vertical) {
            case TOP -> pos.y;
            case BOTTOM -> height - h - pos.y;
        };
    }
}
