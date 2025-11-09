package schrumbo.schrumbohud.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import schrumbo.schrumbohud.SchrumboHUDClient;

public class ChatUtils {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final Text PREFIX = Text.literal("§8[").append(Text.literal("InventoryHUD").styled(style -> style.withColor(SchrumboHUDClient.config.guicolors.accent))).append(Text.literal("§8] "));

    public static void modMessage(String message){
        if(client.player == null)return;
        client.player.sendMessage( PREFIX.copy().append(Text.literal("§f" + message)), false);
    }

}
