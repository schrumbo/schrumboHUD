package schrumbo.schrumbohud.clickgui.widgets;

import com.sun.jna.platform.win32.WinBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Interactive color picker widget for the ClickGUI.
 * Allows selecting colors through HSV color space with visual feedback.
 */
public class ColorPickerWidget extends Widget {
    private final Supplier<Integer> colorGetter;
    private final Consumer<Integer> colorSetter;
    private final Supplier<Float> opacityGetter;
    private final Consumer<Float> opacitySetter;

    private boolean popupOpen = false;
    private boolean hasOpacityControl = false;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingOpacity = false;

    private float hue = 0;
    private float saturation = 1;
    private float value = 1;
    private float opacity;

    private static final int WIDGET_HEIGHT = 50;
    private static final int POPUP_WIDTH = 245;
    private static final int POPUP_HEIGHT = 200;
    private static final int TITLE_BAR_HEIGHT = 30;

    private static final int PICKER_SIZE = 140;
    private static final int PADDING = 20;
    private static final int CLICK_BLOCK_PADDING = 10;

    private static final int SLIDER_WIDTH = 20;
    private static final int SLIDER_SPACING = 15;
    private static final int HANDLE_WIDTH = 28;
    private static final int HANDLE_HEIGHT = 8;

    private static final int Z_LAYER_OFFSET = 400;

    private static final int CLOSE_BUTTON_SIZE = 18;
    private static final int CLOSE_BUTTON_PADDING = 6;

    private int popupX;
    private int popupY;

