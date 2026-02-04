package schrumbo.schrumbohud.misc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.Utils.ChatUtils;

/**
 * First-join welcome message handler
 */
public class FirstJoin {
    public static void register(){

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (SchrumboHUDClient.config.firstJoin) {
                client.execute(() -> {
                    if (client.player != null) {
                        ChatUtils.modMessage("Thanks for using my mod!");
                        ChatUtils.modMessage("Toggle the HUD with L");
                        ChatUtils.modMessage("Open the Settings with RShift");
                    }
                });
                SchrumboHUDClient.config.firstJoin = false;
            }
        });
    }
}
