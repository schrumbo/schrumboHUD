package schrumbo.schrumbohud.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.text.Text;
import schrumbo.schlib.config.ConfigProcessor;
import schrumbo.schlib.gui.theme.Theme;
import schrumbo.schrumbohud.SchrumboHUDClient;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigProcessor.createScreen(
            SchrumboHUDClient.config,
            Text.literal("SchrumboHUD Config"),
            new Theme()
        );
    }
}
