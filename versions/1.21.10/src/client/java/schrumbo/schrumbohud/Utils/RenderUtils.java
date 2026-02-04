package schrumbo.schrumbohud.Utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

/**
 * GUI drawing utilities — rounded rects, outlines, cut-corner shapes
 */
public class RenderUtils {

    /**
     * Filled rectangle with rounded corners
     */
    public static void fillRoundedRect(DrawContext context, int x, int y, int width, int height, float radius, int color) {
        if (radius <= 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }

        int minDimension = Math.min(width, height);
        int cornerRadius = (int) (minDimension * radius * 0.5f);
        cornerRadius = Math.min(cornerRadius, minDimension / 2);

        context.fill(x + cornerRadius, y + cornerRadius, x + width - cornerRadius, y + height - cornerRadius, color);

        context.fill(x + cornerRadius, y, x + width - cornerRadius, y + cornerRadius, color);

        context.fill(x + cornerRadius, y + height - cornerRadius, x + width - cornerRadius, y + height, color);

        context.fill(x, y + cornerRadius, x + cornerRadius, y + height - cornerRadius, color);

        context.fill(x + width - cornerRadius, y + cornerRadius, x + width, y + height - cornerRadius, color);

        fillRoundedCorner(context, x, y, cornerRadius, color, 1);
        fillRoundedCorner(context, x + width - cornerRadius, y, cornerRadius, color, 2);
        fillRoundedCorner(context, x, y + height - cornerRadius, cornerRadius, color, 4);
        fillRoundedCorner(context, x + width - cornerRadius, y + height - cornerRadius, cornerRadius, color, 3);
    }


    /**
     * Rounded rectangle outline
     */
    public static void drawRoundedRectWithOutline(DrawContext context, int x, int y, int width, int height, float radius, int thickness, int color) {
        if (radius <= 0) {
            drawBorder(context, x, y, width, height, color);
            return;
        }

        int minDimension = Math.min(width, height);
        int cornerRadius = (int) (minDimension * radius * 0.5f);
        cornerRadius = Math.min(cornerRadius, minDimension / 2);

        context.fill(x + cornerRadius, y, x + width - cornerRadius, y + thickness, color);

        context.fill(x + cornerRadius, y + height - thickness, x + width - cornerRadius, y + height, color);

        context.fill(x, y + cornerRadius, x + thickness, y + height - cornerRadius, color);

        context.fill(x + width - thickness, y + cornerRadius, x + width, y + height - cornerRadius, color);

        drawRoundedCornerOutline(context, x, y, cornerRadius, thickness, color, 1);
        drawRoundedCornerOutline(context, x + width - cornerRadius, y, cornerRadius, thickness, color, 2);
        drawRoundedCornerOutline(context, x, y + height - cornerRadius, cornerRadius, thickness, color, 4);
        drawRoundedCornerOutline(context, x + width - cornerRadius, y + height - cornerRadius, cornerRadius, thickness, color, 3);
    }

    /**
     * Filled rounded corner
     * @param corner 1=topleft 2=topright 3=bottomright 4=bottomleft
     */
    private static void fillRoundedCorner(DrawContext context, int x, int y, int radius, int color, int corner) {
        for (int dx = 0; dx < radius; dx++) {
            for (int dy = 0; dy < radius; dy++) {
                int checkX = 0, checkY = 0;

                switch (corner) {
                    case 1:
                        checkX = radius - 1 - dx;
                        checkY = radius - 1 - dy;
                        break;
                    case 2:
                        checkX = dx;
                        checkY = radius - 1 - dy;
                        break;
                    case 3:
                        checkX = dx;
                        checkY = dy;
                        break;
                    case 4:
                        checkX = radius - 1 - dx;
                        checkY = dy;
                        break;
                }

                double distance = Math.sqrt(checkX * checkX + checkY * checkY);
                if (distance <= radius) {
                    context.fill(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
                }
            }
        }
    }

    /**
     * Rounded corner outline
     * @param corner 1=topleft 2=topright 3=bottomright 4=bottomleft
     */
    private static void drawRoundedCornerOutline(DrawContext context, int x, int y, int radius, int thickness, int color, int corner) {
        for (int dx = 0; dx < radius; dx++) {
            for (int dy = 0; dy < radius; dy++) {
                int checkX = 0, checkY = 0;

                switch (corner) {
                    case 1:
                        checkX = radius - 1 - dx;
                        checkY = radius - 1 - dy;
                        break;
                    case 2:
                        checkX = dx;
                        checkY = radius - 1 - dy;
                        break;
                    case 3:
                        checkX = dx;
                        checkY = dy;
                        break;
                    case 4:
                        checkX = radius - 1 - dx;
                        checkY = dy;
                        break;
                }

                double distance = Math.sqrt(checkX * checkX + checkY * checkY);
                if (distance >= radius - thickness && distance <= radius) {
                    context.fill(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
                }
            }
        }
    }

    /**
     * Filled rectangle with cut corners
     */
    public static void drawRectWithCutCorners(DrawContext context, int x, int y, int width, int height, int thickness, int color) {
        drawOutlineWithCutCorners(context, x, y, width, height, thickness, color);

        context.fill(x + thickness, y + thickness, x + width - thickness, y + height - thickness, color);
    }

    /**
     * Outline with cut corners
     */
    public static void drawOutlineWithCutCorners(DrawContext context, int x, int y, int width, int height, int thickness, int color) {
        context.fill(x + 1, y, x + width - 1, y + thickness, color);
        context.fill(x + 1, y + height - thickness, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + thickness, y + height - 1, color);
        context.fill(x + width - thickness, y + 1, x + width, y + height - 1, color);
    }

    /**
     * 1px rectangle border
     */
    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }



}
