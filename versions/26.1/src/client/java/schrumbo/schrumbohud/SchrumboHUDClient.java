package schrumbo.schrumbohud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schrumbo.schrumbohud.config.SchrumboHudConfig;
import schrumbo.schrumbohud.hud.ArmorRenderer;
import schrumbo.schrumbohud.hud.ArmorHudSpecialRenderer;
import schrumbo.schrumbohud.hud.InventoryHudSpecialRenderer;
import schrumbo.schrumbohud.hud.InventoryRenderer;
import schrumbo.schrumbohud.keybind.KeybindHandler;
import schrumbo.schrumbohud.misc.Commands;
import schrumbo.schrumbohud.misc.FirstJoin;

/**
 * Client mod entrypoint
 */
public class SchrumboHUDClient implements ClientModInitializer {
	public static final String MOD_ID = "schrumbohud";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static SchrumboHudConfig config;
	private double initTime;

	@Override
	public void onInitializeClient() {
		initTime = Util.getMillis();
		LOGGER.info("initializing SchrumboHUD");
		config = new SchrumboHudConfig().load();

		PictureInPictureRendererRegistry.register(ctx ->
				new InventoryHudSpecialRenderer(ctx.bufferSource())
		);
		PictureInPictureRendererRegistry.register(ctx ->
				new ArmorHudSpecialRenderer(ctx.bufferSource())
		);

		KeybindHandler.register();
		InventoryRenderer.register();
		ArmorRenderer.register();
		FirstJoin.register();
		Commands.register();

		initTime = Util.getMillis() - initTime;
		LOGGER.info("SchrumboHUD initialized in " + initTime + "ms");
	}


}