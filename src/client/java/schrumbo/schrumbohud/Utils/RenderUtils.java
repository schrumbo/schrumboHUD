package schrumbo.schrumbohud.Utils;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    /**
     * Renders a filled rounded rectangle
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
     * Renders an outlined rounded rectangle
     */
    public static void drawRoundedRectWithOutline(DrawContext context, int x, int y, int width, int height, float radius, int thickness, int color) {
        if (radius <= 0) {
            context.drawBorder(x, y, width, height, color);
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
     * Fills a rounded corner
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
     * Draws a rounded corner outline
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
     * draws a rectangle with 1 missing pixel in each corner
     * @param context
     * @param x
     * @param y
     * @param width
     * @param height
     * @param thickness
     * @param color
     */
    public static void drawRectWithCutCorners(DrawContext context, int x, int y, int width, int height, int thickness, int color) {
        context.fill(x + 1, y, x + width - 1, y + thickness, color);

        context.fill(x + 1, y + height - thickness, x + width - 1, y + height, color);

        context.fill(x, y + 1, x + thickness, y + height - 1, color);

        context.fill(x + width - thickness, y + 1, x + width, y + height - 1, color);

        context.fill(x + thickness, y + thickness, x + width - thickness, y + height - thickness, color);
    }

    /**
     * Fills a circle using pixel-based rendering
     * @param context DrawContext for rendering
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param radius Radius of the circle
     * @param color Color in ARGB format
     */
    public static void fillCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    context.fill(centerX + dx, centerY + dy,
                            centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }
    }

}
