package schrumbo.schrumbohud.clickgui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.RenderUtils;
import schrumbo.schrumbohud.clickgui.categories.*;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.Widget;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Main ClickGUI screen for configuration.
 * Handles rendering input and category management
 */
public class ClickGuiScreen extends Screen {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final HudConfig config = SchrumboHUDClient.config;
    private int panelX = 0;
    private int panelY = 0;
    private static final int PANEL_WIDTH = 1000; //default 500
    private static final int PANEL_HEIGHT = 600; // default 400
    private static final int TITLE_BAR_HEIGHT = 25;

    private boolean draggingPanel = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public static final List<Category> categories = new ArrayList<>();
    private int scrollOffset = 0;
    private int contentHeight = 0;

    /**
     * Initializes the ClickGUI with all configuration categories.
     */
    public ClickGuiScreen() {
        super(Text.literal("SchrumboHUD"));

        //prevents categories from duplicating
        categories.clear();

        categories.add(new GeneralCategory());
        categories.add(new PresetsCategory());
        categories.add(new PositionCategory());
        categories.add(new BackgroundCategory());
        categories.add(new OutlineCategory());
        categories.add(new SlotCategory());
        categories.add(new TextCategory());
    }

    @Override
    protected void init() {
        super.init();

        //center
        centerPosX();
        centerPosY();

        initializeCategories();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void resize(MinecraftClient client, int width, int height) {}

    /**
     * Calculates and sets positions for all categories.
     */
    private void initializeCategories() {
        int contentWidth = PANEL_WIDTH - 20;
        int currentY = 0;

        for (Category category : categories) {
            category.setPosition(panelX + 10, panelY + TITLE_BAR_HEIGHT + 10 + currentY - scrollOffset, contentWidth);
            currentY += category.getTotalHeight() + 10;
        }

        contentHeight = currentY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        float scale = config.configScale;
        calcScale();

        float scaledMouseX = (float) mouseX / scale;
        float scaledMouseY = (float) mouseY / scale;

        var matrices = context.getMatrices();

        matrices.pushMatrix();
        matrices.scale(scale, scale);
        renderPanel(context);
        renderScrollbar(context);
        matrices.popMatrix();

        int contentX = panelX + 10;
        int contentY = panelY + TITLE_BAR_HEIGHT + 10;
        int contentWidth = PANEL_WIDTH - 20;
        int contentAreaHeight = PANEL_HEIGHT - TITLE_BAR_HEIGHT - 20;

        int scissorX = (int) (contentX * scale);
        int scissorY = (int) (contentY * scale);
        int scissorX2 = (int) ((contentX + contentWidth) * scale);
        int scissorY2 = (int) ((contentY + contentAreaHeight) * scale);

        context.enableScissor(scissorX, scissorY, scissorX2, scissorY2);

        matrices.pushMatrix();
        matrices.scale(scale, scale);

        for (Category category : categories) {
            category.render(context, (int) scaledMouseX, (int) scaledMouseY, delta);
        }

        matrices.popMatrix();
        context.disableScissor();

        matrices.pushMatrix();
        matrices.scale(scale, scale);

        for (Category category : categories) {
            if (!category.isCollapsed()) {
                for (Widget widget : category.widgets) {
                    if (widget instanceof ColorPickerWidget colorPicker) {
                        colorPicker.renderPopupLayered(context, (int) scaledMouseX, (int) scaledMouseY, delta);
                    }
                }
            }
        }

        matrices.popMatrix();
    }

    /**
     * calculates the gui scale based on the GuiScale setting
     */
    public void calcScale(){
        int guiScale = client.options.getGuiScale().getValue();

        if(guiScale == 0) {
            guiScale = (int) client.getWindow().getScaleFactor();
        }
        //if not 1.0f / guiScale centering will not work
        config.configScale = 1.0f / guiScale;

    }

    /**
     * centers x pos
     */
    public void centerPosX(){
        int windowWidth = (int)(client.getWindow().getFramebufferWidth());
        panelX = (int)(windowWidth / 2 - PANEL_WIDTH / 2);
    }

    /**
     * centers y pos
     */
    public void centerPosY(){
        int windowHeight = (int)(client.getWindow().getFramebufferHeight());
        panelY = (int)(windowHeight / 2 - PANEL_HEIGHT / 2);
    }

    /**
     * Renders the main panel background and title bar.
     */
    private void renderPanel(DrawContext context) {

        RenderUtils.fillRoundedRect(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0.0f, config.guicolors.panelBackground);

        RenderUtils.drawRoundedRectWithOutline(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0.0f, 2, config.colorWithAlpha(config.guicolors.accent, config.guicolors.panelBorderOpacity));

        RenderUtils.fillRoundedRect(context, panelX, panelY, PANEL_WIDTH, TITLE_BAR_HEIGHT, 0.0f, config.colorWithAlpha(config.guicolors.accent, config.guicolors.panelTitleBarOpacity));

        String title = "";
        int titleX = panelX + (PANEL_WIDTH - client.textRenderer.getWidth(title)) / 2;
        int titleY = panelY + (TITLE_BAR_HEIGHT - 8) / 2;
        context.drawText(textRenderer, title, titleX, titleY, config.guicolors.text, true);

    }

    /**
     * Renders the scrollbar based on content height.
     */
    private void renderScrollbar(DrawContext context) {

        int scrollbarWidth = 4;
        int scrollbarX = panelX + PANEL_WIDTH - scrollbarWidth - 3;
        int scrollbarY = panelY + TITLE_BAR_HEIGHT + 10;
        int scrollbarHeight = PANEL_HEIGHT - TITLE_BAR_HEIGHT - 20;

        int trackColor = config.colorWithAlpha(0x000000, 0.3f);
        int maxScroll = Math.max(0, contentHeight - (PANEL_HEIGHT - TITLE_BAR_HEIGHT - 20));

        context.enableScissor(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight);
        context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, trackColor);
        if (maxScroll > 0) {
            float thumbHeightRatio = (float) (PANEL_HEIGHT - TITLE_BAR_HEIGHT - 20) / contentHeight;
            int thumbHeight = Math.max(20, (int) (scrollbarHeight * thumbHeightRatio));
            int thumbY = scrollbarY + (int) ((float) scrollOffset / maxScroll * (scrollbarHeight - thumbHeight));

            //SCROLLBAR THUMB
            RenderUtils.fillRoundedRect(context, scrollbarX, thumbY, scrollbarWidth, thumbHeight, 2.0f, config.colorWithAlpha(config.guicolors.accent, config.guicolors.widgetAccentOpacity));
        }
        context.disableScissor();
    }



    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);

        float scale = config.configScale;
        double scaledMouseX = click.x() / scale;
        double scaledMouseY = click.y() / scale;

        for (Category category : categories) {
            if (!category.isCollapsed()) {
                for (Widget widget : category.widgets) {
                    if (widget instanceof ColorPickerWidget colorPicker) {
                        if (colorPicker.isPopupOpen()) {
                            boolean handled = colorPicker.mouseClicked(scaledMouseX, scaledMouseY, click.button());
                            if (handled) return true;
                        }
                    }
                }
            }
        }

        if (scaledMouseX >= panelX && scaledMouseX <= panelX + PANEL_WIDTH && scaledMouseY >= panelY && scaledMouseY <= panelY + TITLE_BAR_HEIGHT) {
            draggingPanel = true;
            dragOffsetX = (int) (scaledMouseX - panelX);
            dragOffsetY = (int) (scaledMouseY - panelY);
            return true;
        }

        for (Category category : categories) {
            if (category.mouseClicked(scaledMouseX, scaledMouseY, click.button())) {
                initializeCategories();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() != 0) return super.mouseDragged(click, offsetX, offsetY);

        float scale = config.configScale;
        double scaledMouseX = click.x() / scale;
        double scaledMouseY = click.y() / scale;
        double scaledOffsetX = offsetX / scale;
        double scaledOffsetY = offsetY / scale;

        for (Category category : categories) {
            if (!category.isCollapsed()) {
                for (Widget widget : category.widgets) {
                    if (widget instanceof ColorPickerWidget colorPicker) {
                        if (colorPicker.isPopupOpen()) {
                            boolean handled = colorPicker.mouseDragged(scaledMouseX, scaledMouseY, click.button(), scaledOffsetX, scaledOffsetY);
                            if (handled) return true;
                        }
                    }
                }
            }
        }

        for (Category category : categories) {
            if (category.mouseDragged(scaledMouseX, scaledMouseY, click.button(), scaledOffsetX, scaledOffsetY)) {
                return true;
            }
        }

        if (draggingPanel) {
            panelX = (int) (scaledMouseX - dragOffsetX);
            panelY = (int) (scaledMouseY - dragOffsetY);
            initializeCategories();
            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() != 0) return super.mouseReleased(click);

        float scale = config.configScale;
        double scaledMouseX = click.x() / scale;
        double scaledMouseY = click.y() / scale;

        for (Category category : categories) {
            if (!category.isCollapsed()) {
                for (Widget widget : category.widgets) {
                    if (widget instanceof ColorPickerWidget colorPicker) {
                        if (colorPicker.isPopupOpen()) {
                            boolean handled = colorPicker.mouseReleased(scaledMouseX, scaledMouseY, click.button());
                            if (handled) return true;
                        }
                    }
                }
            }
        }

        for (Category category : categories) {
            if (category.mouseReleased(scaledMouseX, scaledMouseY, click.button())) {
                return true;
            }
        }

        if (draggingPanel) {
            draggingPanel = false;
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float scale = SchrumboHUDClient.config.configScale;
        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;

        if (scaledMouseX >= panelX && scaledMouseX <= panelX + PANEL_WIDTH && scaledMouseY >= panelY + TITLE_BAR_HEIGHT && scaledMouseY <= panelY + PANEL_HEIGHT) {

            int maxScroll = Math.max(0, contentHeight - (PANEL_HEIGHT - TITLE_BAR_HEIGHT - 20));
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 20)));

            initializeCategories();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            for (Category category : categories) {
                if (!category.isCollapsed()) {
                    for (Widget widget : category.widgets) {
                        if (widget instanceof ColorPickerWidget colorPicker) {
                            if (colorPicker.isPopupOpen()) {
                                colorPicker.closePopup();
                                return true;
                            }
                        }
                    }
                }
            }

            ConfigManager.save();
            this.close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}