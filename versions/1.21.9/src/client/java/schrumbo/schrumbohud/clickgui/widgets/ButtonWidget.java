package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

public class ButtonWidget extends Widget {
    private final Runnable onClick;
    private boolean isPressed = false;
    private long lastClickTime = 0;
    private static final long CLICK_COOLDOWN_MS = 150;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;


    private ButtonWidget(Builder builder){
        super(builder);
        this.onClick = builder.onClick;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        int bgColor;
        if (isPressed) {
            bgColor = config.guicolors.widgetBackgroundClicked;
        } else if (hovered) {
            bgColor = config.guicolors.widgetBackgroundHovered;
        } else {
            bgColor = config.guicolors.widgetBackground;
        }

        RenderUtils.fillRoundedRect(context, x, y, width, height, 0.0f, bgColor);

        if (hovered) {
            RenderUtils.drawRoundedRectWithOutline(context, x, y, width - 1, height, 0.0f, 1, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetBorderOpacity));
        }


        int labelWidth = (int) (client.textRenderer.getWidth(label) * config.guicolors.textSize);
        int labelX = x + (width - labelWidth) / 2;
        int labelY = y - client.textRenderer.fontHeight + height / 2;

        int textColor = hovered ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(labelX, labelY);
        matrices.scale(config.guicolors.textSize, config.guicolors.textSize);
        context.drawText(textRenderer, Text.literal(label), 0, 0, textColor, true);
        matrices.popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
                return true;
            }

            isPressed = true;
            lastClickTime = currentTime;

            try {
                onClick.run();
            } catch (Exception e) {
                SchrumboHUDClient.LOGGER.error(onClick.toString());
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isPressed) {
            isPressed = false;
            return true;
        }
        return false;
    }

    public static class Builder extends Widget.Builder<Builder> {
        private Runnable onClick;

        public Builder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ButtonWidget build() {
            return new ButtonWidget(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
