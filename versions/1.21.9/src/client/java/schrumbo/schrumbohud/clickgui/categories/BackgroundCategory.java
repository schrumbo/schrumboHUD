package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class BackgroundCategory extends Category {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;



    public BackgroundCategory() {
        super("Background");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;
        widgets.add(new ToggleWidget(
                startX, currentY, width, "Toggle Background",
                () -> config.backgroundEnabled,
                val -> config.backgroundEnabled = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ColorPickerWidget(
                startX, currentY, width,
                "Background Color",
                () -> config.colors.background,
                config::setBackgroundColor,
                () -> config.backgroundOpacity,
                config::setBackgroundOpacity)
        );
    }


}
