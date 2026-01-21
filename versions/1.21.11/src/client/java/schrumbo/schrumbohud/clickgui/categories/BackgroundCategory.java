package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.gui.Click;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class BackgroundCategory extends Category {
    private final HudConfig config = SchrumboHUDClient.config;

    public BackgroundCategory() {
        super("Background");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ToggleWidget toggleBackground = ToggleWidget.builder()
                .label("Background")
                .y(currentY)
                .width(width)
                .value(()->config.backgroundEnabled, config::enableBackground)
                .build();
        widgets.add(toggleBackground);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ColorPickerWidget backgroundColorPicker = ColorPickerWidget.builder()
                .color(() -> config.colors.background, config::setBackgroundColor)
                .opacity(() -> config.backgroundOpacity, config::setBackgroundOpacity)
                .label("Background Color")
                .y(currentY)
                .width(width)
                .build();
        widgets.add(backgroundColorPicker);
        updateWidgetPositions(startX, startY);
    }



}
