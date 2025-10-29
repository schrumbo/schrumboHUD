package schrumbo.schrumbohud.config;

import com.google.gson.annotations.SerializedName;

public class HudConfig {

    public boolean enabled = true;

    @SerializedName("anchor")
    public Anchor anchor = new Anchor();

    @SerializedName("position")
    public Position position = new Position();

    @SerializedName("colors")
    public Colors colors = new Colors();

    @SerializedName("cgcolors")
    public CGColors cgcolors = new CGColors();


    public static class Anchor {
        @SerializedName("horizontal")
        public HorizontalAnchor horizontal = HorizontalAnchor.LEFT;

        @SerializedName("vertical")
        public VerticalAnchor vertical = VerticalAnchor.TOP;
    }

    public enum HorizontalAnchor {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum VerticalAnchor {
        TOP,
        CENTER,
        BOTTOM
    }

    public static class Position{
        public int x = 10;
        public int y = 10;
    }

    public static class Colors {
        //HUD
        @SerializedName("background")
        public int background = 0x5955D9;

        @SerializedName("border")
        public int border = 0x000000;

        @SerializedName("text")
        public int text = 0xFFFFFF;


        @SerializedName("slots")
        public int slots = 0x000000;



    }
    //ClickGuiColors
    public class CGColors{
        //GENERAL ACCENT
        @SerializedName("accent")
        public int accent = 0x927392;

        //WIDGETS
        @SerializedName("text")
        public int text = 0xFFFFFFFF;

        @SerializedName("hoveredText")
        public int hoveredText = getColorWithAlpha(0x927392, 1.0f);

        @SerializedName("border")
        public int border = getColorWithAlpha(0x927392, 0.6f);

        @SerializedName("widgetBackground")
        public int widgetBackground = getColorWithAlpha(0x2a2a2a, 0.5f);

        @SerializedName("widgetBackgroundHovered")
        public int widgetBackgroundHovered = getColorWithAlpha(0x3a3a3a, 0.9f);

        @SerializedName("widgetBackgroundClicked")
        public int widgetBackgroundClicked = getColorWithAlpha(0x1a1a1a, 0.90f);

        @SerializedName("sliderHandle")
        public int sliderHandle = getColorWithAlpha(0xCCCCCC, 1.0f);

        @SerializedName("sliderHandleHovered")
        public int sliderHandleHovered = getColorWithAlpha(0xFFFFFF, 1.0f);

        //PANEL
        @SerializedName("panelBackground")
        public int panelBackground = getColorWithAlpha(0x1a1a1a, 0.95f);

        @SerializedName("panelBorder")
        public int panelBorder = getColorWithAlpha(0x927392, 1.0f);

        @SerializedName("panelTitleBar")
        public int panelTitleBar = getColorWithAlpha(0x927392, 0.3f);

        @SerializedName("accent80")
        public int accent80 = getColorWithAlpha(0x927392, 0.8f);


    }

    public void initColors(){

    }



    public float scale = 1.0f;
    public float configScale = 1.0f;
    public boolean roundedCorners = true;
    public boolean backgroundEnabled = true;
    public boolean outlineEnabled = true;
    public boolean textShadowEnabled = true;
    public boolean slotBackgroundEnabled = true;

    public float backgroundOpacity = 0.14f;
    public float outlineOpacity = 0.5f;
    public float textShadowOpacity = 1.0f;
    public float slotBackgroundOpacity = 0.3f;


    public int getColorWithAlpha(int color, float opacity){
        int alpha = (int) (opacity * 255);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }


    public void loadClassicInventoryHUD() {
        colors.background = 0x5955D9;
        colors.border = 0x000000;
        colors.text = 0xFFFFFF;
        colors.slots = 0x000000;

        backgroundOpacity = 0.14f;
        outlineOpacity = 0.5f;
        textShadowOpacity = 1.0f;
        slotBackgroundOpacity = 0.3f;
    }


    public void loadCatppuccinMocha() {
        colors.background = 0x1E1E2E;
        colors.border = 0x89B4FA;
        colors.text = 0xCDD6F4;
        colors.slots = 0x313244;

        backgroundOpacity = 0.95f;
        outlineOpacity = 1.0f;
        textShadowOpacity = 0.8f;
        slotBackgroundOpacity = 0.7f;
    }


    public void loadGruvbox() {
        colors.background = 0x282828;
        colors.border = 0xFE8019;
        colors.text = 0xEBDBB2;
        colors.slots = 0x3C3836;

        backgroundOpacity = 0.9f;
        outlineOpacity = 1.0f;
        textShadowOpacity = 0.9f;
        slotBackgroundOpacity = 0.65f;
    }


    public void loadMonokai() {
        colors.background = 0x272822;
        colors.border = 0xF92672;
        colors.text = 0xF8F8F2;
        colors.slots = 0x49483E;

        backgroundOpacity = 0.92f;
        outlineOpacity = 1.0f;
        textShadowOpacity = 0.85f;
        slotBackgroundOpacity = 0.7f;
    }


    public void loadDracula() {
        colors.background = 0x282A36;
        colors.border = 0xBD93F9;
        colors.text = 0xF8F8F2;
        colors.slots = 0x44475A;

        backgroundOpacity = 0.95f;
        outlineOpacity = 1.0f;
        textShadowOpacity = 0.9f;
        slotBackgroundOpacity = 0.75f;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void reset() {
        this.enabled = true;
        position.x = 10;
        position.y = 10;
        this.scale = 1.0f;
        this.roundedCorners = true;

        loadClassicInventoryHUD();

        this.backgroundEnabled = true;
        this.outlineEnabled = true;
        this.textShadowEnabled = true;
        this.slotBackgroundEnabled = true;
    }

    public void setBackgroundColor(int color) {
        colors.background = color;
    }

    public void setBorderColor(int color) {
        colors.border = color;
    }

    public void setTextColor(int color) {
        colors.text = color;
    }

    public void setAccentColor(int color) {
        cgcolors.accent = color;
    }

    public void setSlotColor(int color){
        colors.slots = color;
    }
}
