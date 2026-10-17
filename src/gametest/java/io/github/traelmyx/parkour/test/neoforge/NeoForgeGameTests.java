package io.github.traelmyx.parkour.test.neoforge;

//? if neoforge {
/*import io.github.traelmyx.parkour.test.ClientSmoke;
import io.github.traelmyx.parkour.test.ParkourTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.5 {
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
//?} else {
/^import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
^///?}

//? if <1.21.5 {
/^@GameTestHolder(NeoForgeGameTests.MOD_ID)
@PrefixGameTestTemplate(false)
^///?}
@Mod(NeoForgeGameTests.MOD_ID)
public class NeoForgeGameTests {
	public static final String MOD_ID = "mobs_attempt_parkour_gametest";

	public NeoForgeGameTests(IEventBus modBus, Dist dist) {
		//? if >=1.21.5 {
		FUNCTIONS.register(modBus);
		modBus.addListener(NeoForgeGameTests::registerTests);
		//?}
		if (dist.isClient() && ClientSmoke.start()) {
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> ClientSmoke.tick(net.minecraft.client.Minecraft.getInstance()));
		}
	}

	//? if >=1.21.5 {
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, MOD_ID);
	private static final Identifier EMPTY = Identifier.fromNamespaceAndPath(MOD_ID, "empty");

	static {
		FUNCTIONS.register("zombie_jumps_gap", () -> ParkourTests::zombieJumpsGap);
		FUNCTIONS.register("husk_does_not_jump", () -> ParkourTests::huskDoesNotJump);
		FUNCTIONS.register("zombie_jumps_water", () -> ParkourTests::zombieJumpsWater);
		FUNCTIONS.register("block_classification", () -> ParkourTests::blockClassification);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		//? if >=26 {
		Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));
		//?} else
		/^Holder<TestEnvironmentDefinition> env = event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));^/
		register(event, env, "zombie_jumps_gap", ParkourTests.COURSE_TICKS, 5, 5);
		register(event, env, "husk_does_not_jump", ParkourTests.COURSE_TICKS, 1, 1);
		register(event, env, "zombie_jumps_water", ParkourTests.COURSE_TICKS, 5, 3);
		register(event, env, "block_classification", 100, 1, 1);
	}

	//? if >=26 {
	private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> env, String name, int maxTicks, int attempts, int successes) {
	//?} else
	/^private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition> env, String name, int maxTicks, int attempts, int successes) {^/
		Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
		event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id),
			new TestData<>(env, net.minecraft.world.level.Level.OVERWORLD, EMPTY, maxTicks, 0, true,
				net.minecraft.world.level.block.Rotation.NONE, false, attempts, successes, false, 0)));
	}
	//?} else {
	/^@GameTest(template = "empty", timeoutTicks = ParkourTests.COURSE_TICKS, attempts = 5, requiredSuccesses = 5)
	public static void zombieJumpsGap(GameTestHelper helper) {
		ParkourTests.zombieJumpsGap(helper);
	}

	@GameTest(template = "empty", timeoutTicks = ParkourTests.COURSE_TICKS)
	public static void huskDoesNotJump(GameTestHelper helper) {
		ParkourTests.huskDoesNotJump(helper);
	}

	@GameTest(template = "empty", timeoutTicks = ParkourTests.COURSE_TICKS, attempts = 5, requiredSuccesses = 3)
	public static void zombieJumpsWater(GameTestHelper helper) {
		ParkourTests.zombieJumpsWater(helper);
	}

	@GameTest(template = "empty")
	public static void blockClassification(GameTestHelper helper) {
		ParkourTests.blockClassification(helper);
	}
	^///?}
}
*///?}
