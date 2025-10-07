package games.polarbearbytes.walktheline;

import games.polarbearbytes.walktheline.config.WalkTheLineClientConfig;
import games.polarbearbytes.walktheline.network.SyncPacket;
import games.polarbearbytes.walktheline.render.LineBase;
import games.polarbearbytes.walktheline.render.LineRenderer;
import games.polarbearbytes.walktheline.render.RendererHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

public class WalkTheLineClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if(FabricLoader.getInstance().isModLoaded("iris")) {
			IrisApi.getInstance().assignPipeline(LineBase.renderPipeline, IrisProgram.BASIC);
		}
		WalkTheLineClientConfig.register();

		RendererHandler.getInstance().register(LineRenderer.getINSTANCE());

		/*
			Register the packet that we use to tell the client the locked Axis, Coordinate per World (dimension)
			And wither or not the mod is enabled
		 */
		ClientPlayNetworking.registerGlobalReceiver(SyncPacket.PAYLOAD_ID, (packet, context) -> context.client().execute(() -> {
			WalkTheLineClientConfig.setLockedAxisData(packet.data(), packet.worldKey());
			WalkTheLineClientConfig.tolerance = packet.coordTolerance();
			WalkTheLineClientConfig.modEnabled = packet.enabled();
		}));
	}
}
