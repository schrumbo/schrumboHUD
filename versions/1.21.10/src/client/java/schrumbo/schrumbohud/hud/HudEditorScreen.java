package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;

/**
 * interactive screen for positioning and scaling the HUD
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private static final int SLOT_SIZE = 18;
    private static final int ROW_SLOTS = 9;
    private static final int ROWS = 3;
    private static final int PADDING = 4;
    private static final int BASE_WIDTH = ROW_SLOTS * SLOT_SIZE + PADDING * 2;
    private static final int BASE_HEIGHT = ROWS * SLOT_SIZE + PADDING * 2;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var config = SchrumboHUDClient.config;

        renderAlignmentGuides(context, config);
        renderInstructions(context);
    }

    private void renderAlignmentGuides(DrawContext context, HudConfig config) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int guideColor = 0x80FFFFFF;

        context.fill(centerX, 0, centerX + 1, this.height, guideColor);
        context.fill(0, centerY, this.width, centerY + 1, guideColor);

        context.fill(0, 0, 2, this.height, 0x40FFFFFF);
        context.fill(0, 0, this.width, 2, 0x40FFFFFF);
        context.fill(this.width - 2, 0, this.width, this.height, 0x40FFFFFF);
        context.fill(0, this.height - 2, this.width, this.height, 0x40FFFFFF);
    }

    private void renderInstructions(DrawContext context) {
        String[] instructions = {
                "§e[Drag]§r Move HUD",
                "§e[Scroll]§r Resize (0.1x - 5.0x)",
                "§e[R]§r Reset Position",
                "§e[ESC]§r Save & Exit"
        };

        int y = 10;
        for (String instruction : instructions) {
            context.drawText(textRenderer, Text.literal(instruction),
                    10, y, 0xFFFFFFFF, true);
            y += 12;
        }
    }

    private int getX(HudConfig config) {
        return config.position.x;
    }

    private int getY(HudConfig config) {
        return config.position.y;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var config = SchrumboHUDClient.config;
            int hudWidth = (int)(BASE_WIDTH * config.scale);
            int hudHeight = (int)(BASE_HEIGHT * config.scale);
            int hudX = getX(config);
            int hudY = getY(config);

            double mouseX = click.x();
            double mouseY = click.y();

            if (mouseX >= hudX && mouseX <= hudX + hudWidth &&
                    mouseY >= hudY && mouseY <= hudY + hudHeight) {
                dragging = true;
                dragOffsetX = (int)mouseX - hudX;
                dragOffsetY = (int)mouseY - hudY;

                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var config = SchrumboHUDClient.config;

            int targetX = (int)click.x() - dragOffsetX;
            int targetY = (int)click.y() - dragOffsetY;

            config.position.x = targetX;
            config.position.y = targetY;

            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        var config = SchrumboHUDClient.config;
        int hudWidth = (int)(BASE_WIDTH * config.scale);
        int hudHeight = (int)(BASE_HEIGHT * config.scale);
        int hudX = getX(config);
        int hudY = getY(config);

        if (mouseX >= hudX && mouseX <= hudX + hudWidth &&
                mouseY >= hudY && mouseY <= hudY + hudHeight) {

            float delta = (float)verticalAmount * 0.1f;
            config.scale = Math.max(0.1f, Math.min(5.0f, config.scale + delta));

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        var config = SchrumboHUDClient.config;

        if (input.key() == GLFW.GLFW_KEY_R) {
            config.position.x = 10;
            config.position.y = 10;
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            ConfigManager.save();
            this.close();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void close() {
        ConfigManager.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
