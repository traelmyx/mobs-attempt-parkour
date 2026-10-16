package io.github.traelmyx.parkour.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class JumpToTargetGoal extends Goal {
	private static final double MIN_TARGET_DISTANCE = 1.5;
	private static final double CLEARANCE_ABOVE_HEAD = 1.0;
	private static final int AIR_CONTROL_TICKS = 40;
	private static final double EDGE_DETECTION_DISTANCE = 2.0;
	private static final int DETECTION_POINTS = (int) Math.floor(EDGE_DETECTION_DISTANCE * 8);
	private static final double MOVE_SPEED = 1.0;
	private static final double AIR_MOVE_SPEED = 5.0;
	private static final double GRAVITY = 0.1;
	private static final double Y_VELOCITY_SCALE = 1.53;
	private static final double JUMP_NOISE = 0.1;
	private static final int MAX_TICKS_TO_REACH_EDGE = 40;

	private static final boolean DEBUG = Boolean.getBoolean("parkour.debug");

	private final Mob mob;
	private Jump jump;
	private int airTicks;

	private static final class Jump {
		final double horizontalVelocity;
		final double verticalEffort;
		final Vec3 direction;
		final Vec3 edgePos;
		int ticksAttempted;

		Jump(double horizontalVelocity, double verticalEffort, Vec3 direction, Vec3 edgePos) {
			this.horizontalVelocity = horizontalVelocity;
			this.verticalEffort = verticalEffort;
			this.direction = direction;
			this.edgePos = edgePos;
		}
	}

	public JumpToTargetGoal(Mob mob) {
		this.mob = mob;
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (airTicks > 0) return true;
		if (jump != null) return true;
		if (!mob.onGround()) return false;

		Path path = mob.getNavigation().getPath();
		if (path == null) return false;

		boolean obstacleOnPath = false;
		for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
			BlockType type = BlockTypes.of(mob.level(), path.getNode(i).asBlockPos());
			if (type == BlockType.PASSABLE_OBSTACLE || type == BlockType.SOLID_OBSTACLE) {
				obstacleOnPath = true;
				break;
			}
		}
		if (mob.getNavigation().isInProgress() && path.canReach() && !obstacleOnPath) return false;

		BlockPos targetPos = mob.getNavigation().getTargetPos();
		if (targetPos == null) return false;
		Vec3 target = Vec3.atBottomCenterOf(targetPos);
		if (target.distanceTo(mob.position()) < MIN_TARGET_DISTANCE) return false;

		jump = findJump(target);
		if (DEBUG && jump != null) debug("plan edge=%.2f,%.2f dir=%.2f,%.2f vel=%.3f effort=%.2f", jump.edgePos.x, jump.edgePos.z, jump.direction.x, jump.direction.z, jump.horizontalVelocity, jump.verticalEffort);
		return jump != null;
	}

	@Override
	public boolean canContinueToUse() {
		return airTicks > 0 || jump != null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void stop() {
		jump = null;
		airTicks = 0;
	}

	@Override
	public void tick() {
		if (airTicks > 0) {
			steerInAir();
			return;
		}
		if (jump == null) return;

		if (++jump.ticksAttempted > MAX_TICKS_TO_REACH_EDGE) {
			if (DEBUG) debug("abort: edge not reached in %d ticks", MAX_TICKS_TO_REACH_EDGE);
			mob.getNavigation().stop();
			jump = null;
			return;
		}

		if (!atEdge(jump)) {
			mob.getMoveControl().setWantedPosition(jump.edgePos.x, jump.edgePos.y, jump.edgePos.z, MOVE_SPEED);
			return;
		}

		leap(jump);
	}

	private Jump currentAirJump;

	private boolean atEdge(Jump jump) {
		if (mob.blockPosition().equals(BlockPos.containing(jump.edgePos))) return true;
		double along = (mob.getX() - jump.edgePos.x) * jump.direction.x + (mob.getZ() - jump.edgePos.z) * jump.direction.z;
		return along >= 0;
	}

	private void leap(Jump jump) {
		if (DEBUG) debug("leap");
		double noise = (mob.getRandom().nextDouble() * 2 - 1) * JUMP_NOISE;
		leapTowards(mob.position().add(jump.direction), jump.horizontalVelocity * (1 + noise),
			jump.verticalEffort > 0 ? 0.0 : 0.1);
		if (jump.verticalEffort > 0) mob.getJumpControl().jump();

		mob.getNavigation().stop();
		currentAirJump = jump;
		airTicks = AIR_CONTROL_TICKS;
		this.jump = null;
	}

	private void steerInAir() {
		airTicks--;
		if (!mob.isAlive() || (mob.onGround() && airTicks < AIR_CONTROL_TICKS - 2)) {
			airTicks = 0;
			return;
		}
		Vec3 movePos = mob.position().add(currentAirJump.direction.scale(3.0));
		if (!mob.onGround()) mob.getMoveControl().setWantedPosition(movePos.x, movePos.y, movePos.z, AIR_MOVE_SPEED);
	}

	private void debug(String fmt, Object... args) {
		System.out.printf("[parkour] %s#%d t=%d pos=%.2f,%.2f,%.2f %s%n", net.minecraft.world.entity.EntityType.getKey(mob.getType()).getPath(), mob.getId(),
			mob.tickCount, mob.getX(), mob.getY(), mob.getZ(), String.format(fmt, args));
	}

	private void leapTowards(Vec3 target, double horizontalVelocity, double yVelocity) {
		Vec3 dir = target.subtract(mob.position()).normalize();
		Vec3 leap = new Vec3(dir.x, 0, dir.z).normalize().scale(horizontalVelocity);
		Vec3 velocity = mob.getDeltaMovement();
		double clampedY = velocity.y < 0.1 ? yVelocity : 0.0;

		Vec3 horizontal = velocity.add(leap.x, 0, leap.z);
		double scale = horizontalVelocity / horizontal.length();
		if (scale < 1) horizontal = horizontal.scale(scale);
		mob.setDeltaMovement(horizontal.add(0, clampedY, 0));
	}

	private Jump findJump(Vec3 target) {
		Vec3 toTarget = target.subtract(mob.position());
		Vec3 targetDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();

		for (int angle = -45; angle <= 45; angle += 5) {
			Vec3 jumpDirection = targetDirection.yRot((float) Math.toRadians(angle));
			if (mob.getDeltaMovement().add(jumpDirection).lengthSqr() < jumpDirection.lengthSqr()) continue;

			Vec3 start = mob.position();
			Vec3 end = start.add(jumpDirection.scale(EDGE_DETECTION_DISTANCE));
			List<Vec3> points = new ArrayList<>(DETECTION_POINTS);
			List<BlockType> types = new ArrayList<>(DETECTION_POINTS);
			for (int i = 0; i < DETECTION_POINTS; i++) {
				Vec3 p = start.lerp(end, i / (double) (DETECTION_POINTS - 1));
				points.add(p);
				types.add(BlockTypes.of(mob.level(), BlockPos.containing(p)));
			}

			int edge = -1;
			boolean walkableRun = false;
			for (int i = 0; i + 1 < types.size(); i++) {
				if (edge < 0 && types.get(i) == BlockType.WALKABLE && types.get(i + 1) == BlockType.PASSABLE_OBSTACLE) edge = i + 1;
				if (types.get(i) == BlockType.WALKABLE && types.get(i + 1) == BlockType.WALKABLE) walkableRun = true;
			}

			if (edge >= 0 && walkableRun) {
				Jump jump = planJump(points.get(edge), jumpDirection);
				if (jump != null) return jump;
			}
		}
		return null;
	}

	private Jump planJump(Vec3 actorPos, Vec3 targetDirection) {
		double maxYVelocity = jumpVelocity();
		int maxJumpHeight = (int) (maxYVelocity * 4);
		double maxHorizontalVelocity = mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * MOVE_SPEED;
		Level level = mob.level();

		for (int[] offset : jumpOffsets(maxHorizontalVelocity)) {
			int x = offset[0], y = offset[1];
			Vec3 jumpToPos = actorPos.add(targetDirection.scale(1.0 + x));
			BlockPos blockPos = BlockPos.containing(jumpToPos);
			Integer groundHeight = findGroundAt(blockPos, y, maxJumpHeight);
			if (groundHeight == null) continue;

			BlockPos walkablePos = new BlockPos(blockPos.getX(), groundHeight, blockPos.getZ());
			VoxelShape shape = level.getBlockState(walkablePos).getCollisionShape(level, walkablePos);
			Vec3 base = Vec3.atLowerCornerOf(walkablePos);
			Vec3 corner = closestCorner(actorPos.subtract(base), shape, 16);
			if (corner == null) continue;
			corner = corner.add(base);
			Vec3 horizontalJumpPos = new Vec3(corner.x, actorPos.y, corner.z);

			double jumpLength = horizontalJumpPos.subtract(actorPos).length() - mob.getBbWidth() * 0.5;
			Vec3 direction = horizontalJumpPos.subtract(actorPos).normalize();
			if (!hasClearance(actorPos, jumpLength, targetDirection)) return null;

			double blockHeight = shape.isEmpty() ? 0.0 : shape.bounds().getYsize();
			double jumpHeight = groundHeight + blockHeight - actorPos.y;

			for (double effort : new double[] {0.0, maxYVelocity}) {
				double xVelocity = requiredHorizontalVelocity(jumpLength, jumpHeight, effort);
				if (xVelocity < maxHorizontalVelocity) return new Jump(xVelocity, effort, direction, actorPos);
			}
			return null;
		}
		return null;
	}

	private static List<int[]> jumpOffsets(double maxHorizontalVelocity) {
		int steps = maxHorizontalVelocity < 0.24 ? 3 : maxHorizontalVelocity < 0.3 ? 4 : 5;
		List<int[]> offsets = new ArrayList<>();
		for (int d = 0; d <= steps; d++) {
			for (int y = 0; y <= steps - d; y++) offsets.add(new int[] {d, y});
		}
		offsets.sort((a, b) -> Integer.compare(a[0] + a[1], b[0] + b[1]));
		return offsets;
	}

	private static double requiredHorizontalVelocity(double jumpLength, double jumpHeight, double yVelocity) {
		double scaledYVelocity = yVelocity * Y_VELOCITY_SCALE;
		double root = Math.sqrt(scaledYVelocity * scaledYVelocity - 4 * -jumpHeight * -GRAVITY);
		if (Double.isNaN(root)) return Double.POSITIVE_INFINITY;
		double numerator = -scaledYVelocity > 0 ? -scaledYVelocity + root : -scaledYVelocity - root;
		double time = numerator / (2 * -GRAVITY);
		if (time == 0.0) return Double.POSITIVE_INFINITY;
		return jumpLength / time;
	}

	private Integer findGroundAt(BlockPos pos, int height, int maxJumpHeight) {
		for (int i = -height; i <= maxJumpHeight; i++) {
			if (BlockTypes.of(mob.level(), pos.above(i)) == BlockType.WALKABLE) return i + pos.getY() - 1;
		}
		return null;
	}

	private boolean hasClearance(Vec3 actorPos, double jumpLength, Vec3 direction) {
		Vec3 start = actorPos.add(0, mob.getBbHeight() + CLEARANCE_ABOVE_HEAD, 0);
		Vec3 end = start.add(direction.scale(jumpLength));
		HitResult hit = mob.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
		return hit.getType() == HitResult.Type.MISS;
	}

	private double jumpVelocity() {
		double velocity = 0.42 * jumpFactor();
		//? if >=26 {
		MobEffectInstance boost = mob.getEffect(MobEffects.JUMP_BOOST);
		//?} else
		/*MobEffectInstance boost = mob.getEffect(MobEffects.JUMP);*/
		if (boost != null) velocity += 0.1 * (boost.getAmplifier() + 1);
		return velocity;
	}

	private double jumpFactor() {
		Level level = mob.level();
		float atFeet = level.getBlockState(mob.blockPosition()).getBlock().getJumpFactor();
		BlockPos below = BlockPos.containing(mob.getX(), mob.getBoundingBox().minY - 0.5000001, mob.getZ());
		float underFeet = level.getBlockState(below).getBlock().getJumpFactor();
		return atFeet == 1.0f ? underFeet : atFeet;
	}

	private Vec3 closestCorner(Vec3 point, VoxelShape shape, int maxSamples) {
		List<Vec3> corners = new ArrayList<>();
		for (AABB box : shape.toAabbs()) addTopCornersAndEdges(box, corners);
		Collections.shuffle(corners, new java.util.Random(mob.getRandom().nextLong()));
		Vec3 best = null;
		double bestDist = Double.MAX_VALUE;
		for (int i = 0; i < Math.min(maxSamples, corners.size()); i++) {
			double d = corners.get(i).distanceToSqr(point);
			if (d < bestDist) {
				bestDist = d;
				best = corners.get(i);
			}
		}
		return best;
	}

	private static void addTopCornersAndEdges(AABB box, List<Vec3> out) {
		double halfX = box.getXsize() * 0.5, halfZ = box.getZsize() * 0.5;
		out.add(new Vec3(box.minX, box.maxY, box.minZ));
		out.add(new Vec3(box.maxX, box.maxY, box.minZ));
		out.add(new Vec3(box.minX, box.maxY, box.maxZ));
		out.add(new Vec3(box.maxX, box.maxY, box.maxZ));
		out.add(new Vec3(box.minX + halfX, box.maxY, box.minZ));
		out.add(new Vec3(box.minX, box.maxY, box.minZ + halfZ));
		out.add(new Vec3(box.maxX, box.maxY, box.minZ + halfZ));
		out.add(new Vec3(box.minX + halfX, box.maxY, box.maxZ));
	}
}
