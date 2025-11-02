package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class SlotCategory extends Category {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public SlotCategory() {
        super("Item Slots");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;
        widgets.add(new ToggleWidget(
                startX, currentY, width, "Toggle Slot Background",
                () -> config.slotBackgroundEnabled,
                val -> config.slotBackgroundEnabled = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        widgets.add(new SliderWidget(
                startX, currentY, width, "Opacity",
                0.0f, 1.0f, "x",
                () -> config.slotBackgroundOpacity,
                val -> config.slotBackgroundOpacity = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        widgets.add(new ColorPickerWidget(
                startX, currentY, width,
                "Slot Color",
                () -> config.colors.slots,
                (color) -> config.setSlotColor(color)
        ));
    }

}
