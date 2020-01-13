package com.parzivail.pswg;

import com.parzivail.pswg.client.ModelLoader;
import com.parzivail.pswg.client.model.SimpleModels;
import com.parzivail.pswg.util.Lumberjack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.registry.Registry;

public class GalaxiesClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		Lumberjack.debug("onInitializeClient");

		SimpleModels.register(Registry.BLOCK.getId(GalaxiesMain.SAND_TATOOINE), ModelLoader.loadPM3D(Resources.identifier("models/block/crate_octagon.pm3d")));

		ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> SimpleModels.INSTANCE);
	}
}
