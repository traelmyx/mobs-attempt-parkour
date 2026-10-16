package io.github.traelmyx.parkour.fabric;

//? if fabric {
import io.github.traelmyx.parkour.Parkour;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ParkourFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Parkour.init(FabricLoader.getInstance().getConfigDir());
	}
}
//?}
