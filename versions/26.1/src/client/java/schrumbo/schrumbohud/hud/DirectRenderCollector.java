package schrumbo.schrumbohud.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Quaternionf;

import java.util.List;

/**
 * A SubmitNodeCollector that renders immediately to a given bufferSource
 * instead of deferring. Used for rendering special models (skulls, etc.)
 * inside PictureInPictureRenderer where the deferred path doesn't work.
 */
public class DirectRenderCollector implements SubmitNodeCollector {

    private final PoseStack poseStack;
    private final MultiBufferSource.BufferSource bufferSource;

    /**
     * @param poseStack shared pose stack for rendering
     * @param bufferSource PiP buffer source to render into
     */
    public DirectRenderCollector(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

    /** Renders a model immediately to the PiP buffer source. */
    @Override
    public <S> void submitModel(Model<? super S> model, S state, PoseStack matrices,
                                 RenderType renderType, int light, int overlay, int color,
                                 TextureAtlasSprite sprite, int outlineColor,
                                 ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        poseStack.pushPose();
        poseStack.last().set(matrices.last());
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        if (sprite != null) {
            consumer = sprite.wrap(consumer);
        }
        model.setupAnim(state);
        model.renderToBuffer(poseStack, consumer, light, overlay, color);
        poseStack.popPose();
    }

    /** {@inheritDoc} */
    @Override
    public <S> void submitModel(Model<? super S> model, S state, PoseStack matrices,
                                 RenderType renderType, int light, int overlay, int outlineColor,
                                 ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        submitModel(model, state, matrices, renderType, light, overlay, -1, null, outlineColor, crumblingOverlay);
    }

    /** Renders a model part immediately to the PiP buffer source. */
    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType renderType,
                                 int light, int overlay, TextureAtlasSprite sprite,
                                 boolean visible, boolean skipDraw,
                                 int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                 int color) {
        if (!visible || skipDraw) return;
        poseStack.pushPose();
        poseStack.last().set(matrices.last());
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        if (sprite != null) {
            consumer = sprite.wrap(consumer);
        }
        part.render(poseStack, consumer, light, overlay, color);
        poseStack.popPose();
    }

    /** Renders an item immediately to the PiP buffer source. */
    @Override
    public void submitItem(PoseStack matrices, ItemDisplayContext context,
                            int light, int overlay, int outlineColor,
                            int[] tintLayers, List<BakedQuad> quads,
                            RenderType renderType, ItemStackRenderState.FoilType foilType) {
        poseStack.pushPose();
        poseStack.last().set(matrices.last());
        ItemRenderer.renderItem(context, poseStack, bufferSource, light, overlay,
                tintLayers, quads, renderType, foilType);
        poseStack.popPose();
    }

    @Override
    public void submitShadow(PoseStack matrices, float radius,
                              List<EntityRenderState.ShadowPiece> pieces) {}

    @Override
    public void submitNameTag(PoseStack matrices, Vec3 pos, int bgColor,
                               Component text, boolean seeThrough, int packedLight,
                               double distance, CameraRenderState camera) {}

    @Override
    public void submitText(PoseStack matrices, float x, float y,
                            FormattedCharSequence text, boolean shadow,
                            Font.DisplayMode mode, int color, int bgColor,
                            int light, int sortOrder) {}

    @Override
    public void submitFlame(PoseStack matrices, EntityRenderState state, Quaternionf rotation) {}

    @Override
    public void submitLeash(PoseStack matrices, EntityRenderState.LeashState leash) {}

    @Override
    public void submitBlock(PoseStack matrices, BlockState state, int light, int overlay, int color) {}

    @Override
    public void submitMovingBlock(PoseStack matrices, MovingBlockRenderState state) {}

    @Override
    public void submitBlockModel(PoseStack matrices, RenderType renderType,
                                  BlockStateModel model, float x, float y, float z,
                                  int light, int overlay, int color) {}

    @Override
    public void submitCustomGeometry(PoseStack matrices, RenderType renderType,
                                      CustomGeometryRenderer renderer) {}

    @Override
    public void submitParticleGroup(ParticleGroupRenderer renderer) {}
}
