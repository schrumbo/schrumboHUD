package schrumbo.schrumbohud.config;

import net.minecraft.client.MinecraftClient;
import schrumbo.schlib.annotations.*;
import schrumbo.schlib.config.ManagedConfig;
import schrumbo.schrumbohud.hud.HudEditorScreen;

/**
 * SchrumboHUD configuration — colors, layout, and theme presets
 */
public class SchrumboHudConfig extends ManagedConfig {

    public SchrumboHudConfig() {
        super("schrumbohud");
    }

    @Category(name = "General", description = "General settings")
    public static final int GENERAL = 0;

    @Category(name = "Presets", description = "Theme presets")
    public static final int PRESETS = 1;

    @Category(name = "Appearance", description = "Colors and styling")
    public static final int APPEARANCE = 2;

    @Category(name = "Armor HUD", description = "Armor HUD settings")
    public static final int ARMOR_HUD = 3;

    @ConfigOption(name = "Inventory HUD", description = "Toggle inventory HUD", category = "General")
    @Switch
    public boolean enabled = true;

    @ConfigOption(name = "Round Corners", description = "Rounded corners", category = "General")
    @Switch
    public boolean roundedCorners = true;

    public static class Anchor {
        public HorizontalAnchor horizontal = HorizontalAnchor.LEFT;
        public VerticalAnchor vertical = VerticalAnchor.TOP;
    }

    public static class Position {
        public int x = 10;
        public int y = 10;
    }

    public enum HorizontalAnchor {
        LEFT,
        RIGHT
    }

    public enum VerticalAnchor {
        TOP,
        BOTTOM
    }

    public Anchor anchor = new Anchor();
    public Position position = new Position();
    public Anchor armorAnchor = new Anchor();
    public Position armorPosition = new Position();

