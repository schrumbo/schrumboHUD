package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class PresetsCategory extends Category {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public PresetsCategory() {
        super("Presets");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Catppuccin Mocha",
                () -> {
                    SchrumboHUDClient.config.loadCatppuccinMocha();
                    ConfigManager.save();
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Gruvbox",
                () -> {
                    SchrumboHUDClient.config.loadGruvbox();
                    ConfigManager.save();
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Monokai",
                () -> {
                    SchrumboHUDClient.config.loadMonokai();
                    ConfigManager.save();
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Dracula",
                () -> {
                    SchrumboHUDClient.config.loadDracula();
                    ConfigManager.save();
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

    }

}
