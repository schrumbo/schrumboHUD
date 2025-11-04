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

public class GeneralCategory extends Category {

    private final MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public GeneralCategory() {
        super("General");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        widgets.add(new ToggleWidget(
                startX, currentY, width, "HUD Enabled",
                () -> config.enabled,
                val -> config.enabled = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        widgets.add(new ToggleWidget(
                startX, currentY, width, "Rounded Corners",
                () -> config.roundedCorners,
                val -> config.roundedCorners = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ColorPickerWidget(
                startX, currentY, width,
                "ClickGUI Accent Color",
                () -> config.guicolors.accent,
                config::setAccentColor
        ));


    }

}
