package io.github.traelmyx.parkour.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
//? if <26 {
/*import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameRules;
*///?}

import java.nio.file.Files;

public final class ClientSmoke {
	public static final boolean ENABLED = Boolean.getBoolean("smoketest");
	private static final int SETTLE_TICKS = 200;
	private static final long TIMEOUT_MS = 5 * 60 * 1000;

	private enum State { WAIT_TITLE, WAIT_WORLD, SETTLE, DONE }

	private static State state = State.WAIT_TITLE;
	private static int ticks;

	private ClientSmoke() {
	}

	public static boolean start() {
		if (!ENABLED) return false;
		Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(TIMEOUT_MS);
			} catch (InterruptedException e) {
				return;
			}
			fail("timed out in state " + state);
		}, "clientsmoke-watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
		return true;
	}

	public static void tick(Minecraft mc) {
		try {
			switch (state) {
				case WAIT_TITLE -> {
					if (screen(mc) != null && overlayHidden(mc) && ++ticks >= 20) {
						if (screen(mc).getClass().getSimpleName().equals("LoadingErrorScreen")) fail("mod loading errors or warnings, see the log");
						System.out.println("[clientsmoke] menu ready (" + screen(mc).getClass().getSimpleName() + "), creating world");
						ticks = 0;
						createWorld(mc);
						state = State.WAIT_WORLD;
					}
				}
				case WAIT_WORLD -> {
					if (mc.level != null && mc.player != null && screen(mc) == null) state = State.SETTLE;
				}
				case SETTLE -> {
					if (++ticks >= SETTLE_TICKS) {
						screenshot(mc);
						Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
						System.out.println("[clientsmoke] PASSED");
						state = State.DONE;
						mc.stop();
					}
				}
				case DONE -> {}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.toString());
		}
	}

	//? if >=26 {
	private static Object screen(Minecraft mc) {
		return mc.gui.screen();
	}

	private static boolean overlayHidden(Minecraft mc) {
		return mc.gui.overlay() == null;
	}

	private static void createWorld(Minecraft mc) {
		LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE,
			new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
		mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
			registries -> registries.lookupOrThrow(Registries.WORLD_PRESET)
				.getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
			mc.gui.screen());
	}

	private static void screenshot(Minecraft mc) {
		Screenshot.grab(mc, false);
	}
	//?} else {
	/*private static Screen screen(Minecraft mc) {
		return mc.screen;
	}

	private static boolean overlayHidden(Minecraft mc) {
		return mc.getOverlay() == null;
	}

	private static void createWorld(Minecraft mc) {
		LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL,
			true, new GameRules(), WorldDataConfiguration.DEFAULT);
		mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
			registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
				.getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
			mc.screen);
	}

	private static void screenshot(Minecraft mc) {
		Screenshot.grab(mc.gameDirectory, "parkour-singleplayer.png", mc.getMainRenderTarget(), msg -> {});
	}
	*///?}

	public static void fail(String why) {
		System.err.println("[clientsmoke] FAILED: " + why);
		System.err.flush();
		Runtime.getRuntime().halt(1);
	}
}
