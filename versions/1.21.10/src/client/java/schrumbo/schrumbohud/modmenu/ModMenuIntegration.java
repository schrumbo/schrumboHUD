package schrumbo.schrumbohud.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import schrumbo.schrumbohud.clickgui.ClickGuiScreen;

/**
 * integration of the ModMenu Mod
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ClickGuiScreen();
    }
}
