package schrumbo.schrumbohud.clickgui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToggleWidget extends Widget {
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    private static final int TOGGLE_WIDTH = 40;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int KNOB_SIZE = 16;

    private final HudConfig config = SchrumboHUDClient.config;
    private final MinecraftClient client = MinecraftClient.getInstance();


    public ToggleWidget(Builder builder){
        super(builder);
        this.getter = builder.getter;
        this.setter = builder.setter;
    }

    public static class Builder extends Widget.Builder<Builder>{
        private  Supplier<Boolean> getter;
        private Consumer<Boolean> setter;


        public Builder value(Supplier<Boolean> getter, Consumer<Boolean> setter){
            this.getter = getter;
            this.setter = setter;
            return this;
        }

        @Override
        protected Builder self(){
            return this;
        }

        @Override
        public ToggleWidget build(){
            if(getter == null || setter == null){
                throw new IllegalStateException("Setter and getter must be set");
            }
            return new ToggleWidget(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


        hovered = isHovered(mouseX, mouseY);
        boolean enabled = getter.get();

        RenderUtils.fillRoundedRect(context, x, y, width, height, 0.0f, config.guicolors.widgetBackground);

        int labelX = x + PADDING;
        int labelY = y - client.textRenderer.fontHeight + height / 2;

        int textColor = hovered ? config.colorWithAlpha(config.guicolors.accent, config.guicolors.hoveredTextOpacity) : config.guicolors.text;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(labelX, labelY);
        matrices.scale(config.guicolors.textSize, config.guicolors.textSize);
        context.drawText(client.textRenderer, Text.literal(label), 0, 0, textColor, true);
        matrices.popMatrix();

        int toggleX = x + width - TOGGLE_WIDTH;
        //idk why but +3 is need for the switch to be centered
        int toggleY = y + height / 4 + 3;

        int buttonBgColor = enabled ? config.colorWithAlpha(config.guicolors.accent, 0.5f) : config.colorWithAlpha(0x404040, 0.7f);

        int knobX = enabled ? toggleX + TOGGLE_WIDTH - KNOB_SIZE - 2 : toggleX + 2;
        int knobY = toggleY + 2;

        int knobColor = enabled ? config.colorWithAlpha(config.guicolors.accent, 1.0f) : config.colorWithAlpha(0x808080, 1.0f);

        RenderUtils.fillRoundedRect(context, toggleX - PADDING, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, 0.5f, buttonBgColor);


        matrices.pushMatrix();
        //RenderUtils.fillRoundedRect(context, knobX - PADDING, knobY, KNOB_SIZE, KNOB_SIZE, 0.2f, knobColor);
        RenderUtils.drawRectWithCutCorners(context, knobX - PADDING, knobY, KNOB_SIZE, KNOB_SIZE, 2, knobColor);
        matrices.popMatrix();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered((int) mouseX, (int) mouseY)) {
            setter.accept(!getter.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}