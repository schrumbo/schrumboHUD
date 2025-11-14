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

/**
 * Base class for Categories, used to Hold widgets
 */
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

    /**
     * initializes widgets if that has not already happened
     * @param startX
     * @param startY
     * @param width
     */
    public void initializeWidgetsIfNeeded(int startX, int startY, int width) {
        if (!widgetsInitialized) {
            initializeWidgets(startX, startY, width);
            widgetsInitialized = true;
        } else {
            updateWidgetPositions(startX, startY);
        }
    }


    /**
     * updates widget positions so they stay in the panel
     * @param startX
     * @param startY
     */
    protected void updateWidgetPositions(int startX, int startY) {
        int currentY = startY;
        for (Widget widget : widgets) {
            widget.setPosition(startX, currentY);
            currentY += widget.getHeight() + WIDGET_SPACING;
        }
    }


    /**
     * renders all the categories widgets
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    public void renderWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Widget widget : widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }


    /**
     * renders the title bar / header for the category
     * @param context
     * @param mouseX
     * @param mouseY
     * @param isSelected
     */
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
        int textColor = (hovered || isSelected) ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(labelX, labelY);
        matrices.scale(config.guicolors.headingSize, config.guicolors.headingSize);
        if(isSelected){
            context.drawText(client.textRenderer, Text.literal(name), 0, 0, textColor, true);
        }else{
            context.drawText(client.textRenderer, Text.literal(name), 0, 0, textColor, true);
        }

        matrices.popMatrix();
    }

    /**
     * handles mouse clicks on widgets
     * @param mouseX
     * @param mouseY
     * @param button
     * @return
     */
    public boolean mouseClickedWidgets(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
            if (widget instanceof ColorPickerWidget picker) {
                    return picker.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (Widget widget : widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    /**
     * handles dragging for widgets -> used for Color Pickers and Sliders
     * @param mouseX
     * @param mouseY
     * @param button
     * @param deltaX
     * @param deltaY
     * @return
     */
    public boolean mouseDraggedWidgets(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Widget widget : widgets) {

                return widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        for (Widget widget : widgets) {
            if (widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }

        return false;
    }

    /**
     * handles mouse release for widgets -> used for Color Pickers and Sliders
     * @param mouseX
     * @param mouseY
     * @param button
     * @return
     */
    public boolean mouseReleasedWidgets(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
                return widget.mouseReleased(mouseX, mouseY, button);
        }

        for (Widget widget : widgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    /**
     * handles key presses
     * @param keyCode
     * @param scanCode
     * @param modifiers
     * @return
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }


    /**
     * checks if the categories header is hovered
     * @param mouseX
     * @param mouseY
     * @return if hovered
     */
    public boolean isHeaderHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    /**
     * gets height with widgets
     * @deprecated widgets are no longer interacting with the category headers due to layout change
     * @return
     */
    @Deprecated
    public int getTotalHeight() {
        return HEADER_HEIGHT;
    }

    /**
     * gets header height
     * @return
     */
    public int getHeaderHeight() {
        return HEADER_HEIGHT;
    }

    /**
     * sets a categories position and width
     * @param x
     * @param y
     * @param width
     */
    public void setPosition(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    /**
     * sets a categories position
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * gets a categories name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * checks if the categories widgets are initialized
     * @return true if initialized
     */
    public boolean isWidgetsInitialized() {
        return widgetsInitialized;
    }
}