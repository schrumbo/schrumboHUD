package schrumbo.schrumbohud.misc;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import schrumbo.schlib.config.ConfigProcessor;
import schrumbo.schlib.gui.theme.Theme;
import schrumbo.schrumbohud.SchrumboHUDClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Client-side chat commands
 */
public class Commands {

    /**
     * Register a client command bound to the given action
     */
    public static void registerCommand(String name, Runnable action) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal(name).executes(context -> {
                        action.run();
                        return 1;
                    })
            );
        });
    }

    public static void register(){
        registerCommand("shud", () -> {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Component.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            });
        });

        registerCommand("schrumbohud", () -> {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Component.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            });
        });

    }

}
