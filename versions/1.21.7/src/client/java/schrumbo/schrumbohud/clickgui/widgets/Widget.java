package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.gui.DrawContext;
import schrumbo.schrumbohud.Utils.Utils;

public abstract class Widget {
    protected int x, y;
    protected int width, height;
    protected String label;
    protected boolean hovered;

    public Widget(int x, int y, int width, int height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    public abstract boolean mouseReleased(double mouseX, double mouseY, int button);

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        if(Utils.isInColorPickerWidget()){
            return false;
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getHeight() {
        return height;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}
