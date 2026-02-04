package schrumbo.schrumbohud.Utils;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Prefixed chat message utilities
 */
public class ChatUtils {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final Text PREFIX = Text.literal("§8[").append(Text.literal("InventoryHUD").styled(style -> style.withColor(SchrumboHUDClient.config.borderColor))).append(Text.literal("§8] "));

    public static void modMessage(String message){
        if(client.player == null)return;
        client.player.sendMessage( PREFIX.copy().append(Text.literal("§f" + message)), false);
    }

}
