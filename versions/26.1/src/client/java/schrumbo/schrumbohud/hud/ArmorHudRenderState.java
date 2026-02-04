package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import schrumbo.schrumbohud.config.SchrumboHudConfig;

import java.util.List;

/**
 * Armor HUD special element render state
 */
public record ArmorHudRenderState(
        List<ItemStack> armorStacks,
        SchrumboHudConfig config,
        int rows,
        int rowSlots,
        Matrix3x2f pose,
        ScreenRectangle scissorArea,
        int x0, int y0, int x1, int y1
) implements PictureInPictureRenderState {

    @Override
    public float scale() {
        return 16.0f * config.armorScale;
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
