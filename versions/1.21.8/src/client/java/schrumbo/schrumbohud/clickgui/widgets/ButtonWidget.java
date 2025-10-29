package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.gui.DrawContext;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;

public class ButtonWidget extends Widget {
    private final Runnable onClick;
    private boolean isPressed = false;
    private long lastClickTime = 0;
    private static final long CLICK_COOLDOWN_MS = 150; // Anti-spam

    public ButtonWidget(int x, int y, int width, String label, Runnable onClick) {
        super(x, y, width, 25, label);
        this.onClick = onClick;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var config = SchrumboHUDClient.config;
        boolean hovered = isHovered(mouseX, mouseY);

        int bgColor;
        if (isPressed) {
            bgColor = config.getColorWithAlpha(0x1a1a1a, 0.95f);
        } else if (hovered) {
            bgColor = config.getColorWithAlpha(0x3a3a3a, 0.9f);
        } else {
            bgColor = config.getColorWithAlpha(0x2a2a2a, 0.85f);
        }

        RenderUtils.fillRoundedRect(context, x, y, width, height, 0.0f, bgColor);

        if (hovered) {
            int borderColor = config.getColorWithAlpha(config.colors.accent, 0.6f);
            RenderUtils.drawRoundedRectWithOutline(context, x, y, width, height, 0.0f, 1, borderColor);
        }

        var client = net.minecraft.client.MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int labelX = x + (width - labelWidth) / 2;
        int labelY = y + (height - client.textRenderer.fontHeight) / 2;

        int textColor = hovered ? config.colors.accent : 0xFFFFFF;
        context.drawText(client.textRenderer,
                net.minecraft.text.Text.literal(label),
                labelX, labelY,
                textColor, true);
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
                e.printStackTrace();
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

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }
}
