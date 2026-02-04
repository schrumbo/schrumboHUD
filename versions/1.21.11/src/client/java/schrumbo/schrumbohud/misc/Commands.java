package schrumbo.schrumbohud.misc;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import schrumbo.schlib.config.ConfigProcessor;
import schrumbo.schlib.gui.theme.Theme;
import schrumbo.schrumbohud.SchrumboHUDClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
            MinecraftClient.getInstance().send(() -> {
                MinecraftClient.getInstance().setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Text.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            });
        });

        registerCommand("schrumbohud", () -> {
            MinecraftClient.getInstance().send(() -> {
                MinecraftClient.getInstance().setScreen(ConfigProcessor.createScreen(
                    SchrumboHUDClient.config,
                    Text.literal("SchrumboHUD Config"),
                    new Theme()
                ));
            });
        });

    }

}
