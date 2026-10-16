package io.github.traelmyx.parkour.test;

import io.github.traelmyx.parkour.ai.BlockType;
import io.github.traelmyx.parkour.ai.BlockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
//? if >=26 {
import net.minecraft.world.entity.EntityTypes;
//?}

import java.util.concurrent.atomic.AtomicBoolean;

public final class ParkourTests {
	public static final int COURSE_TICKS = 240;
	private static final int PLATFORM_Y = 4;
	private static final int GAP_X = 4;

	private ParkourTests() {
	}

	//? if >=26 {
	private static final EntityType<? extends Mob> PARKOUR_MOB = EntityTypes.ZOMBIE;
	private static final EntityType<? extends Mob> PLAIN_MOB = EntityTypes.HUSK;
	//?} else {
	/*private static final EntityType<? extends Mob> PARKOUR_MOB = EntityType.ZOMBIE;
	private static final EntityType<? extends Mob> PLAIN_MOB = EntityType.HUSK;
	*///?}

	public static void zombieJumpsGap(GameTestHelper helper) {
		buildGapCourse(helper, Blocks.AIR);
		Mob mob = runCourse(helper, PARKOUR_MOB);
		helper.succeedWhen(() -> helper.assertTrue(onFarPlatform(helper, mob), "zombie has not crossed the gap"));
	}

	public static void huskDoesNotJump(GameTestHelper helper) {
		buildGapCourse(helper, Blocks.AIR);
		Mob mob = runCourse(helper, PLAIN_MOB);
		helper.runAtTickTime(COURSE_TICKS - 40, () -> {
			helper.assertFalse(onFarPlatform(helper, mob), "husk crossed the gap without the parkour goal");
			helper.succeed();
		});
	}

	public static void zombieJumpsWater(GameTestHelper helper) {
		buildGapCourse(helper, Blocks.WATER);
		Mob mob = runCourse(helper, PARKOUR_MOB);
		AtomicBoolean gotWet = new AtomicBoolean();
		helper.onEachTick(() -> {
			if (mob.isInWater()) gotWet.set(true);
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(onFarPlatform(helper, mob), "zombie has not crossed the water");
			helper.assertFalse(gotWet.get(), "zombie waded through the water instead of jumping");
		});
	}

	public static void blockClassification(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 2, 1), Blocks.WATER);
		helper.setBlock(new BlockPos(5, 2, 1), Blocks.LAVA);
		helper.setBlock(new BlockPos(1, 2, 3), Blocks.OAK_FENCE);
		helper.setBlock(new BlockPos(3, 1, 3), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 2, 3), Blocks.CACTUS);
		helper.setBlock(new BlockPos(5, 1, 3), Blocks.STONE);
		helper.setBlock(new BlockPos(5, 2, 3), Blocks.FIRE);

		assertType(helper, new BlockPos(1, 2, 1), BlockType.WALKABLE);
		assertType(helper, new BlockPos(1, 2, 5), BlockType.PASSABLE_OBSTACLE);
		assertType(helper, new BlockPos(3, 2, 1), BlockType.PASSABLE_OBSTACLE);
		assertType(helper, new BlockPos(5, 2, 1), BlockType.SOLID_OBSTACLE);
		assertType(helper, new BlockPos(1, 2, 3), BlockType.BLOCKED);
		assertType(helper, new BlockPos(3, 2, 3), BlockType.SOLID_OBSTACLE);
		assertType(helper, new BlockPos(5, 2, 3), BlockType.PASSABLE_OBSTACLE);
		helper.succeed();
	}

	private static void assertType(GameTestHelper helper, BlockPos relative, BlockType expected) {
		BlockType actual = BlockTypes.of(helper.getLevel(), helper.absolutePos(relative));
		helper.assertTrue(actual == expected, "expected " + expected + " at " + relative.toShortString() + ", got " + actual);
	}

	private static void buildGapCourse(GameTestHelper helper, Block gapFill) {
		for (int z = 0; z <= 7; z++) {
			for (int x = 0; x <= 7; x++) {
				if (x != GAP_X) {
					helper.setBlock(new BlockPos(x, PLATFORM_Y, z), Blocks.STONE);
				} else if (gapFill != Blocks.AIR) {
					helper.setBlock(new BlockPos(x, PLATFORM_Y - 1, z), Blocks.STONE);
					helper.setBlock(new BlockPos(x, PLATFORM_Y, z), z == 0 || z == 7 ? Blocks.STONE : gapFill);
				}
			}
		}
	}

	private static Mob runCourse(GameTestHelper helper, EntityType<? extends Mob> type) {
		Mob mob = helper.spawn(type, new Vec3(1.5, PLATFORM_Y + 1, 3.5));
		mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		Vec3 target = helper.absoluteVec(new Vec3(6.5, PLATFORM_Y + 1, 3.5));
		helper.onEachTick(() -> {
			if (helper.getTick() % 10 == 0 && !mob.getNavigation().isInProgress()) {
				mob.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
			}
		});
		return mob;
	}

	private static boolean onFarPlatform(GameTestHelper helper, Mob mob) {
		Vec3 rel = mob.position().subtract(helper.absoluteVec(Vec3.ZERO));
		return mob.isAlive() && mob.onGround() && rel.x >= GAP_X + 1 && rel.y >= PLATFORM_Y + 1 - 0.01;
	}
}
