package schrumbo.schrumbohud.config;

import net.minecraft.client.Minecraft;
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

    public General general = new General();
    public Presets presets = new Presets();
    public Appearance appearance = new Appearance();
    public ArmorHud armorHud = new ArmorHud();
    public Hotbar hotbar = new Hotbar();


    @Category(name = "General", description = "General settings", searchTags = {"general", "settings", "toggle", "visibility"})
    public class General {

        @ConfigOption(name = "Inventory HUD", description = "Enable inventory HUD", searchTags = {"inventory", "hud", "enable", "toggle"})
        @Switch
        public boolean inventoryEnabled = true;

        @ConfigOption(name = "Show Always", description = "Show all enabled HUDs always. Disable for peek mode (hold keybind to show)", searchTags = {"show", "always", "peek", "toggle", "keybind"})
        @Switch
        public boolean showAlways = true;

        @ConfigOption(name = "Hide in Screens", description = "Hide all HUDs while any screen is open", searchTags = {"hide", "screen", "gui", "menu"})
        @Switch
        public boolean hideInScreens = true;

        @ConfigOption(name = "Round Corners", description = "Rounded corners", searchTags = {"round", "corners", "style"})
        @Switch
        public boolean roundedCorners = true;

        @ConfigOption(name = "Edit Position", description = "Open position editor", searchTags = {"position", "editor", "move", "drag"})
        @Button
        public transient Runnable changePosition = () -> {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.execute(() -> client.setScreenAndShow(new HudEditorScreen(null)));
            }
        };
    }


    @Category(name = "Presets", description = "Theme presets", searchTags = {"preset", "theme", "color", "catppuccin", "dracula", "gruvbox", "monokai"})
    public class Presets {

        @ConfigOption(name = "Catppuccin Mocha", description = "Load Catppuccin Mocha theme", searchTags = {"catppuccin", "mocha", "theme", "preset"})
        @Button
        public transient Runnable catppuccinMocha = () -> {
            loadCatppuccinMocha();
            save();
        };

        @ConfigOption(name = "Dark", description = "Load Dark theme", searchTags = {"dark", "theme", "preset"})
        @Button
        public transient Runnable dark = () -> {
            loadDarkMode();
            save();
        };

        @ConfigOption(name = "Gruvbox", description = "Load Gruvbox theme", searchTags = {"gruvbox", "theme", "preset"})
        @Button
        public transient Runnable gruvbox = () -> {
            loadGruvbox();
            save();
        };

        @ConfigOption(name = "Monokai", description = "Load Monokai theme", searchTags = {"monokai", "theme", "preset"})
        @Button
        public transient Runnable monokai = () -> {
            loadMonokai();
            save();
        };

        @ConfigOption(name = "Dracula", description = "Load Dracula theme", searchTags = {"dracula", "theme", "preset"})
        @Button
        public transient Runnable dracula = () -> {
            loadDracula();
            save();
        };

        @ConfigOption(name = "Classic", description = "Load Classic theme", searchTags = {"classic", "theme", "preset"})
        @Button
        public transient Runnable classic = () -> {
            loadClassic();
            save();
        };
    }


    @Category(name = "Appearance", description = "Colors and styling", searchTags = {"appearance", "color", "style", "background", "outline", "text", "slot"})
    public class Appearance {

        @ConfigOption(name = "Background", description = "Show background", searchTags = {"background", "enable", "toggle"})
        @Switch
        public boolean backgroundEnabled = true;

        @ConfigOption(name = "Background Color", description = "Background color", searchTags = {"background", "color", "opacity"})
        @ColorPicker(allowAlpha = true)
        public int backgroundColor = 0xF21E1E2E;

        @ConfigOption(name = "Outline", description = "Show outline", searchTags = {"outline", "border", "enable", "toggle"})
        @Switch
        public boolean outlineEnabled = true;

        @ConfigOption(name = "Outline Color", description = "Outline color", searchTags = {"outline", "border", "color"})
        @ColorPicker(allowAlpha = true)
        public int borderColor = 0xFF89B4FA;

        @ConfigOption(name = "Slot Background", description = "Show slot background", searchTags = {"slot", "background", "enable", "toggle"})
        @Switch
        public boolean slotBackgroundEnabled = true;

        @ConfigOption(name = "Slot Color", description = "Slot background color", searchTags = {"slot", "color", "opacity"})
        @ColorPicker(allowAlpha = true)
        public int slotColor = 0xB3313244;

        @ConfigOption(name = "Text Shadow", description = "Text shadow", searchTags = {"text", "shadow", "enable"})
        @Switch
        public boolean textShadowEnabled = true;

        @ConfigOption(name = "Text Color", description = "Text color", searchTags = {"text", "color", "font"})
        @ColorPicker(allowAlpha = true)
        public int textColor = 0xFFCDD6F4;
    }


    @Category(name = "Armor HUD", description = "Armor HUD settings", searchTags = {"armor", "hud", "display", "equipment"})
    public class ArmorHud {

        @ConfigOption(name = "Armor HUD", description = "Enable armor HUD", searchTags = {"armor", "hud", "enable", "toggle"})
        @Switch
        public boolean armorEnabled = false;

        @ConfigOption(name = "Vertical", description = "Vertical armor layout", searchTags = {"vertical", "layout", "orientation"})
        @Switch
        public boolean armorVertical = false;

        @ConfigOption(name = "Transparency", description = "Armor transparency", searchTags = {"armor", "transparency", "opacity", "alpha"})
        @Slider(min = 0.0f, max = 1.0f, step = 0.05f)
        public float armorTransparency = 1.0f;
    }


    @Category(name = "Hotbar", description = "Hotbar replacement settings", searchTags = {"hotbar", "replace", "custom", "offhand", "slot"})
    public class Hotbar {

        @ConfigOption(name = "Replace Hotbar", description = "Replace vanilla hotbar with custom", searchTags = {"hotbar", "replace", "custom", "enable"})
        @Switch
        public boolean hotbarEnabled = false;

        @ConfigOption(name = "Vertical", description = "Vertical hotbar layout", searchTags = {"vertical", "layout", "orientation"})
        @Switch
        public boolean hotbarVertical = false;

        @ConfigOption(name = "Show Offhand", description = "Show offhand slot", searchTags = {"offhand", "slot", "show"})
        @Switch
        public boolean hotbarShowOffhand = true;

        @ConfigOption(name = "Active Slot Outline", description = "Outline active slot instead of filling", searchTags = {"active", "slot", "outline", "highlight"})
        @Switch
        public boolean hotbarActiveSlotOutline = false;

        @ConfigOption(name = "Active Slot Color", description = "Active slot highlight color", searchTags = {"active", "slot", "color", "highlight"})
        @ColorPicker(allowAlpha = true)
        public int hotbarActiveSlotColor = 0xFF89B4FA;

        @ConfigOption(name = "Transparency", description = "Hotbar transparency", searchTags = {"hotbar", "transparency", "opacity", "alpha"})
        @Slider(min = 0.0f, max = 1.0f, step = 0.05f)
        public float hotbarTransparency = 1.0f;
    }


    /** Runtime visibility state for rendering */
    public transient boolean visible = true;

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

    public Anchor hotbarAnchor = new Anchor();
    public Position hotbarPosition;
    public float hotbarScale = 1.0f;

    {
        hotbarPosition = new Position();
        hotbarPosition.x = -1;
        hotbarPosition.y = 2;
    }

    public boolean firstJoin = true;
    public float scale = 1.0f;
    public float armorScale = 1.0f;
    public float configScale = 1.0f;

    public transient Colors colors = new Colors();

    public class Colors {
        public int background() { return appearance.backgroundColor; }
        public int border() { return appearance.borderColor; }
        public int text() { return appearance.textColor; }
        public int slots() { return appearance.slotColor; }
    }

    @Override
    public SchrumboHudConfig load() {
        super.load();
        presets = new Presets();
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
        appearance.backgroundColor = ensureAlpha(appearance.backgroundColor, 0xF2);
        appearance.borderColor = ensureAlpha(appearance.borderColor, 0xFF);
        appearance.slotColor = ensureAlpha(appearance.slotColor, 0xB3);
        appearance.textColor = ensureAlpha(appearance.textColor, 0xFF);
    }

    private int ensureAlpha(int color, int defaultAlpha) {
        if (((color >> 24) & 0xFF) == 0) {
            return (defaultAlpha << 24) | (color & 0x00FFFFFF);
        }
        return color;
    }

    public void loadDarkMode() {
        appearance.backgroundColor = 0xFF1c1c1c;
        appearance.borderColor = 0xFF434343;
        appearance.textColor = 0xFFfddbbf;
        appearance.slotColor = 0xFF282828;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    public void loadCatppuccinMocha() {
        appearance.backgroundColor = 0xF21E1E2E;
        appearance.borderColor = 0xFF89B4FA;
        appearance.textColor = 0xFFCDD6F4;
        appearance.slotColor = 0xB3313244;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    public void loadGruvbox() {
        appearance.backgroundColor = 0xE6282828;
        appearance.borderColor = 0xFFFE8019;
        appearance.textColor = 0xFFEBDBB2;
        appearance.slotColor = 0xA63C3836;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    public void loadMonokai() {
        appearance.backgroundColor = 0xEB272822;
        appearance.borderColor = 0xFFF92672;
        appearance.textColor = 0xFFF8F8F2;
        appearance.slotColor = 0xB349483E;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    public void loadDracula() {
        appearance.backgroundColor = 0xF2282A36;
        appearance.borderColor = 0xFFBD93F9;
        appearance.textColor = 0xFFF8F8F2;
        appearance.slotColor = 0xBF44475A;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    public void loadClassic() {
        appearance.backgroundColor = 0x80000000;
        appearance.borderColor = 0x60000000;
        appearance.textColor = 0xFFFFFFFF;
        appearance.slotColor = 0x50000000;
        hotbar.hotbarActiveSlotColor = appearance.borderColor;
    }

    /** Toggles showAlways setting */
    public void toggle() {
        this.general.showAlways = !this.general.showAlways;
        this.visible = this.general.showAlways;
    }

    /** Sets showAlways and syncs visibility */
    public void enableHud(boolean value) {
        this.general.showAlways = value;
        this.visible = value;
    }

    public void enableArmorHud(boolean value) {
        this.armorHud.armorEnabled = value;
    }

    public void enableVerticalMode(boolean value) {
        this.armorHud.armorVertical = value;
    }

    public void enableBackground(boolean value) {
        this.appearance.backgroundEnabled = value;
    }

    public void enableBorder(boolean value) {
        this.appearance.outlineEnabled = value;
    }

    public void enableSlotBackground(boolean value) {
        this.appearance.slotBackgroundEnabled = value;
    }

    public void enableTextShadow(boolean value) {
        this.appearance.textShadowEnabled = value;
    }

    public void enableRoundedCorners(boolean value) {
        this.general.roundedCorners = value;
    }

    public void setBackgroundColor(int color) {
        appearance.backgroundColor = color;
    }

    public void setBorderColor(int color) {
        appearance.borderColor = color;
    }

    public void setTextColor(int color) {
        appearance.textColor = color;
    }

    public void setSlotColor(int color) {
        appearance.slotColor = color;
    }

    public void setArmorOpacity(float opacity) {
        armorHud.armorTransparency = opacity;
    }

    public float getArmorOpacity() {
        return armorHud.armorTransparency;
    }

    public void enableHotbar(boolean value) {
        this.hotbar.hotbarEnabled = value;
    }

    public void setHotbarTransparency(float value) {
        this.hotbar.hotbarTransparency = value;
    }

    public float getHotbarTransparency() {
        return hotbar.hotbarTransparency;
    }
}
