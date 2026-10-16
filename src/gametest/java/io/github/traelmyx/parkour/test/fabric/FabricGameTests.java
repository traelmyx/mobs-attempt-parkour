package io.github.traelmyx.parkour.test.fabric;

//? if fabric {
import io.github.traelmyx.parkour.test.ParkourTests;
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
*///?}

//? if >=1.21.5 {
public class FabricGameTests {
	private static final String EMPTY = "mobs_attempt_parkour_gametest:empty";

	@GameTest(structure = EMPTY, maxTicks = ParkourTests.COURSE_TICKS, maxAttempts = 5, requiredSuccesses = 5)
	public void zombieJumpsGap(GameTestHelper helper) {
		ParkourTests.zombieJumpsGap(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = ParkourTests.COURSE_TICKS)
	public void huskDoesNotJump(GameTestHelper helper) {
		ParkourTests.huskDoesNotJump(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = ParkourTests.COURSE_TICKS, maxAttempts = 5, requiredSuccesses = 3)
	public void zombieJumpsWater(GameTestHelper helper) {
		ParkourTests.zombieJumpsWater(helper);
	}

	@GameTest(structure = EMPTY)
	public void blockClassification(GameTestHelper helper) {
		ParkourTests.blockClassification(helper);
	}
}
//?} else {
/*public class FabricGameTests implements FabricGameTest {
	private static final String EMPTY = "mobs_attempt_parkour_gametest:empty";

	@GameTest(template = EMPTY, timeoutTicks = ParkourTests.COURSE_TICKS, attempts = 5, requiredSuccesses = 5)
	public void zombieJumpsGap(GameTestHelper helper) {
		ParkourTests.zombieJumpsGap(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = ParkourTests.COURSE_TICKS)
	public void huskDoesNotJump(GameTestHelper helper) {
		ParkourTests.huskDoesNotJump(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = ParkourTests.COURSE_TICKS, attempts = 5, requiredSuccesses = 3)
	public void zombieJumpsWater(GameTestHelper helper) {
		ParkourTests.zombieJumpsWater(helper);
	}

	@GameTest(template = EMPTY)
	public void blockClassification(GameTestHelper helper) {
		ParkourTests.blockClassification(helper);
	}
}
*///?}
//?}
