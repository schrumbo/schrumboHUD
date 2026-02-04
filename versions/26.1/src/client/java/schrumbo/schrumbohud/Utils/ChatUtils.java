package schrumbo.schrumbohud.Utils;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import schrumbo.schrumbohud.SchrumboHUDClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Prefixed chat message utilities
 */
public class ChatUtils {
    private static final Minecraft client = Minecraft.getInstance();

    private static final Component PREFIX = Component.literal("§8[").append(Component.literal("InventoryHUD").withColor(SchrumboHUDClient.config.borderColor)).append(Component.literal("§8] "));

    public static void modMessage(String message){
        if(client.player == null)return;
        client.player.displayClientMessage(PREFIX.copy().append(Component.literal("\u00A7f" + message)), false);
    }

}