    private int closeButtonX;
    private int closeButtonY;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private final HudConfig config = SchrumboHUDClient.config;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private ColorPickerWidget(Builder builder) {
        super(builder);
        this.colorGetter = builder.colorGetter;
        this.colorSetter = builder.colorSetter;
        this.opacityGetter = builder.opacityGetter;
        this.opacitySetter = builder.opacitySetter;
        this.hasOpacityControl = builder.hasOpacityControl;

        int color = colorGetter.get();
        this.opacity = opacityGetter.get();
        float[] hsv = rgbToHsv(color);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public static class Builder extends Widget.Builder<Builder> {
        private Supplier<Integer> colorGetter;
        private Consumer<Integer> colorSetter;
        private Supplier<Float> opacityGetter = () -> 1.0f;
        private Consumer<Float> opacitySetter = f -> {};
        private boolean hasOpacityControl = false;

        public Builder color(Supplier<Integer> getter, Consumer<Integer> setter) {
            this.colorGetter = getter;
            this.colorSetter = setter;
            return this;
        }

        public Builder opacity(Supplier<Float> getter, Consumer<Float> setter) {
            this.opacityGetter = getter;
            this.opacitySetter = setter;
            this.hasOpacityControl = true;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ColorPickerWidget build() {
            if (colorGetter == null || colorSetter == null) {
                throw new IllegalStateException("Color getter and setter must be set");
            }
            return new ColorPickerWidget(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);

        int textColor = hovered ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;
        RenderUtils.fillRoundedRect(context, x, y, width, WIDGET_HEIGHT, 0.0f, config.guicolors.widgetBackground);

        float textScale = config.guicolors.textSize;
        int labelHeight = client.textRenderer.fontHeight;

        int scaledHeight = (int)(labelHeight * textScale);

        int labelX = x + 7;
        int labelY = y + (WIDGET_HEIGHT - scaledHeight) / 2;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(labelX, labelY);
        matrices.scale(textScale, textScale);
        context.drawText(client.textRenderer, Text.literal(label), 0, 0, textColor, true);
        matrices.popMatrix();

        int previewSize = 20;
        int previewX = x + width - 8 - previewSize;
        int previewY = y + (WIDGET_HEIGHT - previewSize) / 2;

        int previewColor = config.colorWithAlpha(colorGetter.get(), opacityGetter.get());
        int borderColor = config.colorWithAlpha(0x404040, 0.8f);
        RenderUtils.drawRectWithCutCorners(context, previewX - 2, previewY - 2, previewSize + 4, previewSize + 4, 1, borderColor);

        RenderUtils.fillCheckerboard(context, previewX, previewY, previewSize, previewSize, 6);
        RenderUtils.fillRoundedRect(context, previewX, previewY, previewSize, previewSize, 0.0f, previewColor);

        if (popupOpen) {
            panelX = popupX;
            panelY = popupY;
            panelWidth = POPUP_WIDTH;
            panelHeight = POPUP_HEIGHT;
        }
    }

    /**
     * Renders the color picker popup on a separate layer
     */
    public void renderPopupLayered(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!popupOpen) return;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        renderPopupContent(context, mouseX, mouseY, delta);
        matrices.popMatrix();
    }

    /**
     * centers x pos
     */
    public void centerPosX(){
        int windowWidth = (int)(client.getWindow().getFramebufferWidth());
        popupX = (int)(windowWidth / 2 - POPUP_WIDTH / 2);
    }

    /**
     * centers y pos
     */
    public void centerPosY(){
        int windowHeight = (int)(client.getWindow().getFramebufferHeight());
        popupY = (int)(windowHeight / 2 - POPUP_HEIGHT / 2);
    }

    /**
     * Renders the main content of the color picker popup
     */
    private void renderPopupContent(DrawContext context, int mouseX, int mouseY, float delta) {


        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        centerPosX();
        centerPosY();

        closeButtonX = popupX + POPUP_WIDTH - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_PADDING;
        closeButtonY = popupY + CLOSE_BUTTON_PADDING;

        int shadowColor = config.colorWithAlpha(0x000000, 0.4f);
        RenderUtils.fillRoundedRect(context, popupX + 2, popupY + 2, POPUP_WIDTH, POPUP_HEIGHT, 0.0f, shadowColor);

        RenderUtils.fillRoundedRect(context, popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 0.0f, config.guicolors.panelBackground);

        RenderUtils.drawRoundedRectWithOutline(context, popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 0.0f, 2, config.colorWithAlpha(config.guicolors.accent, config.guicolors.panelBorderOpacity));

        RenderUtils.fillRoundedRect(context, popupX, popupY, POPUP_WIDTH, TITLE_BAR_HEIGHT, 0.0f, config.colorWithAlpha(config.guicolors.accent, config.guicolors.panelTitleBarOpacity));

        String title = "Color Picker";
        int titleWidth = client.textRenderer.getWidth(title);
        context.drawText(client.textRenderer, Text.literal(title), popupX + (POPUP_WIDTH - titleWidth) / 2, popupY + 11, config.guicolors.text, true);

        renderCloseButton(context, mouseX, mouseY, config);

        int contentY = popupY + TITLE_BAR_HEIGHT + 15;
        int pickerX = popupX + PADDING;

        renderSVPicker(context, pickerX, contentY, mouseX, mouseY);
        int hueSliderX = pickerX + PICKER_SIZE + SLIDER_SPACING;
        renderHueSlider(context, hueSliderX, contentY, mouseX, mouseY);
        int opacitySliderX = hueSliderX + SLIDER_WIDTH + SLIDER_SPACING;
        renderOpacitySlider(context, opacitySliderX, contentY, mouseX, mouseY);
    }

    /**
     * Renders the close/save button for the color picker popup
     */
    private void renderCloseButton(DrawContext context, int mouseX, int mouseY, HudConfig config) {
        boolean isHovered = isMouseOverCloseButton(mouseX, mouseY);
        var client = MinecraftClient.getInstance();

        String closeText = "close";
        int textWidth = client.textRenderer.getWidth(closeText);

        int buttonWidth = textWidth + 12;
        int buttonHeight = 18;

        closeButtonX = popupX + POPUP_WIDTH - buttonWidth - CLOSE_BUTTON_PADDING;
        closeButtonY = popupY + CLOSE_BUTTON_PADDING;


        RenderUtils.drawRoundedRectWithOutline(context, closeButtonX, closeButtonY, buttonWidth, buttonHeight, 0.2f, 1, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetBorderOpacity));

        int textColor = isHovered ? 0xFFFF4444 : 0xFFFFFFFF;
        int textX = closeButtonX + 6;
        int textY = closeButtonY + (buttonHeight - client.textRenderer.fontHeight) / 2;

        context.drawText(client.textRenderer, Text.literal(closeText), textX, textY, textColor, false);
    }

    /**
     * Checks if the mouse is over the close button
     */
    private boolean isMouseOverCloseButton(double mouseX, double mouseY) {
        var client = MinecraftClient.getInstance();
        String closeText = "save";
        int textWidth = client.textRenderer.getWidth(closeText);
        int buttonWidth = textWidth + 12;
        int buttonHeight = 18;

        int tempCloseX = popupX + POPUP_WIDTH - buttonWidth - CLOSE_BUTTON_PADDING;
        int tempCloseY = popupY + CLOSE_BUTTON_PADDING;

        return mouseX >= tempCloseX && mouseX <= tempCloseX + buttonWidth && mouseY >= tempCloseY && mouseY <= tempCloseY + buttonHeight;
    }

    /**
     * Renders the saturation-value picker
     */
    private void renderSVPicker(DrawContext context, int px, int py, int mouseX, int mouseY) {
        var matrices = context.getMatrices();
        context.enableScissor(px, py, px + PICKER_SIZE, py + PICKER_SIZE);
        matrices.pushMatrix();

        int baseColor = hsvToRgb(hue, 1.0f, 1.0f);
        for (int i = 0; i < PICKER_SIZE; i++) {
            float s = (float) i / PICKER_SIZE;
            int gradColor = lerpColor(0xFFFFFFFF, 0xFF000000 | baseColor, s);
            context.fill(px + i, py, px + i + 1, py + PICKER_SIZE, gradColor);
        }
        for (int i = 0; i < PICKER_SIZE; i++) {
            float v = 1.0f - ((float) i / PICKER_SIZE);
            int alpha = (int) ((1.0f - v) * 255);
            int blackOverlay = (alpha << 24);
            context.fill(px, py + i, px + PICKER_SIZE, py + i + 1, blackOverlay);
        }


        int cursorX = px + (int) (saturation * PICKER_SIZE);
        int cursorY = py + (int) ((1.0f - value) * PICKER_SIZE);
        RenderUtils.drawRoundedRectWithOutline(context, cursorX - 2, cursorY - 2, 4, 4, 0.0f, 1, 0xFFFFFFFF);
        RenderUtils.drawRoundedRectWithOutline(context, cursorX - 3, cursorY - 3, 6, 6, 0.0f, 1, 0xFF000000);
        context.disableScissor();
        matrices.popMatrix();
    }

    /**
     * Renders the hue slider
     */
    private void renderHueSlider(DrawContext context, int sx, int sy, int mouseX, int mouseY) {
        for (int i = 0; i < PICKER_SIZE; i++) {
            float h = (float) i / PICKER_SIZE;
            int color = 0xFF000000 | hsvToRgb(h, 1.0f, 1.0f);
            context.fill(sx, sy + i, sx + SLIDER_WIDTH, sy + i + 1, color);
        }
        renderSliderHandle(context, sx, sy, mouseX, mouseY, hue, false);
    }

    private void renderOpacitySlider(DrawContext context, int sx, int sy, int mouseX, int mouseY) {
        if(!hasOpacityControl)return;

        context.fill(sx, sy, sx + SLIDER_WIDTH, sy + PICKER_SIZE, 0xFFFFFFFF);

        int baseColor = colorGetter.get();

        for (int i = 0; i < PICKER_SIZE; i++) {
            float alpha = 1.0f - ((float) i / PICKER_SIZE);
            int color = config.colorWithAlpha(baseColor, alpha);
            context.fill(sx, sy + i, sx + SLIDER_WIDTH, sy + i + 1, color);
        }

        renderSliderHandle(context, sx, sy, mouseX, mouseY, opacity, true);
    }

    /**
     *
     * @param context
     * @param sx
     * @param sy
     * @param mouseX
     * @param mouseY
     * @param variable the value that should be changed, either hue or opacity
     */
    private void renderSliderHandle(DrawContext context, int sx, int sy, int mouseX, int mouseY, float variable, boolean reverse) {
        int handleY;
        if (reverse){
            handleY = sy + (int) ((1.0f - opacity) * PICKER_SIZE) - HANDLE_HEIGHT / 2;
        }else{
            handleY = sy + (int) (variable * PICKER_SIZE) - HANDLE_HEIGHT / 2;
        }

        handleY = Math.max(sy - HANDLE_HEIGHT / 2, Math.min(handleY, sy + PICKER_SIZE - HANDLE_HEIGHT / 2));
        int handleX = sx + (SLIDER_WIDTH - HANDLE_WIDTH) / 2;

        boolean hovered = mouseX >= sx - 5 && mouseX <= sx + SLIDER_WIDTH + 5 &&
                mouseY >= handleY && mouseY <= handleY + HANDLE_HEIGHT;

        int handleColor = hovered || draggingHue ? config.guicolors.sliderHandleHovered : config.guicolors.sliderHandle;
        RenderUtils.drawRectWithCutCorners(context, handleX, handleY, HANDLE_WIDTH, HANDLE_HEIGHT, 2, handleColor);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (popupOpen) {
            if (isMouseOverCloseButton(mouseX, mouseY)) {
                popupOpen = false;
                return true;
            }

            int paddedX = popupX - CLICK_BLOCK_PADDING;
            int paddedY = popupY - CLICK_BLOCK_PADDING;
            int paddedWidth = POPUP_WIDTH + (CLICK_BLOCK_PADDING * 2);
            int paddedHeight = POPUP_HEIGHT + (CLICK_BLOCK_PADDING * 2);

            if (mouseX >= paddedX && mouseX <= paddedX + paddedWidth && mouseY >= paddedY && mouseY <= paddedY + paddedHeight) {
                return handlePopupClick(mouseX, mouseY);
            } else {
                popupOpen = false;
                return true;
            }
        }

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            popupOpen = true;
            return true;
        }

        return false;
    }

