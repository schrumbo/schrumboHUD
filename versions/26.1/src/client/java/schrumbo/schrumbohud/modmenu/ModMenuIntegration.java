package schrumbo.schrumbohud.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import schrumbo.schimgui.config.ImGuiConfigScreen;

import schrumbo.schrumbohud.SchrumboHUDClient;

/**
 * ModMenu config screen integration
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ImGuiConfigScreen.create(
            SchrumboHUDClient.config,
            "SchrumboHUD"
        );
    }
}
