package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class PositionCategory extends Category {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public PositionCategory() {
        super("Size and Position");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;
        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Reset Position",
                () -> {
                    SchrumboHUDClient.config.position.x = 10;
                    SchrumboHUDClient.config.position.y = 10;
                    ConfigManager.save();
                }
        ));

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Change Position",
                () -> {
                    MinecraftClient.getInstance().setScreen(
                            new HudEditorScreen(MinecraftClient.getInstance().currentScreen)
                    );
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new SliderWidget(
                startX, currentY, width, "HUD Scale",
                0.1f, 5.0f, "x",
                () -> config.scale,
                val -> config.scale = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

    }

}
