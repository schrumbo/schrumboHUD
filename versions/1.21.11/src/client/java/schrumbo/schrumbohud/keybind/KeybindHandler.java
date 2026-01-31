package schrumbo.schrumbohud.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import schrumbo.schlib.config.ConfigProcessor;
import schrumbo.schlib.gui.theme.Theme;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.ChatUtils;

/**
 * handles keybindings
 */
public class KeybindHandler {
    private static KeyBinding toggleHudKey;
    private static KeyBinding configKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("schrumbohud", "main"));


    /**
     * registers all keybinds
     */
    public static void register(){
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle InventoryHUD",
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "ClickGUI",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(toggleHudKey.wasPressed()){
                ChatUtils.modMessage("Toggled InventoryHUD");
                SchrumboHUDClient.config.toggle();
                SchrumboHUDClient.config.save();
            }

            if(configKey.wasPressed()){
                client.setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Text.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            }
            });


    }

}
