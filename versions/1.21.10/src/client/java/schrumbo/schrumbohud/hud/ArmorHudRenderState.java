package schrumbo.schrumbohud.hud;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.item.ItemStack;
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
        ScreenRect scissorArea,
        int x1, int y1, int x2, int y2
) implements SpecialGuiElementRenderState {

    @Override
    public float scale() {
        return 16.0f * config.armorScale;
    }

    @Override
    public Matrix3x2f pose() {
        return pose;
    }

    @Override
    public ScreenRect scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRect bounds() {
        return SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, scissorArea);
    }
}
