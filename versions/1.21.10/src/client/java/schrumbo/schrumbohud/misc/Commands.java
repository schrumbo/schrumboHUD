package schrumbo.schrumbohud.misc;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
//danke julianh06!
public class Commands {

    /**
     * helper method for creating commands
     * @param name
     * @param action
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

    /**
     *  initializes commands
     */
    public static void register(){
        registerCommand("shud", () -> {
            MinecraftClient.getInstance().send(() -> {
                MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
            });
        });

        registerCommand("schrumbohud", () -> {
            MinecraftClient.getInstance().send(() -> {
                MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
            });
        });

    }

}
