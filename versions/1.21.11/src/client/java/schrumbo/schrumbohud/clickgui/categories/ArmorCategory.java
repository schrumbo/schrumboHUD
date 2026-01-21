package schrumbo.schrumbohud.clickgui.categories;

import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class ArmorCategory extends Category {
    private final HudConfig config = SchrumboHUDClient.config;

    public ArmorCategory() {
        super("Armor HUD");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ToggleWidget armorToggle = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Armor HUD")
                .value(() -> config.armorEnabled, config::enableArmorHud)
                .build();
        widgets.add(armorToggle);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ToggleWidget verticalMode = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Vertical")
                .value(() -> config.armorVertical, config::enableVerticalMode)
                .build();
        widgets.add(verticalMode);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        SliderWidget transparency = SliderWidget.builder()
                .value(config::getArmorOpacity, config::setArmorOpacity)
                .label("Transparency")
                .suffix("x")
                .range(0.0f, 1.0f)
                .width(width)
                .y(currentY)
                .build();
        widgets.add(transparency);
        updateWidgetPositions(startX, startY);
    }

}