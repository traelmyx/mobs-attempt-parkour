package io.github.traelmyx.parkour.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public final class BlockTypes {
	private BlockTypes() {
	}

	public static BlockType of(BlockGetter level, BlockPos pos) {
		return of(level, pos, 2);
	}

	private static BlockType of(BlockGetter level, BlockPos pos, int callsLeft) {
		BlockState state = level.getBlockState(pos);
		FluidState fluid = level.getFluidState(pos);

		if (state.is(Blocks.SWEET_BERRY_BUSH) || state.is(BlockTags.FIRE) || CampfireBlock.isLitCampfire(state)
			|| fluid.is(FluidTags.WATER)) {
			return BlockType.PASSABLE_OBSTACLE;
		}
		if (fluid.is(FluidTags.LAVA) || state.is(Blocks.CACTUS) || state.is(Blocks.HONEY_BLOCK) || state.is(Blocks.MAGMA_BLOCK)) {
			return BlockType.SOLID_OBSTACLE;
		}
		if (state.getBlock() instanceof LeavesBlock || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS)
			|| (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN))
			|| state.getBlock() instanceof DoorBlock
			|| !state.isPathfindable(PathComputationType.LAND)) {
			return BlockType.BLOCKED;
		}

		BlockType below = callsLeft > 0 ? of(level, pos.below(), callsLeft - 1) : BlockType.OPEN;
		return switch (below) {
			case BLOCKED -> BlockType.WALKABLE;
			case OPEN, PASSABLE_OBSTACLE, SOLID_OBSTACLE -> BlockType.PASSABLE_OBSTACLE;
			case WALKABLE -> BlockType.OPEN;
		};
	}
}
