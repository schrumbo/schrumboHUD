package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;


public class SliderWidget extends Widget {
    private final String label;
    private final float min;
    private final float max;
    private final String suffix;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;

    private boolean isDragging = false;

    private static final int SLIDER_HEIGHT = 50;
    private static final int HANDLE_WIDTH = 8;
    private static final int HANDLE_HEIGHT = 16;
    private static final int TRACK_PADDING = 8;
    private static final int TRACK_HEIGHT = 4;
    private static final int PADDING = 7;

    private final HudConfig config = SchrumboHUDClient.config;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public SliderWidget(int x, int y, int width, String label, float min, float max, String suffix, Supplier<Float> getter, Consumer<Float> setter) {
        super(x, y, width, SLIDER_HEIGHT, label);
        this.label = label;
        this.min = min;
        this.max = max;
        this.suffix = suffix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);

        RenderUtils.fillRoundedRect(context, x, y, width, height, 0.0f, config.guicolors.widgetBackground);

        int labelX = x + PADDING;
        int labelY = y + PADDING;

        int textColor = hovered ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(labelX, labelY, 1.0f);
        matrices.scale(config.guicolors.textSize, config.guicolors.textSize, 1.0f);
        context.drawText(client.textRenderer, Text.literal(label), 0, 0, textColor, true);
        matrices.pop();

        float currentValue = getter.get();
        float valueTextSize = 1.5f;
        String valueText = formatValue(currentValue);
        int valueWidth = (int) (client.textRenderer.getWidth(valueText) * valueTextSize);
        matrices.push();
        matrices.translate(x+ width - PADDING - valueWidth, y + PADDING, 1.0f);
        matrices.scale(valueTextSize, valueTextSize, 1.0f);

        context.drawText(client.textRenderer, Text.literal(valueText), 0, 0, config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity), true);
        matrices.pop();
        renderTrack(context, config, currentValue, mouseX, mouseY);
    }

    private String formatValue(float value) {
        float range = max - min;

        if (range <= 1.0f) {
            return String.format("%.2f%s", value, suffix);
        } else if (range <= 10.0f) {
            return String.format("%.1f%s", value, suffix);
        } else {
            return String.format("%.0f%s", value, suffix);
        }
    }

    private void renderTrack(DrawContext context, HudConfig config, float currentValue, int mouseX, int mouseY) {
        int trackX = x + TRACK_PADDING;
        int trackY = y + height - TRACK_PADDING - TRACK_HEIGHT - 4;
        int trackWidth = width - TRACK_PADDING * 2;

        int trackColor = config.colorWithAlpha(0x333333, 0.5f);
        RenderUtils.fillRoundedRect(context, trackX, trackY, trackWidth, TRACK_HEIGHT, 2.0f, trackColor);

        float percentage = (currentValue - min) / (max - min);
        int fillWidth = (int) (trackWidth * percentage);

        if (fillWidth > 0) {
            RenderUtils.fillRoundedRect(context, trackX, trackY, fillWidth, TRACK_HEIGHT, 2.0f, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetAccentOpacity));
        }

        int handleX = trackX + fillWidth - HANDLE_WIDTH / 2;
        int handleY = trackY + TRACK_HEIGHT / 2 - HANDLE_HEIGHT / 2;

        boolean hovered = isHovered(mouseX, mouseY);

        int handleColor = hovered || isDragging ? config.guicolors.sliderHandleHovered : config.guicolors.sliderHandle;

        RenderUtils.fillRoundedRect(context, handleX, handleY, HANDLE_WIDTH, HANDLE_HEIGHT, 3.0f, handleColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (button != 0) return false;

        if (isHovered((int) mouseX, (int) mouseY)) {
            isDragging = true;
            updateValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

        if (!isDragging || button != 0) return false;

        updateValueFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        if (button == 0 && isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }


    private void updateValueFromMouse(double mouseX) {
        int trackX = x + TRACK_PADDING;
        int trackWidth = width - TRACK_PADDING * 2;

        double clampedX = Math.max(trackX, Math.min(trackX + trackWidth, mouseX));

        float percentage = (float) ((clampedX - trackX) / trackWidth);
        float newValue = min + percentage * (max - min);

        newValue = roundValue(newValue);

        if (Math.abs(getter.get() - newValue) > 0.001f) {
            setter.accept(newValue);
        }
    }

    private float roundValue(float value) {
        float range = max - min;

        if (range <= 1.0f) {
            return Math.round(value * 100) / 100.0f;
        } else if (range <= 5.0f) {
            return Math.round(value * 10) / 10.0f;
        } else if (range <= 20.0f) {
            return Math.round(value * 2) / 2.0f;
        } else {
            return Math.round(value);
        }
    }
}
