package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.gui.DrawContext;
import schrumbo.schrumbohud.Utils.Utils;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.Widget;

import java.util.ArrayList;
import java.util.List;

public abstract class Category {
    protected String name;
    public List<Widget> widgets;
    protected boolean collapsed;
    protected boolean widgetsInitialized;

    protected int x, y;
    protected int width;

    protected static final int HEADER_HEIGHT = 30;
    protected static final int PADDING = 5;
    protected static final int WIDGET_SPACING = 5;

    public Category(String name) {
        this.name = name;
        this.widgets = new ArrayList<>();
        this.collapsed = true;
        this.widgetsInitialized = false;
    }

    public abstract void initializeWidgets(int startX, int startY, int width);

    protected void updateWidgetPositions(int startX, int startY) {
        int currentY = startY;
        for (Widget widget : widgets) {
            widget.setPosition(startX, currentY);
            currentY += widget.getHeight() + WIDGET_SPACING;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderHeader(context, mouseX, mouseY);

        if (!collapsed) {
            for (Widget widget : widgets) {
                widget.render(context, mouseX, mouseY, delta);
            }

            for (Widget widget : widgets) {
                if (widget instanceof ColorPickerWidget) {
                    ColorPickerWidget picker = (ColorPickerWidget) widget;
                    if (picker.isPopupOpen()) {
                        picker.renderPopupLayered(context, mouseX, mouseY, delta);
                    }
                }
            }
        }
    }


    protected abstract void renderHeader(DrawContext context, int mouseX, int mouseY);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!collapsed) {
            for (Widget widget : widgets) {
                if (widget instanceof ColorPickerWidget) {
                    ColorPickerWidget picker = (ColorPickerWidget) widget;
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
        }

        if (button == 0 && isHeaderHovered(mouseX, mouseY)) {
            collapsed = !collapsed;
            return true;
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!collapsed) {
            for (Widget widget : widgets) {
                if (widget instanceof ColorPickerWidget && ((ColorPickerWidget) widget).isPopupOpen()) {
                    return widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }

            for (Widget widget : widgets) {
                if (widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!collapsed) {
            for (Widget widget : widgets) {
                if (widget instanceof ColorPickerWidget && ((ColorPickerWidget) widget).isPopupOpen()) {
                    return widget.mouseReleased(mouseX, mouseY, button);
                }
            }

            for (Widget widget : widgets) {
                if (widget.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!collapsed) {
            for (Widget widget : widgets) {
                if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!collapsed) {
            for (Widget widget : widgets) {
                if (widget.charTyped(chr, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isHeaderHovered(double mouseX, double mouseY) {
        if(Utils.isInColorPickerWidget()) {
            return false;
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public int getTotalHeight() {
        if (collapsed) {
            return HEADER_HEIGHT;
        }

        int height = HEADER_HEIGHT + PADDING;
        for (Widget widget : widgets) {
            height += widget.getHeight() + WIDGET_SPACING;
        }
        return height + PADDING;
    }

    public void setPosition(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;

        int startX = x + PADDING;
        int startY = y + HEADER_HEIGHT + PADDING;
        int contentWidth = width - PADDING ;

        if (!widgetsInitialized) {
            initializeWidgets(startX, startY, contentWidth);
            widgetsInitialized = true;
        } else {
            updateWidgetPositions(startX, startY);
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;

        int startX = x + PADDING;
        int startY = y + HEADER_HEIGHT + PADDING;
        int contentWidth = width - PADDING * 2;

        if (!widgetsInitialized) {
            initializeWidgets(startX, startY, contentWidth);
            widgetsInitialized = true;
        } else {
            updateWidgetPositions(startX, startY);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public boolean isWidgetsInitialized() {
        return widgetsInitialized;
    }

}
