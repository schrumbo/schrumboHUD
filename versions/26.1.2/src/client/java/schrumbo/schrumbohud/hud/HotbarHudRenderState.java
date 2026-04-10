package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix3x2f;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

/**
 * Hotbar HUD special element render state
 */
public record HotbarHudRenderState(
        Inventory inventory,
        SchrumboHudConfig config,
        int selectedSlot,
        int rows,
        int rowSlots,
        boolean showOffhand,
        int mainOffsetX,
        int mainOffsetY,
        int offhandOffsetX,
        int offhandOffsetY,
        Matrix3x2f pose,
        ScreenRectangle scissorArea,
        int x0, int y0, int x1, int y1
) implements PictureInPictureRenderState {

    @Override
    public float scale() {
        return 16.0f * config.hotbarScale;
    }

    @Override
    public Matrix3x2f pose() {
        return pose;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
    }
}
