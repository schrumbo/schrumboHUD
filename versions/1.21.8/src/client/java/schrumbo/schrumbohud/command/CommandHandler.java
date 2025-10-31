package schrumbo.schrumbohud.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.hud.HudEditorScreen;

public class CommandHandler {

    public static void register() {
        String[] alias = {"shud", "schrumbohud"};

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            for (String a : alias){
                dispatcher.register(CommandManager.literal(a)
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.currentScreen instanceof ClickGuiScreen) {
                                client.setScreen(null);
                            } else {
                                client.setScreen(new ClickGuiScreen());
                            }
                            return 1;
                        })
                        .then(CommandManager.literal("editpos").executes(context -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    if (client.currentScreen instanceof ClickGuiScreen) {
                                        client.setScreen(new HudEditorScreen(client.currentScreen));
                                    } else {
                                        client.setScreen(new HudEditorScreen(new ClickGuiScreen()));
                                    }
                                    return 1;
                                })
                        )
                );
            }
        });
    }
}
