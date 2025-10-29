package schrumbo.schrumbohud;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schrumbo.schrumbohud.command.CommandHandler;
import schrumbo.schrumbohud.config.ConfigManager;
import schrumbo.schrumbohud.config.HudConfig;
import schrumbo.schrumbohud.hud.InventoryRenderer;
import schrumbo.schrumbohud.keybind.KeybindHandler;

public class SchrumboHUDClient implements ClientModInitializer {
	public static final String MOD_ID = "schrumbohud";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConfigManager configManager;
	public static HudConfig config;
	private double initTime;

	@Override
	public void onInitializeClient() {
		initTime = Util.getMeasuringTimeMs();
		LOGGER.info("initializing SchrumboHUD");

		configManager = new ConfigManager();
		config = configManager.load();

		KeybindHandler.register();
		InventoryRenderer.register();
		CommandHandler.register();
		config.initColors();

		initTime = Util.getMeasuringTimeMs() - initTime;
		LOGGER.info("SchrumboHUD initialized in " + initTime + "ms");
	}


}