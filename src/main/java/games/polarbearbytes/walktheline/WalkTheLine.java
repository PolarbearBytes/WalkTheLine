package games.polarbearbytes.walktheline;

import games.polarbearbytes.walktheline.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalkTheLine implements ModInitializer {
	public static final String MOD_ID = "walk-the-line";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server = null;

	@Override
	public void onInitialize() {
		ConfigManager.loadConfig();
		ServerEvents.register();
	}
}