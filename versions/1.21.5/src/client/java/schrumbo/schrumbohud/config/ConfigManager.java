package schrumbo.schrumbohud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schrumbo.schrumbohud.SchrumboHUD;
import schrumbo.schrumbohud.Utils.ChatUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Used for handling the config writing and reading
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER =  LoggerFactory.getLogger(SchrumboHUD.class.getName());
    private static HudConfig config = new HudConfig();
    private final MinecraftClient client = MinecraftClient.getInstance();

    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "schrumbohud.json"
    );

    /**
     * loads config from file - creates new if needed
     */
    public static HudConfig load(){
        if(!CONFIG_FILE.exists()){
            LOGGER.info("creating new config file");
            save();
            return new HudConfig();
        }

        try(FileReader reader = new FileReader(CONFIG_FILE)){
            config = GSON.fromJson(reader, HudConfig.class);
        }catch(IOException e){
            config = new HudConfig();
            LOGGER.info("failed to read config, using default");
        }
        return config;
    }

    /**
     * writes config changes to file
     */
    public static void save(){
        try(FileWriter writer = new FileWriter(CONFIG_FILE)){
            GSON.toJson(config, writer);
        }catch (IOException e){
            LOGGER.info("failed to write changes to config" + e);
        }
    }
}
