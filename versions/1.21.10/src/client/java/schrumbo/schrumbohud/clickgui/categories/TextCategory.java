package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.SliderWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class TextCategory extends Category {

    private final MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;

    public TextCategory() {
        super("Text");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;
        widgets.add(new ToggleWidget(
                startX, currentY, width, "Toggle Text Shadow",
                () -> config.textShadowEnabled,
                val -> config.textShadowEnabled = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        widgets.add(new SliderWidget(
                startX, currentY, width, "Text Shadow Opacity",
                0.0f, 1.0f, "x",
                () -> config.textShadowOpacity,
                val -> config.textShadowOpacity = val
        ));
        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;
        widgets.add(new ColorPickerWidget(
                startX, currentY, width,
                "Text Color",
                () -> config.colors.text,
                (color) -> config.setTextColor(color)
        ));
    }

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY) {

        boolean hovered = isHeaderHovered(mouseX, mouseY);

        int bgColor = hovered ? config.cgcolors.widgetBackground : config.cgcolors.widgetBackgroundHovered;
        RenderUtils.fillRoundedRect(context, x, y, width, HEADER_HEIGHT, 0.0f, bgColor);

        RenderUtils.fillRoundedRect(context, x, y, width, 3, 0.0f, config.cgcolors.accent80);

        int textY = y + (HEADER_HEIGHT - 8) / 2;
        context.drawText(client.textRenderer, Text.literal(name),
                x + PADDING, textY, config.cgcolors.text, true);

        String indicator = collapsed ? "▶" : "▼";
        int indicatorX = x + width - PADDING - client.textRenderer.getWidth(indicator);
        context.drawText(client.textRenderer, Text.literal(indicator),
                indicatorX, textY, config.cgcolors.accent80, false);
    }
}
