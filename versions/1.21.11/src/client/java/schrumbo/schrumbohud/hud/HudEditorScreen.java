package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
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

    //inv hud drag
    private boolean draggingInventory = false;
    private int invDragOffsetX = 0;
    private int invDragOffsetY = 0;
    private int invTempAbsX = 0;
    private int invTempAbsY = 0;

    //armor hud drag
    private boolean draggingArmor = false;
    private int armorDragOffsetX = 0;
    private int armorDragOffsetY = 0;
    private int armorTempAbsX = 0;
    private int armorTempAbsY = 0;

    //inv hud const
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
        renderInventoryHudPreview(context, config);
        renderArmorHudPreview(context, config);
        renderInstructions(context);
    }

    private void renderAlignmentGuides(DrawContext context, HudConfig config) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int guideColor = 0x80FFFFFF;

        context.fill(centerX, 0, centerX + 1, this.height, guideColor);
        context.fill(0, centerY, this.width, centerY + 1, guideColor);
    }

    private void renderInventoryHudPreview(DrawContext context, HudConfig config) {
        int hudX = getInventoryX(config);
        int hudY = getInventoryY(config);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(hudX, hudY);
        matrices.scale(config.scale, config.scale);

        int bgColor = config.guicolors.widgetBackground;
        if(config.roundedCorners){
            RenderUtils.fillRoundedRect(context, 0, 0, BASE_WIDTH, BASE_HEIGHT, 0.2f, bgColor);
        } else {
            context.fill(0, 0, BASE_WIDTH, BASE_HEIGHT, bgColor);
        }

        int borderColor = config.colorWithAlpha(config.colors.border, config.outlineOpacity);
        if(config.roundedCorners){
            RenderUtils.drawRoundedRectWithOutline(context, 0, 0, BASE_WIDTH, BASE_HEIGHT, 0.2f, 1, borderColor);
        } else {
            drawBorder(context, 0, 0, BASE_WIDTH, BASE_HEIGHT, borderColor);
        }

        matrices.popMatrix();
    }

    private void renderArmorHudPreview(DrawContext context, HudConfig config) {
        if (!config.armorEnabled) return;

        int rows, rowSlots;
        if (!config.armorVertical) {
            rows = 1;
            rowSlots = 4;
        } else {
            rows = 4;
            rowSlots = 1;
        }

        int armorWidth = rowSlots * SLOT_SIZE + PADDING * 2;
        int armorHeight = rows * SLOT_SIZE + PADDING * 2;

        int armorX = getArmorX(config, armorWidth);
        int armorY = getArmorY(config, armorHeight);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(armorX, armorY);

        int bgColor = config.guicolors.widgetBackground;
        if(config.roundedCorners){
            RenderUtils.fillRoundedRect(context, 0, 0, armorWidth, armorHeight, 0.5f, bgColor);
        } else {
            context.fill(0, 0, armorWidth, armorHeight, bgColor);
        }

        int borderColor = config.colorWithAlpha(config.colors.border, config.outlineOpacity);
        if(config.roundedCorners){
            RenderUtils.drawRoundedRectWithOutline(context, 0, 0, armorWidth, armorHeight, 0.5f, 1, borderColor);
        } else {
            drawBorder(context, 0, 0, armorWidth, armorHeight, borderColor);
        }

        matrices.popMatrix();
    }

    private void renderInstructions(DrawContext context) {
        Text[] instructions = {
                Text.literal("§8[").append(Text.literal("Drag").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fMove HUD Elements"),
                Text.literal("§8[").append(Text.literal("R").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fReset Positions"),
                Text.literal("§8[").append(Text.literal("ESC").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] ")).append("§fSave & Exit")
        };

        int y = 10;
        for (Text instruction : instructions) {
            context.drawText(textRenderer, instruction, 10, y, 0xFFFFFFFF, true);
            y += 12;
        }
    }

    private void updateInventoryAnchor() {
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

    private void updateArmorAnchor(int armorWidth, int armorHeight) {
        var config = SchrumboHUDClient.config;

        int absX = config.armorPosition.x;
        int absY = config.armorPosition.y;

        if (absX + armorWidth / 2 > this.width / 2) {
            config.armorAnchor.horizontal = HudConfig.HorizontalAnchor.RIGHT;
            config.armorPosition.x = this.width - absX - armorWidth;
        } else {
            config.armorAnchor.horizontal = HudConfig.HorizontalAnchor.LEFT;
        }

        if (absY + armorHeight / 2 > this.height / 2) {
            config.armorAnchor.vertical = HudConfig.VerticalAnchor.BOTTOM;
            config.armorPosition.y = this.height - absY - armorHeight;
        } else {
            config.armorAnchor.vertical = HudConfig.VerticalAnchor.TOP;
        }
    }

    private int getInventoryX(HudConfig config) {
        if (draggingInventory) return invTempAbsX;

        int hudWidth = (int)(BASE_WIDTH * config.scale);
        return switch (config.anchor.horizontal) {
            case LEFT -> config.position.x;
            case RIGHT -> this.width - hudWidth - config.position.x;
        };
    }

    private int getInventoryY(HudConfig config) {
        if (draggingInventory) return invTempAbsY;

        int hudHeight = (int)(BASE_HEIGHT * config.scale);
        return switch (config.anchor.vertical) {
            case TOP -> config.position.y;
            case BOTTOM -> this.height - hudHeight - config.position.y;
        };
    }

    private int getArmorX(HudConfig config, int armorWidth) {
        if (draggingArmor) return armorTempAbsX;

        return switch (config.armorAnchor.horizontal) {
            case LEFT -> config.armorPosition.x;
            case RIGHT -> this.width - armorWidth - config.armorPosition.x;
        };
    }

    private int getArmorY(HudConfig config, int armorHeight) {
        if (draggingArmor) return armorTempAbsY;

        return switch (config.armorAnchor.vertical) {
            case TOP -> config.armorPosition.y;
            case BOTTOM -> this.height - armorHeight - config.armorPosition.y;
        };
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var config = SchrumboHUDClient.config;

            //armor hud
            if (config.armorEnabled) {
                int rows = config.armorVertical ? 4 : 1;
                int rowSlots = config.armorVertical ? 1 : 4;
                int armorWidth = rowSlots * SLOT_SIZE + PADDING * 2;
                int armorHeight = rows * SLOT_SIZE + PADDING * 2;
                int armorX = getArmorX(config, armorWidth);
                int armorY = getArmorY(config, armorHeight);

                if (click.x() >= armorX && click.x() <= armorX + armorWidth &&
                        click.y() >= armorY && click.y() <= armorY + armorHeight) {
                    draggingArmor = true;
                    armorDragOffsetX = (int)click.x() - armorX;
                    armorDragOffsetY = (int)click.y() - armorY;
                    armorTempAbsX = armorX;
                    armorTempAbsY = armorY;
                    return true;
                }
            }

            //inv hud
            int hudWidth = (int)(BASE_WIDTH * config.scale);
            int hudHeight = (int)(BASE_HEIGHT * config.scale);
            int hudX = getInventoryX(config);
            int hudY = getInventoryY(config);

            if (click.x() >= hudX && click.x() <= hudX + hudWidth &&
                    click.y() >= hudY && click.y() <= hudY + hudHeight) {
                draggingInventory = true;
                invDragOffsetX = (int)click.x() - hudX;
                invDragOffsetY = (int)click.y() - hudY;
                invTempAbsX = hudX;
                invTempAbsY = hudY;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (draggingInventory) {
                invTempAbsX = (int)click.x() - invDragOffsetX;
                invTempAbsY = (int)click.y() - invDragOffsetY;
                return true;
            }
            if (draggingArmor) {
                armorTempAbsX = (int)click.x() - armorDragOffsetX;
                armorTempAbsY = (int)click.y() - armorDragOffsetY;
                return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var config = SchrumboHUDClient.config;

            if (draggingInventory) {
                draggingInventory = false;
                config.position.x = invTempAbsX;
                config.position.y = invTempAbsY;
                updateInventoryAnchor();
                ConfigManager.save();
                return true;
            }

            if (draggingArmor) {
                draggingArmor = false;
                config.armorPosition.x = armorTempAbsX;
                config.armorPosition.y = armorTempAbsY;

                int rows = config.armorVertical ? 4 : 1;
                int rowSlots = config.armorVertical ? 1 : 4;
                int armorWidth = rowSlots * SLOT_SIZE + PADDING * 2;
                int armorHeight = rows * SLOT_SIZE + PADDING * 2;

                updateArmorAnchor(armorWidth, armorHeight);
                ConfigManager.save();
                return true;
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        var config = SchrumboHUDClient.config;

        if (input.key() == GLFW.GLFW_KEY_R) {
            config.position.x = 10;
            config.position.y = 10;
            config.armorPosition.x = 10;
            config.armorPosition.y = 10;
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