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
    private static KeyMapping toggleHudKey;
    private static KeyMapping configKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("schrumbohud:main"));

    public static void register(){
        toggleHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Toggle InventoryHUD",
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));
        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "ClickGUI",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(toggleHudKey.consumeClick()){
                ChatUtils.modMessage("Toggled InventoryHUD");
                SchrumboHUDClient.config.toggle();
                SchrumboHUDClient.config.save();
            }

            if(configKey.consumeClick()){
                client.setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Component.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            }
            });


    }

}
