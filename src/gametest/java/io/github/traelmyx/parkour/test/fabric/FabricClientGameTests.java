package io.github.traelmyx.parkour.test.fabric;

//? if fabric && >=1.21.4 {
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if >=26 {
import net.minecraft.world.entity.EntityTypes;
//?} else
/*import net.minecraft.world.entity.EntityType;*/

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public class FabricClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		if (!FabricLoader.getInstance().isModLoaded("mobs_attempt_parkour")) {
			throw new AssertionError("mod is not loaded");
		}
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			//? if >=26 {
			singleplayer.getConnection().waitForChunksRender();
			//?} else
			/*singleplayer.getClientWorld().waitForChunksRender();*/
			context.takeScreenshot("parkour-singleplayer");
			filmJump(context, singleplayer, "creeper");
			filmJump(context, singleplayer, "zombie");
		}
	}

	private static void filmJump(ClientGameTestContext context, TestSingleplayerContext sp, String take) {
		BlockPos p = sp.getServer().computeOnServer(s -> s.getPlayerList().getPlayers().get(0).blockPosition());
		int y = p.getY() + 7, x = p.getX() + (take.equals("zombie") ? 40 : 0), z = p.getZ();
		sp.getServer().runCommand("gamemode spectator @a");
		sp.getServer().runCommand("time set 6000");
		sp.getServer().runCommand("weather clear");
		sp.getServer().runCommand(String.format("fill %d %d %d %d %d %d dirt", x + 3, y - 6, z, x + 5, y - 1, z + 1));
		sp.getServer().runCommand(String.format("fill %d %d %d %d %d %d grass_block", x + 3, y, z, x + 5, y, z + 1));
		sp.getServer().runCommand(String.format("fill %d %d %d %d %d %d dirt", x + 7, y - 6, z, x + 9, y - 1, z + 1));
		sp.getServer().runCommand(String.format("fill %d %d %d %d %d %d grass_block", x + 7, y, z, x + 9, y, z + 1));
		sp.getServer().runCommand(String.format("tp @a %.1f %.1f %.1f 0 16", x + 6.5, y + 2.0, z - 3.0));
		context.getInput().pressKey(o -> o.keyToggleGui);
		context.waitTicks(20);
		//? if >=26 {
		sp.getConnection().waitForChunksRender();
		//?} else
		/*sp.getClientWorld().waitForChunksRender();*/

		AtomicReference<Mob> zombie = new AtomicReference<>();
		sp.getServer().runOnServer(s -> {
			ServerLevel level = s.overworld();
			//? if >=26 {
			Mob mob = (take.equals("zombie") ? EntityTypes.ZOMBIE : EntityTypes.CREEPER).create(level, EntitySpawnReason.COMMAND);
			//?} else
			/*Mob mob = (take.equals("zombie") ? EntityType.ZOMBIE : EntityType.CREEPER).create(level, EntitySpawnReason.COMMAND);*/
			if (take.equals("zombie")) mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
			mob.snapTo(x + 3.5, y + 1, z + 1.0, -90, 0);
			level.addFreshEntity(mob);
			zombie.set(mob);
		});

		boolean crossed = false;
		for (int tick = 0; tick < 200 && !crossed; tick++) {
			if (tick % 10 == 0) {
				sp.getServer().runOnServer(s -> {
					Mob m = zombie.get();
					if (!m.getNavigation().isInProgress()) m.getNavigation().moveTo(x + 8.5, y + 1, z + 1.0, 1.0);
				});
			}
			context.waitTick();
			double mx = sp.getServer().computeOnServer(s -> zombie.get().getX());
			boolean airborne = sp.getServer().computeOnServer(s -> !zombie.get().onGround());
			if (mx > x + 4.5 && mx < x + 8.5) {
				context.takeScreenshot(String.format("showcase-%s-%03d", take, tick));
			}
			crossed = sp.getServer().computeOnServer(s -> zombie.get().onGround() && zombie.get().getX() > x + 7.3);
		}
		context.getInput().pressKey(o -> o.keyToggleGui);
		sp.getServer().runOnServer(s -> zombie.get().discard());
		if (!crossed) throw new AssertionError("showcase " + take + " did not cross the gap");
	}
}
//?}