    /**
     * Handles mouse clicks within the popup area
     */
    private boolean handlePopupClick(double mouseX, double mouseY) {
        int contentY = popupY + TITLE_BAR_HEIGHT + 15;
        int pickerX = popupX + PADDING;
        int hueSliderX = pickerX + PICKER_SIZE + SLIDER_SPACING;
        int opacitySliderX = hueSliderX + SLIDER_WIDTH + SLIDER_SPACING;

        if (mouseX >= pickerX && mouseX <= pickerX + PICKER_SIZE && mouseY >= contentY && mouseY <= contentY + PICKER_SIZE) {
            draggingSV = true;
            updateSVFromMouse(mouseX, mouseY, pickerX, contentY);
            return true;
        }

        if (mouseX >= hueSliderX && mouseX <= hueSliderX + SLIDER_WIDTH && mouseY >= contentY && mouseY <= contentY + PICKER_SIZE) {
            draggingHue = true;
            updateHueFromMouse(mouseY, contentY);
            return true;
        }

        if (hasOpacityControl && mouseX >= opacitySliderX && mouseX <= opacitySliderX + SLIDER_WIDTH && mouseY >= contentY && mouseY <= contentY + PICKER_SIZE) {
            draggingOpacity = true;
            updateOpacityFromMouse(mouseY, contentY);
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!popupOpen || button != 0) return false;

        int contentY = popupY + TITLE_BAR_HEIGHT + 15;
        int pickerX = popupX + PADDING;

        if (draggingSV) {
            updateSVFromMouse(mouseX, mouseY, pickerX, contentY);
            return true;
        }

        if (draggingHue) {
            updateHueFromMouse(mouseY, contentY);
            return true;
        }

        if(draggingOpacity){
            updateOpacityFromMouse(mouseY, contentY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (draggingSV || draggingHue) {
            draggingSV = false;
            draggingHue = false;
            draggingOpacity = false;
            return true;
        }

        return false;
    }

    private void updateOpacityFromMouse(double mouseY, int contentY) {
        opacity = Math.max(0, Math.min(1, 1.0f - (float) (mouseY - contentY) / PICKER_SIZE)); // Inverted!
        opacitySetter.accept(opacity);
    }

    /**
     * Updates saturation and value based on mouse position
     */
    private void updateSVFromMouse(double mouseX, double mouseY, int pickerX, int contentY) {
        saturation = Math.max(0, Math.min(1, (float) (mouseX - pickerX) / PICKER_SIZE));
        value = Math.max(0, Math.min(1, 1.0f - (float) (mouseY - contentY) / PICKER_SIZE));

        int newColor = hsvToRgb(hue, saturation, value);
        colorSetter.accept(newColor);
    }

    /**
     * Updates hue based on mouse position
     */
    private void updateHueFromMouse(double mouseY, int contentY) {
        hue = Math.max(0, Math.min(1, (float) (mouseY - contentY) / PICKER_SIZE));

        int newColor = hsvToRgb(hue, saturation, value);
        colorSetter.accept(newColor);
    }

    /**
     * Linearly interpolates between two colors
     */
    private int lerpColor(int from, int to, float t) {
        int aFrom = (from >> 24) & 0xFF;
        int rFrom = (from >> 16) & 0xFF;
        int gFrom = (from >> 8) & 0xFF;
        int bFrom = from & 0xFF;

        int aTo = (to >> 24) & 0xFF;
        int rTo = (to >> 16) & 0xFF;
        int gTo = (to >> 8) & 0xFF;
        int bTo = to & 0xFF;

        int a = (int) (aFrom + (aTo - aFrom) * t);
        int r = (int) (rFrom + (rTo - rFrom) * t);
        int g = (int) (gFrom + (gTo - gFrom) * t);
        int b = (int) (bFrom + (bTo - bFrom) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Converts RGB color to HSV color space
     */
    private static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0;
        if (delta != 0) {
            if (max == r) h = ((g - b) / delta) % 6;
            else if (max == g) h = ((b - r) / delta) + 2;
            else h = ((r - g) / delta) + 4;
            h /= 6;
            if (h < 0) h += 1;
        }

        float s = (max == 0) ? 0 : delta / max;
        float v = max;

        return new float[]{h, s, v};
    }

    /**
     * Converts HSV color to RGB color space
     */
    private static int hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
            default: r = g = b = 0;
        }

        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);

        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Checks if the popup is currently open
     */
    public boolean isPopupOpen() {
        return popupOpen;
    }

    /**
     * Closes the color picker popup
     */
    public void closePopup() {
        this.popupOpen = false;
    }




}
