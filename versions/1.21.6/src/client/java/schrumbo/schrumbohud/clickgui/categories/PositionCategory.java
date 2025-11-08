package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class PositionCategory extends Category {


    public PositionCategory() {
        super("Position");
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
        updateWidgetPositions(startX, startY);
    }

}
