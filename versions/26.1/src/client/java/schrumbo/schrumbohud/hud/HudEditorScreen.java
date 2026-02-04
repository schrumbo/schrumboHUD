package schrumbo.schrumbohud.hud;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD position editor — drag, snap, scale, auto-anchor
 */
public class HudEditorScreen extends Screen {

    private final Screen parent;

    private static final int SLOT_SIZE = 18;
    private static final int INV_COLS = 9;
    private static final int INV_ROWS = 3;
    private static final int PADDING = 4;
    private static final int INV_WIDTH = INV_COLS * SLOT_SIZE + PADDING * 2;
    private static final int INV_HEIGHT = INV_ROWS * SLOT_SIZE + PADDING * 2;

    private static final int OUTLINE_COLOR = 0xFF007ACC;
    private static final int OUTLINE_HOVER_COLOR = 0x80007ACC;
    private static final int SNAP_LINE_COLOR = 0xAAFFFFFF;
    private static final int SNAP_THRESHOLD = 5;
    private static final int HIT_PADDING = 4;

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

    private enum Element { INVENTORY, ARMOR }

    private Element selected = null;
    private boolean dragging = false;
    private boolean dragStarted = false;
    private int dragAbsoluteX = 0;
    private int dragAbsoluteY = 0;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int dragOriginX = 0;
    private int dragOriginY = 0;

