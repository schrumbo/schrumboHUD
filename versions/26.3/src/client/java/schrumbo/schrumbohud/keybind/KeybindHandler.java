package schrumbo.schrumbohud.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import schrumbo.schimgui.config.ImGuiConfigScreen;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.ChatUtils;

/**
 * Keybindings for HUD toggle, peek, and config screen
 */
public class KeybindHandler {

    private static KeyMapping configKey;
    private static KeyMapping toggleKey;
    private static KeyMapping peekKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("schrumbohud:main"));

    public static void register() {
        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.schrumbohud.open_config", InputConstants.KEY_RSHIFT, CATEGORY));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.schrumbohud.toggle_show_always", InputConstants.KEY_L, CATEGORY));
        peekKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.schrumbohud.peek_hud", InputConstants.UNKNOWN.getValue(), CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SchrumboHUDClient.config.general.showAlways) {
                SchrumboHUDClient.config.visible = true;
            } else {
                SchrumboHUDClient.config.visible = peekKey.isDown();
            }

            if (toggleKey.consumeClick()) {
                SchrumboHUDClient.config.toggle();
                SchrumboHUDClient.config.save();
                ChatUtils.modMessage(SchrumboHUDClient.config.general.showAlways ? "ON" : "OFF");
            }

            if (configKey.consumeClick()) {
                client.setScreenAndShow(ImGuiConfigScreen.create(SchrumboHUDClient.config, "SchrumboHUD"));
            }
        });
    }
}
