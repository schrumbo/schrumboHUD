package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.gui.DrawContext;
import schrumbo.schrumbohud.Utils.Utils;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;

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

    protected Widget(Builder<?> builder){
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.label = builder.label;
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

    public static abstract class Builder<T extends Builder<T>> {
        protected int x = 0;
        protected int y = 0;
        protected int width = 100;
        protected int height = 50;
        protected String label = "";

        public T position(int x, int y) {
            this.x = x;
            this.y = y;
            return self();
        }

        public T x(int x) {
            this.x = x;
            return self();
        }

        public T y(int y) {
            this.y = y;
            return self();
        }

        public T size(int width, int height) {
            this.width = width;
            this.height = height;
            return self();
        }

        public T width(int width) {
            this.width = width;
            return self();
        }

        public T height(int height) {
            this.height = height;
            return self();
        }

        public T label(String label) {
            this.label = label;
            return self();
        }

        protected abstract T self();
        public abstract Widget build();
    }
}
