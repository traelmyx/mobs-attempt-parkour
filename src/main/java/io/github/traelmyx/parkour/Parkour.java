package io.github.traelmyx.parkour;

import io.github.traelmyx.parkour.config.ParkourConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class Parkour {
	public static final String MOD_ID = "mobs_attempt_parkour";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static ParkourConfig config = ParkourConfig.defaults();

	private Parkour() {
	}

	public static void init(Path configDir) {
		config = ParkourConfig.load(configDir.resolve(MOD_ID).resolve("config.json"));
		LOGGER.info("Parkour AI enabled for {} mob types", config.size());
	}

	public static ParkourConfig config() {
		return config;
	}

	public static void setConfig(ParkourConfig newConfig) {
		config = newConfig;
	}
}
