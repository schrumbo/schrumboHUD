package schrumbo.schrumbohud.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.ArrayList;
import java.util.List;


public class ArmorRenderer implements HudElement {
    public static final Identifier ID = Identifier.of("schrumbomods", "armor_hud");

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private List<ItemStack> armor = new ArrayList<>();

    int rows;
    int rowSlots;

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.SUBTITLES,
                ID,
                new ArmorRenderer()
        );
    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        HudConfig config = SchrumboHUDClient.config;

        if (!config.armorEnabled || client == null || client.player == null) return;

        if (!config.armorVertical){
            rows = 1;
            rowSlots = 4;
        }else{
            rows = 4;
            rowSlots = 1;
        }
        getArmor();

        int hudWidth = rowSlots * SLOT_SIZE + PADDING * 2;
        int hudHeight = rows * SLOT_SIZE + PADDING * 2;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = calcX(config, screenWidth, hudWidth);
        int y = calcY(config, screenHeight, hudHeight);

        var matrices = drawContext.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);

        drawBackground(drawContext, hudWidth, hudHeight, config);
        renderArmor(drawContext, config);

        matrices.popMatrix();

    }

    /**
     * gets the players current equipped armor
     */
    private void getArmor(){
        if(client.player == null)return;
        armor.clear();
        armor.add(client.player.getEquippedStack(EquipmentSlot.HEAD));
        armor.add(client.player.getEquippedStack(EquipmentSlot.CHEST));
        armor.add(client.player.getEquippedStack(EquipmentSlot.LEGS));
        armor.add(client.player.getEquippedStack(EquipmentSlot.FEET));
    }

    /**
     * Calculates X position from relative offset
     */
    private int calcX(HudConfig config, int screenWidth, int hudWidth) {
        return switch (config.armorAnchor.horizontal) {
            case LEFT -> config.armorPosition.x;
            case RIGHT -> screenWidth - hudWidth - config.armorPosition.x;
        };
    }

    /**
     * Calculates Y position from relative offset
     */
    private int calcY(HudConfig config, int screenHeight, int hudHeight) {
        return switch (config.armorAnchor.vertical) {
            case TOP -> config.armorPosition.y;
            case BOTTOM -> screenHeight - hudHeight - config.armorPosition.y;
        };
    }
    /**
     * Renders background and outline
     */
    private void drawBackground(DrawContext context, int width, int height, HudConfig config) {
        if (config.backgroundEnabled) {
            int backgroundColor = config.colorWithAlpha(config.colors.background, config.backgroundOpacity);
            if (config.roundedCorners) {
                RenderUtils.fillRoundedRect(context, 0, 0, width, height, 0.5f, backgroundColor);
            } else {
                context.fill(0, 0, width, height, backgroundColor);
            }
        }

        if (config.outlineEnabled) {
            int borderColor = config.colorWithAlpha(config.colors.border, config.outlineOpacity);
            if (config.roundedCorners) {
                RenderUtils.drawRoundedRectWithOutline(context, 0, 0, width, height, 0.5f, 1, borderColor);
            } else {
                RenderUtils.drawBorder(context, 0, 0, width, height, borderColor);
            }
        }
    }

    private void renderArmor(DrawContext context, HudConfig config){
        int index = 0;
        for (int row = 0; row < rows; row ++){
            for (int col = 0; col < rowSlots; col ++){

                ItemStack stack = armor.get(index);
                index++;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                if (config.slotBackgroundEnabled) {
                    int slotColor = config.colorWithAlpha(config.colors.slots, config.slotBackgroundOpacity);
                    if (config.roundedCorners) {
                        RenderUtils.drawRectWithCutCorners(context, slotX, slotY, SLOT_SIZE - 2, SLOT_SIZE - 2, 1, slotColor);
                    } else {
                        context.fill(slotX, slotY, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2, slotColor);
                    }
                }
                renderItem(context, stack, slotX, slotY, config);
            }
        }
    }

    /**
     * Renders single item with count and durability
     */
    private void renderItem(DrawContext context, ItemStack stack, int x, int y, HudConfig config) {
        if (stack.isEmpty()) return;


        context.drawItem(stack, x, y);

        if (stack.isDamaged()) {
            renderDurabilityBar(context, stack, x, y, config);
        }
    }

    /**
     * Renders durability bar below item
     */
    private void renderDurabilityBar(DrawContext context, ItemStack stack, int x, int y, HudConfig config) {
        int maxDurability = stack.getMaxDamage();
        int currDurability = maxDurability - stack.getDamage();
        float percentDurability = (float) currDurability / maxDurability;

        int barWidth = SLOT_SIZE - 6;
        int filledWidth = (int) (barWidth * percentDurability);
        int barY = y + SLOT_SIZE - 4;

        var matrices = context.getMatrices();
        matrices.pushMatrix();

        context.fill(x + 2, barY, x + 2 + barWidth, barY + 1, 0xFF000000);
        context.fill(x + 2, barY, x + 2 + filledWidth, barY + 1, getDurabilityColor(percentDurability));

        matrices.popMatrix();
    }

    /**
     * Returns color based on durability percentage (green > yellow > red)
     */
    private int getDurabilityColor(float percent) {
        if (percent > 0.5f) return 0xFF00FF00;
        else if (percent > 0.25f) return 0xFFFFFF00;
        else return 0xFFFF0000;
    }

}
