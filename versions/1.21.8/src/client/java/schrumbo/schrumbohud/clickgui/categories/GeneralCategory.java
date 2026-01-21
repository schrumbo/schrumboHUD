package schrumbo.schrumbohud.clickgui.categories;

import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class GeneralCategory extends Category {
    private final HudConfig config = SchrumboHUDClient.config;

    public GeneralCategory() {
        super("General");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;


        ToggleWidget hudToggle = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Inventory HUD")
                .value(() -> config.enabled, config::enableHud)
                .build();
        widgets.add(hudToggle);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ToggleWidget roundedCorners = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Round Corners")
                .value(() -> config.roundedCorners, config::enableRoundedCorners)
                .build();
        widgets.add(roundedCorners);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ColorPickerWidget accentColorPicker = ColorPickerWidget.builder()
                .width(width)
                .y(currentY)
                .label("Accent Color")
                .color(() -> config.guicolors.accent, config::setAccentColor)
                .build();
        widgets.add(accentColorPicker);
        updateWidgetPositions(startX, startY);
    }

}