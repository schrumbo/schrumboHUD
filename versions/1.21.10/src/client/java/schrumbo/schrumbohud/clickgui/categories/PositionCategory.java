package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUD;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class PositionCategory extends Category {

    private final MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public PositionCategory() {
        super("Size and Position");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ButtonWidget resetPos = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Reset Position")
                .onClick(() -> {
                    SchrumboHUDClient.config.position.x = 10;
                    SchrumboHUDClient.config.position.y = 10;
                    SchrumboHUDClient.config.anchor.horizontal = HudConfig.HorizontalAnchor.LEFT;
                    SchrumboHUDClient.config.anchor.vertical = HudConfig.VerticalAnchor.TOP;
                    ConfigManager.save();
                })
                .build();
        widgets.add(resetPos);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ButtonWidget changePos = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Change Position")
                .onClick(()->{
                    MinecraftClient.getInstance().setScreen(new HudEditorScreen((MinecraftClient.getInstance().currentScreen)));
                })
                .build();
        widgets.add(changePos);

    }

}
