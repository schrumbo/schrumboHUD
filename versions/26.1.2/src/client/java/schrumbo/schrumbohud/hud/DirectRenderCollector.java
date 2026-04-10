package schrumbo.schrumbohud.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionf;

import java.util.List;

/** Immediate-mode SubmitNodeCollector for PiP rendering */
public class DirectRenderCollector implements SubmitNodeCollector {

    private final PoseStack poseStack;
    private final MultiBufferSource.BufferSource bufferSource;
    private final QuadInstance quadInstance = new QuadInstance();

    public DirectRenderCollector(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

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

    @Override
    public void submitItem(PoseStack matrices, ItemDisplayContext context,
                            int light, int overlay, int outlineColor,
                            int[] tintLayers, List<BakedQuad> quads,
                            ItemStackRenderState.FoilType foilType) {
        PoseStack.Pose pose = matrices.last();
        boolean hasFoil = foilType != ItemStackRenderState.FoilType.NONE;
        boolean isSpecialFoil = foilType == ItemStackRenderState.FoilType.SPECIAL;
        PoseStack.Pose foilPose = hasFoil ? computeFoilDecalPose(context, pose) : null;

        quadInstance.setLightCoords(light);
        quadInstance.setOverlayCoords(overlay);

        for (BakedQuad quad : quads) {
            RenderType renderType = quad.materialInfo().itemRenderType();
            if (renderType == null) continue;

            int color = getLayerColor(tintLayers, quad.materialInfo());
            quadInstance.setColor(color);

            VertexConsumer consumer = bufferSource.getBuffer(renderType);
            consumer.putBakedQuad(pose, quad, quadInstance);

            if (hasFoil) {
                RenderType foilRenderType = ItemFeatureRenderer.getFoilRenderType(renderType, true);
                VertexConsumer foilConsumer = bufferSource.getBuffer(foilRenderType);
                if (foilPose != null) {
                    foilConsumer = new com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator(
                            foilConsumer, foilPose, 0.0078125f);
                }
                quadInstance.setColor(0xFFFFFFFF);
                foilConsumer.putBakedQuad(pose, quad, quadInstance);
                quadInstance.setColor(color);
            }
        }
    }

    private static PoseStack.Pose computeFoilDecalPose(ItemDisplayContext context, PoseStack.Pose pose) {
        PoseStack.Pose copy = pose.copy();
        com.mojang.math.MatrixUtil.mulComponentWise(copy.pose(), 8.0f);
        if (context.firstPerson()) {
            com.mojang.math.MatrixUtil.mulComponentWise(copy.pose(), 0.75f);
        }
        return copy;
    }

    private static int getLayerColor(int[] tintLayers, BakedQuad.MaterialInfo materialInfo) {
        if (!materialInfo.isTinted() || tintLayers == null) return 0xFFFFFFFF;
        int idx = materialInfo.tintIndex();
        if (idx >= 0 && idx < tintLayers.length) {
            return tintLayers[idx] | 0xFF000000;
        }
        return 0xFFFFFFFF;
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
    public void submitMovingBlock(PoseStack matrices, MovingBlockRenderState state) {}

    @Override
    public void submitBlockModel(PoseStack matrices, RenderType renderType,
                                  List<BlockStateModelPart> parts, int[] tints,
                                  int light, int overlay, int color) {}

    @Override
    public void submitBreakingBlockModel(PoseStack matrices, BlockStateModel model,
                                          long seed, int destroyStage) {}

    @Override
    public void submitCustomGeometry(PoseStack matrices, RenderType renderType,
                                      CustomGeometryRenderer renderer) {}

    @Override
    public void submitParticleGroup(ParticleGroupRenderer renderer) {}
}