    private final List<int[]> snapLines = new ArrayList<>();

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x80000000);

        drawLegend(context);
        renderPreview(context, mouseX, mouseY, Element.INVENTORY);
        renderPreview(context, mouseX, mouseY, Element.ARMOR);

        for (int[] line : snapLines) {
            if (line[0] == line[2]) {
                context.fill(line[0], line[1], line[0] + 1, line[3], SNAP_LINE_COLOR);
            } else {
                context.fill(line[0], line[1], line[2], line[1] + 1, SNAP_LINE_COLOR);
            }
        }

        if (selected != null) {
            drawAnchorIndicator(context, selected);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderPreview(GuiGraphics context, int mouseX, int mouseY, Element element) {
        var config = SchrumboHUDClient.config;

        if (element == Element.ARMOR && !config.armorEnabled) return;

        int w = getElementWidth(element, config);
        int h = getElementHeight(element, config);
        int x = getElementX(element, config, w);
        int y = getElementY(element, config, h);

        float scale = getElementScale(element, config);

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        int unscaledW = getUnscaledWidth(element, config);
        int unscaledH = getUnscaledHeight(element, config);

        int bgColor = config.colors.background();
        float radius = element == Element.INVENTORY ? 0.2f : 0.5f;
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
            String name = element == Element.INVENTORY ? "Inventory HUD" : "Armor HUD";
            int nameWidth = font.width(name);
            context.drawString(font, name,
                    x + (w - nameWidth) / 2, y - HIT_PADDING - 12,
                    OUTLINE_COLOR, true);
        } else if (isHovered) {
            RenderUtils.drawBorder(context,
                    x - HIT_PADDING, y - HIT_PADDING,
                    w + HIT_PADDING * 2, h + HIT_PADDING * 2,
                    OUTLINE_HOVER_COLOR);
        }
    }

    private void drawLegend(GuiGraphics context) {
        List<String> lines = new ArrayList<>();
        lines.add("HUD Editor");
        lines.add("");
        if (selected != null) {
            String name = selected == Element.INVENTORY ? "Inventory HUD" : "Armor HUD";
            SchrumboHudConfig.Anchor anchor = getAnchor(selected);
            lines.add("Selected: " + name);
            lines.add("Anchor: " + anchor.vertical.name() + "-" + anchor.horizontal.name());
            float displayScale = selected == Element.INVENTORY
                    ? SchrumboHUDClient.config.scale : SchrumboHUDClient.config.armorScale;
            lines.add("Scale: " + String.format("%.0f%%", displayScale * 100));
            lines.add("");
            lines.add("Drag to move");
            lines.add("Scroll to resize");
            lines.add("R - Reset position");
        } else {
            lines.add("Click to select");
        }
        lines.add("Esc - Close");

        int maxTextWidth = 0;
        for (String line : lines) {
            int w = font.width(line);
            if (w > maxTextWidth) maxTextWidth = w;
        }

        int panelWidth = maxTextWidth + LEGEND_PAD * 2;
        int panelHeight = lines.size() * LEGEND_LINE_HEIGHT + LEGEND_PAD * 2 - (LEGEND_LINE_HEIGHT - font.lineHeight);
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
                    || line.startsWith("Click ") || line.startsWith("Scroll ")) {
                color = LEGEND_MUTED;
            }
            context.drawString(font, line, textX, textY, color, false);
            textY += LEGEND_LINE_HEIGHT;
        }
    }

    private void drawAnchorIndicator(GuiGraphics context, Element element) {
        SchrumboHudConfig.Anchor anchor = getAnchor(element);
        int dotSize = 6;
        int anchorX = anchor.horizontal == SchrumboHudConfig.HorizontalAnchor.LEFT ? 0 : width;
        int anchorY = anchor.vertical == SchrumboHudConfig.VerticalAnchor.TOP ? 0 : height;
        context.fill(anchorX - dotSize / 2, anchorY - dotSize / 2,
                anchorX + dotSize / 2, anchorY + dotSize / 2, 0xFF5DADE2);
    }

    private void applySnapping() {
        snapLines.clear();
        if (selected == null) return;

        var config = SchrumboHUDClient.config;
        int sw = getElementWidth(selected, config);
        int sh = getElementHeight(selected, config);

        Element other = selected == Element.INVENTORY ? Element.ARMOR : Element.INVENTORY;
        if (other == Element.ARMOR && !config.armorEnabled) return;

        int ow = getElementWidth(other, config);
        int oh = getElementHeight(other, config);
        int oLeft = getElementX(other, config, ow);
        int oTop = getElementY(other, config, oh);

        int[] sEdgesX = {dragAbsoluteX, dragAbsoluteX + sw, dragAbsoluteX + sw / 2};
        int[] oEdgesX = {oLeft, oLeft + ow, oLeft + ow / 2};
        int[] sEdgesY = {dragAbsoluteY, dragAbsoluteY + sh, dragAbsoluteY + sh / 2};
        int[] oEdgesY = {oTop, oTop + oh, oTop + oh / 2};

        int bestDist = SNAP_THRESHOLD + 1;
        int bestSnapX = dragAbsoluteX;
        int bestLineX = -1;

        for (int se : sEdgesX) {
            for (int oe : oEdgesX) {
                int dist = Math.abs(se - oe);
                if (dist > 0 && dist < bestDist) {
                    bestDist = dist;
                    bestSnapX = dragAbsoluteX + (oe - se);
                    bestLineX = oe;
                }
            }
        }

        if (bestDist <= SNAP_THRESHOLD) {
            dragAbsoluteX = Math.max(0, Math.min(bestSnapX, width - sw));
            snapLines.add(new int[]{bestLineX, 0, bestLineX, height});
        }

        bestDist = SNAP_THRESHOLD + 1;
        int bestSnapY = dragAbsoluteY;
        int bestLineY = -1;

        for (int se : sEdgesY) {
            for (int oe : oEdgesY) {
                int dist = Math.abs(se - oe);
                if (dist > 0 && dist < bestDist) {
                    bestDist = dist;
                    bestSnapY = dragAbsoluteY + (oe - se);
                    bestLineY = oe;
                }
            }
        }

        if (bestDist <= SNAP_THRESHOLD) {
            dragAbsoluteY = Math.max(0, Math.min(bestSnapY, height - sh));
            snapLines.add(new int[]{0, bestLineY, width, bestLineY});
        }
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

        offsetX = Math.max(0, offsetX);
        offsetY = Math.max(0, offsetY);

        SchrumboHudConfig.Anchor anchor = getAnchor(element);
        SchrumboHudConfig.Position position = getPosition(element);
        anchor.horizontal = hAnchor;
        anchor.vertical = vAnchor;
        position.x = offsetX;
        position.y = offsetY;
    }

    private Element elementAt(double mouseX, double mouseY) {
        var config = SchrumboHUDClient.config;

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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
                dragOriginX = ex;
                dragOriginY = ey;
                dragStarted = false;
                return true;
            } else {
                selected = null;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging && selected != null) {
            var config = SchrumboHUDClient.config;
            int sw = getElementWidth(selected, config);
            int sh = getElementHeight(selected, config);
            dragAbsoluteX = Math.max(0, Math.min((int) click.x() - dragOffsetX, width - sw));
            dragAbsoluteY = Math.max(0, Math.min((int) click.y() - dragOffsetY, height - sh));

            if (!dragStarted) {
                int movedX = Math.abs(dragAbsoluteX - dragOriginX);
                int movedY = Math.abs(dragAbsoluteY - dragOriginY);
                if (movedX > SNAP_THRESHOLD || movedY > SNAP_THRESHOLD) {
                    dragStarted = true;
                }
            }

            if (dragStarted) {
                applySnapping();
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging && selected != null) {
            dragging = false;
            snapLines.clear();
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
            float current = selected == Element.INVENTORY ? config.scale : config.armorScale;
            float newScale = current + (float) verticalAmount * SCALE_STEP;
            newScale = Math.round(newScale * 10f) / 10f;
            newScale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, newScale));
            if (selected == Element.INVENTORY) {
                config.scale = newScale;
            } else {
                config.armorScale = newScale;
            }
            config.save();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (selected != null && input.key() == GLFW.GLFW_KEY_R) {
            var config = SchrumboHUDClient.config;
            SchrumboHudConfig.Anchor anchor = getAnchor(selected);
            SchrumboHudConfig.Position position = getPosition(selected);
            anchor.horizontal = SchrumboHudConfig.HorizontalAnchor.LEFT;
            anchor.vertical = SchrumboHudConfig.VerticalAnchor.TOP;
            position.x = 10;
            position.y = 10;
            if (selected == Element.INVENTORY) {
                config.scale = 1.0f;
            } else {
                config.armorScale = 1.0f;
            }
            config.save();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            SchrumboHUDClient.config.save();
            this.onClose();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        SchrumboHUDClient.config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private SchrumboHudConfig.Anchor getAnchor(Element element) {
        var config = SchrumboHUDClient.config;
        return element == Element.INVENTORY ? config.anchor : config.armorAnchor;
    }

    private SchrumboHudConfig.Position getPosition(Element element) {
        var config = SchrumboHUDClient.config;
        return element == Element.INVENTORY ? config.position : config.armorPosition;
    }

    private int getUnscaledWidth(Element element, SchrumboHudConfig config) {
        if (element == Element.INVENTORY) return INV_WIDTH;
        int cols = config.armorVertical ? 1 : 4;
        return cols * SLOT_SIZE + PADDING * 2;
    }

    private int getUnscaledHeight(Element element, SchrumboHudConfig config) {
        if (element == Element.INVENTORY) return INV_HEIGHT;
        int rows = config.armorVertical ? 4 : 1;
        return rows * SLOT_SIZE + PADDING * 2;
    }

    private float getElementScale(Element element, SchrumboHudConfig config) {
        return element == Element.INVENTORY ? config.scale : config.armorScale;
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
