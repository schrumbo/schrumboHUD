package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToggleWidget extends Widget {
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    private static final int TOGGLE_WIDTH = 40;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int KNOB_SIZE = 16;

    public ToggleWidget(int x, int y, int width, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(x, y, width, 25, label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var config = SchrumboHUDClient.config;
        var client = MinecraftClient.getInstance();

        hovered = isHovered(mouseX, mouseY);
        boolean enabled = getter.get();

        int bgColor = config.getColorWithAlpha(0x1a1a1a, 0.8f);
        RenderUtils.fillRoundedRect(context, x, y, width, height, 0.0f, bgColor);

        context.drawText(client.textRenderer, label, x, y + 7, 0xFFFFFF, false);

        int toggleX = x + width - TOGGLE_WIDTH;
        int toggleY = y + 2;

        int buttonBgColor = enabled ? config.getColorWithAlpha(config.colors.accent, 0.5f) : config.getColorWithAlpha(0x404040, 0.8f);
        RenderUtils.fillRoundedRect(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, 0.5f, buttonBgColor);

        int knobX = enabled ? toggleX + TOGGLE_WIDTH - KNOB_SIZE - 2 : toggleX + 2;
        int knobY = toggleY + 2;

        int knobColor = enabled ? config.getColorWithAlpha(config.colors.accent, 1.0f) : config.getColorWithAlpha(0x808080, 1.0f);

        RenderUtils.fillRoundedRect(context, knobX, knobY, KNOB_SIZE, KNOB_SIZE, 0.5f, knobColor);

        if (hovered) {
            int hoverColor = config.getColorWithAlpha(0xFFFFFF, 0.1f);
            RenderUtils.fillRoundedRect(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, 0.5f, hoverColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered((int) mouseX, (int) mouseY)) {
            setter.accept(!getter.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}
