package schrumbo.schrumbohud.Utils;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

public class RenderUtils {


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

    public static void drawRectWithCutCorners(DrawContext context, int x, int y, int width, int height, int thickness, int color) {
        context.fill(x + 1, y, x + width - 1, y + thickness, color);

        context.fill(x + 1, y + height - thickness, x + width - 1, y + height, color);

        context.fill(x, y + 1, x + thickness, y + height - 1, color);

        context.fill(x + width - thickness, y + 1, x + width, y + height - 1, color);

        context.fill(x + thickness, y + thickness, x + width - thickness, y + height - thickness, color);
    }


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



    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color){
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void fillCheckerboard(DrawContext context, int x, int y, int width, int height, int squareSize) {

        int lightColor = 0xFFFFFFFF;
        int darkColor = 0xFFC0C0C0;

        for (int row = 0; row < Math.ceil((double) height / squareSize); row++) {
            for (int col = 0; col < Math.ceil((double) width / squareSize); col++) {
                boolean isLight = (row + col) % 2 == 0;
                int color = isLight ? lightColor : darkColor;

                int squareX = x + col * squareSize;
                int squareY = y + row * squareSize;
                int squareW = Math.min(squareSize, width - col * squareSize);
                int squareH = Math.min(squareSize, height - row * squareSize);

                context.fill(squareX, squareY, squareX + squareW, squareY + squareH, color);
            }
        }

    }

}
