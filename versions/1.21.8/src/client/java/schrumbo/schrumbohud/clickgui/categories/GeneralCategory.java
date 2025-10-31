package schrumbo.schrumbohud.clickgui.categories;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.ToggleWidget;
import schrumbo.schrumbohud.config.HudConfig;

public class GeneralCategory extends Category {

    private final MinecraftClient client = MinecraftClient.getInstance();
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
                (color) -> config.setAccentColor(color)
        ));

    }

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY) {

        boolean hovered = isHeaderHovered(mouseX, mouseY);

        int bgColor = hovered ? config.guicolors.widgetBackground : config.guicolors.widgetBackgroundHovered;
        RenderUtils.fillRoundedRect(context, x, y, width, HEADER_HEIGHT, 0.0f, bgColor);


        RenderUtils.fillRoundedRect(context, x, y, width, 3, 0.0f, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetAccentOpacity));

        int textY = y + (HEADER_HEIGHT - 8) / 2;
        context.drawText(client.textRenderer, Text.literal(name),
                x + PADDING, textY, config.guicolors.text, true);

        String indicator = collapsed ? "▶" : "▼";
        int indicatorX = x + width - PADDING - client.textRenderer.getWidth(indicator);
        context.drawText(client.textRenderer, Text.literal(indicator),
                indicatorX, textY, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetAccentOpacity), false);
    }
}
