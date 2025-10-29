package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class PresetsCategory extends Category {

    public PresetsCategory() {
        super("Presets");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {
        var config = SchrumboHUDClient.config;

        int currentY = startY;

        widgets.add(new ButtonWidget(
                startX, currentY, width,
                "Classic InventoryHUD+",
                () -> {
                    SchrumboHUDClient.config.loadClassicInventoryHUD();
                    ConfigManager.save();
                }
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

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

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY) {
        var config = SchrumboHUDClient.config;
        var client = MinecraftClient.getInstance();

        boolean hovered = isHeaderHovered(mouseX, mouseY);

        int bgColor = config.getColorWithAlpha(
                hovered ? 0x2a2a2a : 0x1f1f1f,
                0.9f
        );
        RenderUtils.fillRoundedRect(context, x, y, width, HEADER_HEIGHT, 0.0f, bgColor);

        int accentColor = config.getColorWithAlpha(config.colors.accent, 0.8f);
        RenderUtils.fillRoundedRect(context, x, y, width, 3, 0.0f, accentColor);

        int textY = y + (HEADER_HEIGHT - 8) / 2;
        context.drawText(client.textRenderer, Text.literal(name),
                x + PADDING, textY, 0xFFFFFF, true);

        String indicator = collapsed ? "▶" : "▼";
        int indicatorX = x + width - PADDING - client.textRenderer.getWidth(indicator);
        context.drawText(client.textRenderer, Text.literal(indicator),
                indicatorX, textY, accentColor, false);
    }
}