    @ConfigOption(name = "Edit Position", description = "Open position editor", category = "General")
    @Button
    public transient Runnable changePosition = () -> {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.send(() -> client.setScreen(new HudEditorScreen(client.currentScreen)));
        }
    };

    @ConfigOption(name = "Catppuccin Mocha", description = "Load Catppuccin Mocha theme", category = "Presets")
    @Button
    public transient Runnable catppuccinMocha = () -> {
        loadCatppuccinMocha();
        save();
    };

    @ConfigOption(name = "Dark", description = "Load Dark theme", category = "Presets")
    @Button
    public transient Runnable dark = () -> {
        loadDarkMode();
        save();
    };

    @ConfigOption(name = "Gruvbox", description = "Load Gruvbox theme", category = "Presets")
    @Button
    public transient Runnable gruvbox = () -> {
        loadGruvbox();
        save();
    };

    @ConfigOption(name = "Monokai", description = "Load Monokai theme", category = "Presets")
    @Button
    public transient Runnable monokai = () -> {
        loadMonokai();
        save();
    };

    @ConfigOption(name = "Dracula", description = "Load Dracula theme", category = "Presets")
    @Button
    public transient Runnable dracula = () -> {
        loadDracula();
        save();
    };

    @ConfigOption(name = "Classic", description = "Load Classic theme", category = "Presets")
    @Button
    public transient Runnable classic = () -> {
        loadClassic();
        save();
    };

    @ConfigOption(name = "Background", description = "Show background", category = "Appearance")
    @Switch
    public boolean backgroundEnabled = true;

    @ConfigOption(name = "Background Color", description = "Background color", category = "Appearance")
    @ColorPicker(allowAlpha = true)
    public int backgroundColor = 0xF21E1E2E;

    @ConfigOption(name = "Outline", description = "Show outline", category = "Appearance")
    @Switch
    public boolean outlineEnabled = true;

    @ConfigOption(name = "Outline Color", description = "Outline color", category = "Appearance")
    @ColorPicker(allowAlpha = true)
    public int borderColor = 0xFF89B4FA;

    @ConfigOption(name = "Slot Background", description = "Show slot background", category = "Appearance")
    @Switch
    public boolean slotBackgroundEnabled = true;

    @ConfigOption(name = "Slot Color", description = "Slot background color", category = "Appearance")
    @ColorPicker(allowAlpha = true)
    public int slotColor = 0xB3313244;

    @ConfigOption(name = "Text Shadow", description = "Text shadow", category = "Appearance")
    @Switch
    public boolean textShadowEnabled = true;

    @ConfigOption(name = "Text Color", description = "Text color", category = "Appearance")
    @ColorPicker(allowAlpha = true)
    public int textColor = 0xFFCDD6F4;

    @ConfigOption(name = "Armor HUD", description = "Enable armor HUD", category = "Armor HUD")
    @Switch
    public boolean armorEnabled = false;

    @ConfigOption(name = "vertical", description = "Vertical armor layout", category = "Armor HUD")
    @Switch
    public boolean armorVertical = false;

    @ConfigOption(name = "Transparency", description = "Armor transparency", category = "Armor HUD")
    @Slider(min = 0.0f, max = 1.0f, step = 0.05f)
    public float armorTransparency = 1.0f;

    public boolean firstJoin = true;
    public float scale = 1.0f;
    public float armorScale = 1.0f;
    public float configScale = 1.0f;

    public transient Colors colors = new Colors();

    public class Colors {
        public int background() { return backgroundColor; }
        public int border() { return borderColor; }
        public int text() { return textColor; }
        public int slots() { return slotColor; }
    }

    @Override
    public SchrumboHudConfig load() {
        super.load();
        migrateColors();
        initColors();
        return this;
    }

    @Override
    public ManagedConfig save() {
        return super.save();
    }

    public void initColors() {
        colors = new Colors();
    }

    private void migrateColors() {
        backgroundColor = ensureAlpha(backgroundColor, 0xF2);
        borderColor = ensureAlpha(borderColor, 0xFF);
        slotColor = ensureAlpha(slotColor, 0xB3);
        textColor = ensureAlpha(textColor, 0xFF);
    }

    private int ensureAlpha(int color, int defaultAlpha) {
        if (((color >> 24) & 0xFF) == 0) {
            return (defaultAlpha << 24) | (color & 0x00FFFFFF);
        }
        return color;
    }

    public void loadDarkMode() {
        backgroundColor = 0xFF1c1c1c;
        borderColor = 0xFF434343;
        textColor = 0xFFfddbbf;
        slotColor = 0xFF282828;
    }

    public void loadCatppuccinMocha() {
        backgroundColor = 0xF21E1E2E;
        borderColor = 0xFF89B4FA;
        textColor = 0xFFCDD6F4;
        slotColor = 0xB3313244;
    }

    public void loadGruvbox() {
        backgroundColor = 0xE6282828;
        borderColor = 0xFFFE8019;
        textColor = 0xFFEBDBB2;
        slotColor = 0xA63C3836;
    }

    public void loadMonokai() {
        backgroundColor = 0xEB272822;
        borderColor = 0xFFF92672;
        textColor = 0xFFF8F8F2;
        slotColor = 0xB349483E;
    }

    public void loadDracula() {
        backgroundColor = 0xF2282A36;
        borderColor = 0xFFBD93F9;
        textColor = 0xFFF8F8F2;
        slotColor = 0xBF44475A;
    }

    public void loadClassic() {
        backgroundColor = 0x80000000;
        borderColor = 0x60000000;
        textColor = 0xFFFFFFFF;
        slotColor = 0x50000000;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void enableHud(boolean value) {
        this.enabled = value;
    }

    public void enableArmorHud(boolean value) {
        this.armorEnabled = value;
    }

    public void enableVerticalMode(boolean value) {
        this.armorVertical = value;
    }

    public void enableBackground(boolean value) {
        this.backgroundEnabled = value;
    }

    public void enableBorder(boolean value) {
        this.outlineEnabled = value;
    }

    public void enableSlotBackground(boolean value) {
        this.slotBackgroundEnabled = value;
    }

    public void enableTextShadow(boolean value) {
        this.textShadowEnabled = value;
    }

    public void enableRoundedCorners(boolean value) {
        this.roundedCorners = value;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    public void setBorderColor(int color) {
        borderColor = color;
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public void setSlotColor(int color) {
        slotColor = color;
    }

    public void setArmorOpacity(float opacity) {
        armorTransparency = opacity;
    }

    public float getArmorOpacity() {
        return armorTransparency;
    }
}
