package schrumbo.schrumbohud.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schrumbo.schrumbohud.SchrumboHUD;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;

import javax.swing.text.JTextComponent;

/**
 * handles keybindings
 */
public class KeybindHandler {
    private static KeyBinding toggleHudKey;
    private static KeyBinding configKey;
    private static final String CATEGORY = "SchrumboHUD";
    /**
     * registers all keybinds
     */
    public static void register(){
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle InventoryHUD",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "ClickGUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(toggleHudKey.wasPressed()){
                SchrumboHUDClient.config.toggle();
                ConfigManager.save();
            }

            if(configKey.wasPressed()){
                client.setScreen(new ClickGuiScreen());
            }
            });


    }

}
