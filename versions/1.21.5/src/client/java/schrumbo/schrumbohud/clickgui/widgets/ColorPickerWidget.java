package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Inline color picker with HSV controls and opacity slider
 */
public class ColorPickerWidget extends Widget {
    private final Supplier<Integer> colorGetter;
    private final Consumer<Integer> colorSetter;
    private final Supplier<Float> opacityGetter;
    private final Consumer<Float> opacitySetter;
    private final boolean hasOpacityControl;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final HudConfig config = SchrumboHUDClient.config;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float value = 1.0f;
    private float opacity = 1.0f;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingOpacity = false;


    private static final int SV_PICKER_SIZE = 120;
    private static final int SLIDER_WIDTH = 15;
    private static final int SLIDER_SPACING = 10;
    private static final int CONTENT_PADDING = 10;

    private ColorPickerWidget(Builder builder) {
        super(builder);
        this.colorGetter = builder.colorGetter;
        this.colorSetter = builder.colorSetter;
        this.opacityGetter = builder.opacityGetter;
        this.opacitySetter = builder.opacitySetter;
        this.hasOpacityControl = builder.hasOpacityControl;

        syncFromConfig();

        this.height = 45 + SV_PICKER_SIZE + CONTENT_PADDING;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);

        RenderUtils.fillRoundedRect(context, x, y, width, height, 0f, config.guicolors.widgetBackground);

        int labelX = x + PADDING;
        int currentY = y - client.textRenderer.fontHeight + 45 / 2;

        int textColor = hovered ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(labelX, currentY, 1.0f);
        matrices.scale(config.guicolors.textSize, config.guicolors.textSize, 1.0f);
        context.drawText(client.textRenderer, Text.literal(label), 0, 0, textColor, true);
        matrices.pop();

        currentY = y + 45;
        int currentX = x + PADDING;

        renderSVPicker(context, currentX, currentY, mouseX, mouseY);
        currentX += SV_PICKER_SIZE + SLIDER_SPACING;

        renderHueSlider(context, currentX, currentY, mouseX, mouseY);
        currentX += SLIDER_WIDTH + SLIDER_SPACING;

