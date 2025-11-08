package schrumbo.schrumbohud.clickgui.categories;

import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class TextCategory extends Category {

    private final HudConfig config = SchrumboHUDClient.config;

    public TextCategory() {
        super("Text");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ToggleWidget toggleTextShadow = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Toggle Text Shadow")
                .value(()-> config.textShadowEnabled, config::enableTextShadow)
                .build();
        widgets.add(toggleTextShadow);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ColorPickerWidget textColorPicker = ColorPickerWidget.builder()
                .y(currentY)
                .width(width)
                .label("Text Color")
                .color(() -> config.colors.text, config::setTextColor)
                .build();
        widgets.add(textColorPicker);
        updateWidgetPositions(startX, startY);
    }

}
