package schrumbo.schrumbohud.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Armor HUD overlay element
 */
public class ArmorRenderer implements HudElement {
    public static final Identifier ID = Identifier.of("schrumbomods", "armor_hud");

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final List<ItemStack> armor = new ArrayList<>();

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
        SchrumboHudConfig config = SchrumboHUDClient.config;

        if (!config.armorEnabled || client == null || client.player == null) return;

        if (!config.armorVertical) {
            rows = 1;
            rowSlots = 4;
        } else {
            rows = 4;
            rowSlots = 1;
        }
        getArmor();

        int hudWidth = rowSlots * SLOT_SIZE + PADDING * 2;
        int hudHeight = rows * SLOT_SIZE + PADDING * 2;

        float scale = config.armorScale;
        int scaledWidth = Math.round(hudWidth * scale);
        int scaledHeight = Math.round(hudHeight * scale);

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = calcX(config, screenWidth, scaledWidth);
        int y = calcY(config, screenHeight, scaledHeight);

        var matrices = drawContext.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        drawBackground(drawContext, hudWidth, hudHeight, config);
        renderSlotBackgrounds(drawContext, config);

        matrices.popMatrix();

        ScreenRect scissor = drawContext.scissorStack.peekLast();
        Matrix3x2f pose = new Matrix3x2f(drawContext.getMatrices());

        ArmorHudRenderState renderState = new ArmorHudRenderState(
                List.copyOf(armor), config, rows, rowSlots, pose, scissor,
                x, y, x + scaledWidth, y + scaledHeight
        );
        drawContext.state.addSpecialElement(renderState);

        matrices.pushMatrix();
        matrices.translate(x, y);
        if (scale != 1.0f) {
            matrices.scale(scale, scale);
        }

        renderOverlays(drawContext, config);

        matrices.popMatrix();
    }

    private void getArmor() {
        if (client.player == null) return;
        armor.clear();
        armor.add(client.player.getEquippedStack(EquipmentSlot.HEAD));
        armor.add(client.player.getEquippedStack(EquipmentSlot.CHEST));
        armor.add(client.player.getEquippedStack(EquipmentSlot.LEGS));
        armor.add(client.player.getEquippedStack(EquipmentSlot.FEET));
    }

    private int calcX(SchrumboHudConfig config, int screenWidth, int hudWidth) {
        return switch (config.armorAnchor.horizontal) {
            case LEFT -> config.armorPosition.x;
            case RIGHT -> screenWidth - hudWidth - config.armorPosition.x;
        };
    }

    private int calcY(SchrumboHudConfig config, int screenHeight, int hudHeight) {
        return switch (config.armorAnchor.vertical) {
            case TOP -> config.armorPosition.y;
            case BOTTOM -> screenHeight - hudHeight - config.armorPosition.y;
        };
    }

    private int applyArmorTransparency(int color, float transparency) {
        int alpha = (int) (((color >> 24) & 0xFF) * transparency);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private void drawBackground(DrawContext context, int width, int height, SchrumboHudConfig config) {
        float t = config.armorTransparency;
        if (config.backgroundEnabled) {
            int bgColor = applyArmorTransparency(config.colors.background(), t);
            if (config.roundedCorners) {
                RenderUtils.fillRoundedRect(context, 0, 0, width, height, 0.5f, bgColor);
            } else {
                context.fill(0, 0, width, height, bgColor);
            }
        }

        if (config.outlineEnabled) {
            int borderColor = applyArmorTransparency(config.colors.border(), t);
            if (config.roundedCorners) {
                RenderUtils.drawRoundedRectWithOutline(context, 0, 0, width, height, 0.5f, 1, borderColor);
            } else {
                RenderUtils.drawBorder(context, 0, 0, width, height, borderColor);
            }
        }
    }

    private void renderSlotBackgrounds(DrawContext context, SchrumboHudConfig config) {
        if (!config.slotBackgroundEnabled) return;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < rowSlots; col++) {
                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;
                int slotColor = applyArmorTransparency(config.colors.slots(), config.armorTransparency);
                if (config.roundedCorners) {
                    RenderUtils.drawRectWithCutCorners(context, slotX, slotY, SLOT_SIZE - 2, SLOT_SIZE - 2, 1, slotColor);
                } else {
                    context.fill(slotX, slotY, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2, slotColor);
                }
                index++;
            }
        }
    }

    private void renderOverlays(DrawContext context, SchrumboHudConfig config) {
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < rowSlots; col++) {
                ItemStack stack = armor.get(index);
                index++;

                if (stack.isEmpty()) continue;

                int slotX = PADDING + col * SLOT_SIZE + 1;
                int slotY = PADDING + row * SLOT_SIZE + 1;

                if (stack.isDamaged()) {
                    renderDurabilityBar(context, stack, slotX, slotY);
                }
            }
        }
    }

    private void renderDurabilityBar(DrawContext context, ItemStack stack, int x, int y) {
        int maxDurability = stack.getMaxDamage();
        int currDurability = maxDurability - stack.getDamage();
        float percentDurability = (float) currDurability / maxDurability;

        int barWidth = SLOT_SIZE - 6;
        int filledWidth = (int) (barWidth * percentDurability);
        int barY = y + SLOT_SIZE - 4;

        context.fill(x + 2, barY, x + 2 + barWidth, barY + 1, 0xFF000000);
        context.fill(x + 2, barY, x + 2 + filledWidth, barY + 1, getDurabilityColor(percentDurability));
    }

    private int getDurabilityColor(float percent) {
        if (percent > 0.5f) return 0xFF00FF00;
        else if (percent > 0.25f) return 0xFFFFFF00;
        else return 0xFFFF0000;
    }
}