        if (hasOpacityControl) {
            renderOpacitySlider(context, currentX, currentY, mouseX, mouseY);
            currentX += SLIDER_WIDTH + SLIDER_SPACING;
        }
    }

    private void renderSVPicker(DrawContext context, int px, int py, int mouseX, int mouseY) {
        var matrices = context.getMatrices();



        context.enableScissor(px, py, px + SV_PICKER_SIZE, py + SV_PICKER_SIZE);
        matrices.push();

        int baseColor = hsvToRgb(hue, 1.0f, 1.0f);
        for (int i = 0; i < SV_PICKER_SIZE; i++) {
            float s = (float) i / SV_PICKER_SIZE;
            int gradColor = lerpColor(0xFFFFFFFF, 0xFF000000 | baseColor, s);
            context.fill(px + i, py, px + i + 1, py + SV_PICKER_SIZE, gradColor);
        }
        for (int i = 0; i < SV_PICKER_SIZE; i++) {
            float v = 1.0f - ((float) i / SV_PICKER_SIZE);
            int alpha = (int) ((1.0f - v) * 255);
            int blackOverlay = (alpha << 24);
            context.fill(px, py + i, px + SV_PICKER_SIZE, py + i + 1, blackOverlay);
        }

        RenderUtils.drawRoundedRectWithOutline(context, px, py, SV_PICKER_SIZE, SV_PICKER_SIZE, 0f, 1, 0xFF000000);

        int cursorX = px + (int) (saturation * SV_PICKER_SIZE);
        int cursorY = py + (int) ((1.0f - value) * SV_PICKER_SIZE);
        RenderUtils.drawRoundedRectWithOutline(context, cursorX - 2, cursorY - 2, 4, 4, 0.0f, 1, 0xFFFFFFFF);
        RenderUtils.drawRoundedRectWithOutline(context, cursorX - 3, cursorY - 3, 6, 6, 0.0f, 1, 0xFF000000);
        context.disableScissor();
        matrices.pop();

    }

    private void renderHueSlider(DrawContext context, int sx, int sy, int mouseX, int mouseY) {
        for (int i = 0; i < SV_PICKER_SIZE; i++) {
            float h = (float) i / SV_PICKER_SIZE;
            int color = hsvToRgb(h, 1.0f, 1.0f);
            context.fill(sx, sy + i, sx + SLIDER_WIDTH, sy + i + 1, 0xFF000000 | color);
        }

        RenderUtils.drawRoundedRectWithOutline(context, sx, sy, SLIDER_WIDTH, SV_PICKER_SIZE, 0f, 1, 0xFF000000);

        int handleY = sy + (int) (hue * SV_PICKER_SIZE);
        boolean sliderHovered = isHoveringSlider(mouseX, mouseY, sx, sy, SLIDER_WIDTH, SV_PICKER_SIZE);
        renderSliderHandle(context, sx, handleY, SLIDER_WIDTH, sliderHovered || draggingHue);
    }

    private void renderOpacitySlider(DrawContext context, int sx, int sy, int mouseX, int mouseY) {
        context.fill(sx, sy, sx + SLIDER_WIDTH, sy + SV_PICKER_SIZE, 0xFFFFFFFF);

        int baseColor = colorGetter.get();
        for (int i = 0; i < SV_PICKER_SIZE; i++) {
            float alpha = 1.0f - ((float) i / SV_PICKER_SIZE);
            int color = config.colorWithAlpha(baseColor, alpha);
            context.fill(sx, sy + i, sx + SLIDER_WIDTH, sy + i + 1, color);
        }

        RenderUtils.drawRoundedRectWithOutline(context, sx, sy, SLIDER_WIDTH, SV_PICKER_SIZE, 0f, 1, 0xFF000000);

        int handleY = sy + (int) ((1.0f - opacity) * SV_PICKER_SIZE);
        boolean sliderHovered = isHoveringSlider(mouseX, mouseY, sx, sy, SLIDER_WIDTH, SV_PICKER_SIZE);
        renderSliderHandle(context, sx, handleY, SLIDER_WIDTH, sliderHovered || draggingOpacity);
    }

    private void renderSliderHandle(DrawContext context, int x, int y, int width, boolean hovered) {
        int handleColor = hovered ? config.guicolors.sliderHandleHovered : config.guicolors.sliderHandle;
        int handleHeight = 6;

        RenderUtils.fillRoundedRect(context, x - 2, y - handleHeight / 2, width + 4, handleHeight, 0f, handleColor);
        RenderUtils.drawRoundedRectWithOutline(context, x - 2, y - handleHeight / 2, width + 4, handleHeight, 0f, 1, 0xFF000000);
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (!isHovered((int) mouseX, (int) mouseY)) return false;

        int contentY = y + 45;
        int currentX = x + CONTENT_PADDING;

        if (mouseX >= currentX && mouseX <= currentX + SV_PICKER_SIZE &&
                mouseY >= contentY && mouseY <= contentY + SV_PICKER_SIZE) {
            draggingSV = true;
            updateSVFromMouse(mouseX, mouseY, currentX, contentY);
            return true;
        }
        currentX += SV_PICKER_SIZE + SLIDER_SPACING;

        if (isHoveringSlider(mouseX, mouseY, currentX, contentY, SLIDER_WIDTH, SV_PICKER_SIZE)) {
            draggingHue = true;
            updateHueFromMouse(mouseY, contentY);
            return true;
        }
        currentX += SLIDER_WIDTH + SLIDER_SPACING;

        if (hasOpacityControl && isHoveringSlider(mouseX, mouseY, currentX, contentY, SLIDER_WIDTH, SV_PICKER_SIZE)) {
            draggingOpacity = true;
            updateOpacityFromMouse(mouseY, contentY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) return false;

        int contentY = y + 45;
        int pickerX = x + CONTENT_PADDING;

        if (draggingSV) {
            updateSVFromMouse(mouseX, mouseY, pickerX, contentY);
            return true;
        }

        if (draggingHue) {
            updateHueFromMouse(mouseY, contentY);
            return true;
        }

        if (draggingOpacity) {
            updateOpacityFromMouse(mouseY, contentY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingSV || draggingHue || draggingOpacity)) {
            draggingSV = false;
            draggingHue = false;
            draggingOpacity = false;
            return true;
        }
        return false;
    }

    private void updateSVFromMouse(double mouseX, double mouseY, int pickerX, int contentY) {
        saturation = Math.max(0, Math.min(1, (float) (mouseX - pickerX) / SV_PICKER_SIZE));
        value = Math.max(0, Math.min(1, 1.0f - (float) (mouseY - contentY) / SV_PICKER_SIZE));
        applyColor();
    }

    private void updateHueFromMouse(double mouseY, int contentY) {
        hue = Math.max(0, Math.min(1, (float) (mouseY - contentY) / SV_PICKER_SIZE));
        applyColor();
    }

    private void updateOpacityFromMouse(double mouseY, int contentY) {
        opacity = Math.max(0, Math.min(1, 1.0f - (float) (mouseY - contentY) / SV_PICKER_SIZE));
        opacitySetter.accept(opacity);
    }

    private void applyColor() {
        int newColor = hsvToRgb(hue, saturation, value);
        colorSetter.accept(newColor);
    }

    private void syncFromConfig() {
        int color = colorGetter.get();
        opacity = opacityGetter.get();

        float[] hsv = rgbToHsv(color);
        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
    }

    private boolean isHoveringSlider(double mouseX, double mouseY, int sx, int sy, int width, int height) {
        return mouseX >= sx && mouseX <= sx + width && mouseY >= sy && mouseY <= sy + height;
    }

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

        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

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
}
