package schrumbo.schrumbohud.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import schrumbo.schlib.config.ConfigProcessor;
import schrumbo.schlib.gui.theme.Theme;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.ChatUtils;

/**
 * HUD toggle and config screen keybindings
 */
public class KeybindHandler {
    private static KeyMapping configKey;
    private static KeyMapping toggleKey;
    private static KeyMapping peekKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("schrumbohud:main"));

    public static void register(){
        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Open Config",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Toggle Show Always",
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));
        peekKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Peek HUD",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SchrumboHUDClient.config.showAlways) {
                SchrumboHUDClient.config.visible = true;
            } else {
                SchrumboHUDClient.config.visible = peekKey.isDown();
            }

            if (toggleKey.consumeClick()) {
                SchrumboHUDClient.config.toggle();
                SchrumboHUDClient.config.save();
                ChatUtils.modMessage(SchrumboHUDClient.config.showAlways ? "Show Always: ON" : "Show Always: OFF");
            }

            if (configKey.consumeClick()) {
                client.setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Component.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            }
        });
    }

}
