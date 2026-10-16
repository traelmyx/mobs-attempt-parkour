package io.github.traelmyx.parkour.mixin;

import io.github.traelmyx.parkour.Parkour;
import io.github.traelmyx.parkour.ai.JumpToTargetGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Shadow
	@Final
	protected GoalSelector goalSelector;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void parkour$addJumpGoal(EntityType<? extends Mob> type, Level level, CallbackInfo ci) {
		if (!(level instanceof ServerLevel)) return;
		OptionalInt priority = Parkour.config().priorityFor(String.valueOf(EntityType.getKey(type)));
		if (priority.isPresent()) goalSelector.addGoal(priority.getAsInt(), new JumpToTargetGoal((Mob) (Object) this));
	}
}
