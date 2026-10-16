package io.github.traelmyx.parkour.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.github.traelmyx.parkour.Parkour;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class ParkourConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String CONFIG_VERSION = "0.2.1";

	private static final Object[][] DEFAULTS = {
		{1, "minecraft:zombie"}, {1, "minecraft:zombie_villager"}, {1, "minecraft:zombified_piglin"},
		{1, "minecraft:silverfish"}, {1, "minecraft:endermite"}, {1, "minecraft:enderman"},
		{1, "minecraft:wither_skeleton"}, {1, "minecraft:skeleton"}, {2, "minecraft:spider"},
		{2, "minecraft:cave_spider"}, {1, "minecraft:pillager"}, {1, "minecraft:vindicator"},
		{3, "minecraft:illusioner"}, {2, "minecraft:witch"}, {0, "minecraft:iron_golem"}, {3, "minecraft:creeper"},
	};

	private final Map<String, Integer> priorities;

	private ParkourConfig(Map<String, Integer> priorities) {
		this.priorities = priorities;
	}

	public static ParkourConfig defaults() {
		Map<String, Integer> map = new LinkedHashMap<>();
		for (Object[] d : DEFAULTS) map.put((String) d[1], (Integer) d[0]);
		return new ParkourConfig(map);
	}

	public static ParkourConfig of(Map<String, Integer> priorities) {
		return new ParkourConfig(new LinkedHashMap<>(priorities));
	}

	public OptionalInt priorityFor(String entityTypeId) {
		Integer p = priorities.get(entityTypeId);
		return p == null ? OptionalInt.empty() : OptionalInt.of(p);
	}

	public int size() {
		return priorities.size();
	}

	public static ParkourConfig load(Path file) {
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				FileFormat format = GSON.fromJson(reader, FileFormat.class);
				if (format == null || format.data == null) throw new JsonParseException("missing \"data\"");
				Map<String, Integer> map = new LinkedHashMap<>();
				for (Entry e : format.data) {
					if (e != null && e.mobId != null) map.put(e.mobId, e.priority);
				}
				return new ParkourConfig(map);
			} catch (IOException | RuntimeException e) {
				Parkour.LOGGER.error("Could not read {}, using the defaults: {}", file, e.toString());
				return defaults();
			}
		}
		ParkourConfig config = defaults();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(config.toFileFormat(), writer);
			}
		} catch (IOException e) {
			Parkour.LOGGER.warn("Could not write default config {}: {}", file, e.toString());
		}
		return config;
	}

	private FileFormat toFileFormat() {
		FileFormat format = new FileFormat();
		format.configVersion = CONFIG_VERSION;
		format.data = new ArrayList<>();
		priorities.forEach((id, p) -> {
			Entry e = new Entry();
			e.priority = p;
			e.mobId = id;
			format.data.add(e);
		});
		return format;
	}

	private static final class FileFormat {
		List<Entry> data;
		String configVersion;
	}

	private static final class Entry {
		int priority;
		String mobId;
	}
}
