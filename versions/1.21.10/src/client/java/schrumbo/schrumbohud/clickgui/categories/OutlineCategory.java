package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

import java.awt.*;

public class OutlineCategory extends Category {

    private final MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public OutlineCategory() {
        super("Outline");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ToggleWidget toggleOutline = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Toggle Outline")
                .value(()->config.outlineEnabled, config::enableBorder)
                .build();
        widgets.add(toggleOutline);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ColorPickerWidget borderColorPicker = ColorPickerWidget.builder()
                .y(currentY)
                .width(width)
                .label("Border Color")
                .color(() -> config.colors.border, config::setBorderColor)
                .opacity(() -> config.outlineOpacity, config::setBorderOpacity)
                .build();
        widgets.add(borderColorPicker);
    }

}
