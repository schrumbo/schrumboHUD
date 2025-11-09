package schrumbo.schrumbohud.clickgui.categories;

import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class SlotCategory extends Category {
    private final HudConfig config = SchrumboHUDClient.config;

    public SlotCategory() {
        super("Item Slots");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ToggleWidget toggleSlotBackground = ToggleWidget.builder()
                .y(currentY)
                .width(width)
                .label("Toggle Slot Background")
                .value(()->config.slotBackgroundEnabled, config::enableSlotBackground)
                .build();
        widgets.add(toggleSlotBackground);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ColorPickerWidget slotBackgroundColorPicker = ColorPickerWidget.builder()
                .y(currentY)
                .width(width)
                .label("Slot Background Color")
                .color(() -> config.colors.slots, config::setSlotColor)
                .opacity(() -> config.slotBackgroundOpacity, config::setSlotOpacity)
                .build();
        widgets.add(slotBackgroundColorPicker);
        updateWidgetPositions(startX, startY);
    }
}
