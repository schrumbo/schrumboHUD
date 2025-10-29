package schrumbo.schrumbohud.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

public class InventoryRenderer implements HudRenderCallback {
    HudConfig config = SchrumboHUDClient.config;

    private static final int SLOT_SIZE = 18;
    private static final int ROW_SLOTS = 9;
    private static final int ROWS = 3;
    private static final int PADDING = 4;

    public static void register(){
        HudRenderCallback.EVENT.register(new InventoryRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter){
        MinecraftClient client = MinecraftClient.getInstance();

        if(!config.enabled || client == null)return;

        PlayerInventory inventory = client.player.getInventory();
        float scale = config.scale;

        int hudWidth = ROW_SLOTS * SLOT_SIZE + PADDING * 2;
        int hudHeight = ROWS * SLOT_SIZE + PADDING * 2;

        int scaledWidth = (int) (hudWidth * scale);
        int scaledHeight = (int) (hudHeight * scale);

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = calcX(config, screenWidth, scaledWidth);
        int y = calcY(config, screenHeight, scaledHeight);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x,y);
        matrices.scale(scale, scale);

        drawBackground(context, hudWidth, hudHeight, config);
        renderInventory(context, inventory, config);

        matrices.popMatrix();
    }

    /**
     * calculates the x Position of the hud
     * @param config
     * @param screenWidth
     * @param hudWidth
     * @return x position of the hud
     */
    private int calcX(HudConfig config, int screenWidth, int hudWidth){
        int offset = config.position.x;

        return switch(config.anchor.horizontal){
            case LEFT -> offset;
            case RIGHT -> (screenWidth / 2) - (hudWidth / 2) + offset;
            case CENTER -> screenWidth - hudWidth - offset;
        };
    }

    /**
     * calculates the y Position of the hud
     * @param config
     * @param screenHeight
     * @param hudHeight
     * @return y position of the hud
     */
    private int calcY(HudConfig config, int screenHeight, int hudHeight){
        int offset = config.position.y;

        return switch(config.anchor.vertical){
            case TOP -> offset;
            case BOTTOM -> (screenHeight / 2) - (hudHeight / 2) + offset;
            case CENTER ->screenHeight - hudHeight - offset;
        };
    }

    /**
     * draws the hud background + outline
     * @param context
     * @param width
     * @param height
     * @param config
     */
    private void drawBackground(DrawContext context, int width, int height, HudConfig config){
        if(config.backgroundEnabled){
            int backgroundColor = config.colorWithAlpha(config.colors.background, config.backgroundOpacity);
            if(config.roundedCorners){
                RenderUtils.fillRoundedRect(context, 0, 0, width, height, 0.2f, backgroundColor);
            }else{
                context.fill(0, 0, width, height, backgroundColor);
            }

        }

        if(config.outlineEnabled){
            int borderColor = config.colorWithAlpha(config.colors.border, config.outlineOpacity);
            if(config.roundedCorners){
                RenderUtils.drawRoundedRectWithOutline(context, 0, 0, width, height, 0.2f, 1, borderColor);
            }else{
                RenderUtils.drawBorder(context, 0, 0, width, height, borderColor);
            }

        }
    }

    /**
     * renders the inventory of the player on the screen
     * @param context
     * @param inventory
     * @param config
     */
    private void renderInventory(DrawContext context, PlayerInventory inventory, HudConfig config){
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < ROW_SLOTS; col++){
                int slot = 9 + row * ROW_SLOTS + col;
                ItemStack stack = inventory.getStack(slot);

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + (row * SLOT_SIZE) + 1;
                if(config.slotBackgroundEnabled){
                    int slotColor = config.colorWithAlpha(config.colors.slots, config.slotBackgroundOpacity);
                    if (config.roundedCorners){
                        RenderUtils.drawRectWithCutCorners(context, slotX, slotY,SLOT_SIZE - 2, SLOT_SIZE - 2, 1, slotColor );
                    }else{
                        context.fill(slotX, slotY, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2, slotColor);
                    }
                }
                renderItem(context, stack, slotX, slotY, config);
            }
        }
    }

    /**
     * renders an item or an item stack
     * @param context
     * @param stack
     * @param x
     * @param y
     * @param config
     */
    private void renderItem(DrawContext context, ItemStack stack, int x, int y, HudConfig config){
        if(stack.isEmpty())return;

        context.drawItem(stack, x, y);

        if(stack.getCount() > 1){
            renderStackCount(context, stack, x, y, config);
        }
        if(stack.isDamaged()){
            renderDurabilityBar(context, stack, x, y, config);
        }
    }

    /**
     * renders the stack count of an item
     * @param context
     * @param stack
     * @param x
     * @param y
     * @param config
     */
    private void renderStackCount(DrawContext context, ItemStack stack, int x, int y, HudConfig config){
        String count = String.valueOf(stack.getCount());
        int textColor = config.colorWithAlpha(config.colors.text, 1.0f);

        var textRenderer = MinecraftClient.getInstance().textRenderer;

        var matrices = context.getMatrices();
        matrices.pushMatrix();

        context.drawText(textRenderer, count, x + SLOT_SIZE - 2 - textRenderer.getWidth(count), y + SLOT_SIZE - 9, textColor, config.textShadowEnabled);


        matrices.popMatrix();
    }

    private void renderDurabilityBar(DrawContext context, ItemStack stack, int x, int y, HudConfig config){
        int maxDurability = stack.getMaxDamage();
        int currDurability = maxDurability - stack.getDamage();
        float percentDurability = (float) currDurability / maxDurability;

        int barWidth = SLOT_SIZE - 6;
        int filledWidth = (int) (barWidth * percentDurability);
        int barY = y + SLOT_SIZE - 4;

        var matrices = context.getMatrices();
        matrices.pushMatrix();

        context.fill(x + 2, barY, x + 2 + barWidth, barY + 1, 0xFF000000);

        int durabilityColor = getDurabilityColor(percentDurability);

        context.fill(x + 2, barY, x + 2 + filledWidth, barY + 1, durabilityColor);

        matrices.popMatrix();
    }

    /**
     * gets the color for the durability
     * @param percent
     * @return color based on the current durability of an item
     */
    private int getDurabilityColor(float percent) {
        if (percent > 0.5f) {
            return 0xFF00FF00;
        } else if (percent > 0.25f) {
            return 0xFFFFFF00;
        } else {
            return 0xFFFF0000;
        }
    }











































}
