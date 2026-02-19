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
 * HUD toggle and config screen keybindings
 */
public class KeybindHandler {
    private static KeyBinding configKey;
    private static KeyBinding toggleKey;
    private static KeyBinding peekKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("schrumbohud", "main"));

    public static void register(){
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Config",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Show Always",
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));
        peekKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Peek HUD",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SchrumboHUDClient.config.showAlways) {
                SchrumboHUDClient.config.visible = true;
            } else {
                SchrumboHUDClient.config.visible = peekKey.isPressed();
            }

            if (toggleKey.wasPressed()) {
                SchrumboHUDClient.config.toggle();
                SchrumboHUDClient.config.save();
                ChatUtils.modMessage(SchrumboHUDClient.config.showAlways ? "Show Always: ON" : "Show Always: OFF");
            }

            if (configKey.wasPressed()) {
                client.setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Text.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            }
        });
    }

}
