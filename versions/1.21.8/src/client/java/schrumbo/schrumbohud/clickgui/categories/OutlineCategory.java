package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class OutlineCategory extends Category {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public OutlineCategory() {
        super("Outline");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;
        widgets.add(new ToggleWidget(
                startX, currentY, width, "Toggle Outline",
                () -> config.outlineEnabled,
                val -> config.outlineEnabled = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ColorPickerWidget(
                startX, currentY, width,
                "Border Color",
                () -> config.colors.border,
                config::setBorderColor,
                () -> config.outlineOpacity,
                config::setBorderOpacity)
        );
    }

}
