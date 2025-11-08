package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.Utils.Utils;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.Widget;

import java.util.ArrayList;
import java.util.List;

import static schrumbo.schrumbohud.SchrumboHUDClient.config;


public abstract class Category {
    protected String name;
    public List<Widget> widgets;
    protected boolean widgetsInitialized;

    protected int x, y;
    protected int width;

    protected MinecraftClient client = MinecraftClient.getInstance();

    protected static final int HEADER_HEIGHT = 40;
    protected static final int PADDING = 7;
    protected static final int WIDGET_SPACING = 5;

    public Category(String name) {
        this.name = name;
        this.widgets = new ArrayList<>();
        this.widgetsInitialized = false;
    }


    public abstract void initializeWidgets(int startX, int startY, int width);


    public void initializeWidgetsIfNeeded(int startX, int startY, int width) {
        if (!widgetsInitialized) {
            initializeWidgets(startX, startY, width);
            widgetsInitialized = true;
        } else {
            updateWidgetPositions(startX, startY);
        }
    }


    protected void updateWidgetPositions(int startX, int startY) {
        int currentY = startY;
        for (Widget widget : widgets) {
            widget.setPosition(startX, currentY);
            currentY += widget.getHeight() + WIDGET_SPACING;
        }
    }


    public void renderWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Widget widget : widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }


    public void renderHeader(DrawContext context, int mouseX, int mouseY, boolean isSelected) {
        boolean hovered = isHeaderHovered(mouseX, mouseY);

        int bgColor;
        if (isSelected) {
            bgColor = config.colorWithAlpha(config.guicolors.accent, 0.2f);
        } else if (hovered) {
            bgColor = config.guicolors.widgetBackgroundHovered;
        } else {
            bgColor = config.guicolors.widgetBackground;
        }

        RenderUtils.fillRoundedRect(context, x, y, width, HEADER_HEIGHT, 0.0f, bgColor);

        RenderUtils.fillRoundedRect(context, x, y, width, 3, 0.0f,
                config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetAccentOpacity));

        int labelX = x + PADDING + 13;
        int labelY = y + (HEADER_HEIGHT - client.textRenderer.fontHeight) / 2;
        int textColor = (hovered || isSelected) ?
                config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) :
                config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(labelX, labelY, 1.0f);
        matrices.scale(config.guicolors.headingSize, config.guicolors.headingSize, 1.0f);
        context.drawText(client.textRenderer, Text.literal(name), 0, 0, textColor, true);
        matrices.pop();
    }

    public boolean mouseClickedWidgets(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
            if (widget instanceof ColorPickerWidget picker) {
                if (picker.isPopupOpen()) {
                    return picker.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        for (Widget widget : widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    public boolean mouseDraggedWidgets(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Widget widget : widgets) {
            if (widget instanceof ColorPickerWidget picker && picker.isPopupOpen()) {
                return widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }

        for (Widget widget : widgets) {
            if (widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }

        return false;
    }


    public boolean mouseReleasedWidgets(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
            if (widget instanceof ColorPickerWidget picker && picker.isPopupOpen()) {
                return widget.mouseReleased(mouseX, mouseY, button);
            }
        }

        for (Widget widget : widgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        for (Widget widget : widgets) {
            if (widget.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHeaderHovered(double mouseX, double mouseY) {
        if(Utils.isInColorPickerWidget()) {
            return false;
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public int getTotalHeight() {
        return HEADER_HEIGHT;
    }

    public int getHeaderHeight() {
        return HEADER_HEIGHT;
    }

    public void setPosition(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public boolean isWidgetsInitialized() {
        return widgetsInitialized;
    }
}