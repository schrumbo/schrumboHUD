package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;

import static schrumbo.schrumbohud.Utils.RenderUtils.drawBorder;


/**
 * interactive screen for positioning and scaling the HUD
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int tempAbsX = 0;
    private int tempAbsY = 0;

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
        renderHudPreview(context, config);
        renderInstructions(context);
    }

    private void renderAlignmentGuides(DrawContext context, HudConfig config) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int guideColor = 0x80FFFFFF;

        context.fill(centerX, 0, centerX - 1, this.height, guideColor);
        context.fill(0, centerY, this.width, centerY + 1, guideColor);

    }

    private void renderHudPreview(DrawContext context, HudConfig config) {
        int hudX = getX(config);
        int hudY = getY(config);

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(hudX, hudY, 1);
        matrices.scale(config.scale, config.scale, 1);


        int bgColor = config.guicolors.widgetBackground;
        if(config.roundedCorners){
            RenderUtils.fillRoundedRect(context,0,0, BASE_WIDTH, BASE_HEIGHT, 0.2f, bgColor);
        }else{
            context.fill(0, 0, BASE_WIDTH, BASE_HEIGHT, bgColor);
        }


        int borderColor = config.colorWithAlpha(config.colors.border, config.outlineOpacity);

        if(config.roundedCorners){
            RenderUtils.drawRoundedRectWithOutline(context, 0, 0, BASE_WIDTH, BASE_HEIGHT, 0.2f, 1, borderColor);
        }else{
            drawBorder(context, 0, 0, BASE_WIDTH, BASE_HEIGHT, borderColor);
        }


        matrices.pop();
    }

    private void renderInstructions(DrawContext context) {
        Text[] instructions = {
                Text.literal("§8[").append(Text.literal("Drag").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fMoveHUD"),
                Text.literal("§8[").append(Text.literal("Mouse Scroll").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fResize"),
                Text.literal("§8[").append(Text.literal("R").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fReset Position"),
                Text.literal("§8[").append(Text.literal("ESC").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fSave & Exit")
        };

        int y = 10;
        for (Text instruction : instructions) {
            context.drawText(textRenderer, instruction, 10, y, 0xFFFFFFFF, true);
            y += 12;
        }
    }

    private void updateAnchor() {
        var config = SchrumboHUDClient.config;
        int hudWidth = (int)(BASE_WIDTH * config.scale);
        int hudHeight = (int)(BASE_HEIGHT * config.scale);

        int absX = config.position.x;
        int absY = config.position.y;

        if (absX + hudWidth / 2 > this.width / 2) {
            config.anchor.horizontal = HudConfig.HorizontalAnchor.RIGHT;
            config.position.x = this.width - absX - hudWidth;
        } else {
            config.anchor.horizontal = HudConfig.HorizontalAnchor.LEFT;
        }

        if (absY + hudHeight / 2 > this.height / 2) {
            config.anchor.vertical = HudConfig.VerticalAnchor.BOTTOM;
            config.position.y = this.height - absY - hudHeight;
        } else {
            config.anchor.vertical = HudConfig.VerticalAnchor.TOP;
        }
    }

    private int getX(HudConfig config) {
        if (dragging) return tempAbsX;

        int hudWidth = (int)(BASE_WIDTH * config.scale);
        return switch (config.anchor.horizontal) {
            case LEFT -> config.position.x;
            case RIGHT -> this.width - hudWidth - config.position.x;
        };
    }

    private int getY(HudConfig config) {
        if (dragging) return tempAbsY;

        int hudHeight = (int)(BASE_HEIGHT * config.scale);
        return switch (config.anchor.vertical) {
            case TOP -> config.position.y;
            case BOTTOM -> this.height - hudHeight - config.position.y;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var config = SchrumboHUDClient.config;
            int hudWidth = (int)(BASE_WIDTH * config.scale);
            int hudHeight = (int)(BASE_HEIGHT * config.scale);
            int hudX = getX(config);
            int hudY = getY(config);


            if (mouseX >= hudX && mouseX <= hudX + hudWidth &&
                    mouseY >= hudY && mouseY <= hudY + hudHeight) {
                dragging = true;
                dragOffsetX = (int)mouseX - hudX;
                dragOffsetY = (int)mouseY - hudY;

                tempAbsX = hudX;
                tempAbsY = hudY;

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            tempAbsX = (int)mouseX - dragOffsetX;
            tempAbsY = (int)mouseY - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            dragging = false;

            var config = SchrumboHUDClient.config;
            config.position.x = tempAbsX;
            config.position.y = tempAbsY;

            updateAnchor();
            ConfigManager.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var config = SchrumboHUDClient.config;

        if (keyCode == GLFW.GLFW_KEY_R) {
            config.position.x = 10;
            config.position.y = 10;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ConfigManager.save();
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
