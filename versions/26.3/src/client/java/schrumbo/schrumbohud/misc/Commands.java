package schrumbo.schrumbohud.misc;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import schrumbo.schimgui.config.ImGuiConfigScreen;
import schrumbo.schrumbohud.SchrumboHUDClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Client-side chat commands (/shud, /schrumbohud)
 */
public class Commands {

    public static void registerCommand(String name, Runnable action) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal(name).executes(context -> {
                action.run();
                return 1;
            }))
        );
    }

    public static void register() {
        Runnable openConfig = () -> Minecraft.getInstance().execute(() ->
            Minecraft.getInstance().setScreenAndShow(
                ImGuiConfigScreen.create(SchrumboHUDClient.config, "SchrumboHUD")
            )
        );

        registerCommand("shud", openConfig);
        registerCommand("schrumbohud", openConfig);
    }
}
