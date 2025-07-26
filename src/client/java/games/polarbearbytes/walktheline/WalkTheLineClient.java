package games.polarbearbytes.walktheline;

import games.polarbearbytes.walktheline.network.SyncPacket;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WalkTheLineClient implements ClientModInitializer {
	public static final Map<RegistryKey<World>, LockedAxisData> CLIENT_LOCKED_AXES = new HashMap<>();
	//public static KeyBinding openParticleGuiKey;
	public static boolean modEnabled = false;
	@Override
	public void onInitializeClient() {
		AxisLineRenderer.register();
		//ParticleLineRenderer.register();

		/*
			Register the packet that we use to tell the client the locked Axis, Coordinate per World (dimension)
			And wither or not the mod is enabled
		 */
		ClientPlayNetworking.registerGlobalReceiver(SyncPacket.PAYLOAD_ID, (packet, context) -> context.client().execute(() -> {
			CLIENT_LOCKED_AXES.put(packet.worldKey(), packet.data());
			modEnabled = packet.enabled();
		}));

		//TODO: decide wither or not to implement a particle line and / or particle picker screen
		/*
		 * Code that was going to display a screen to select which particle to use
		 * May revisit or delete later
		 *
		openParticleGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.walktheline.open_particle_gui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				"category.walktheline"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openParticleGuiKey.wasPressed()) {
				client.setScreen(new ParticleSelectionScreen());
			}
		});
		*/
	}

	/**
	 * Method for getting the locked axis data per world
	 *
	 * @param worldKey Key of the world (dimension)
	 * @return LockedAxisData containing the axis we are locked to and on which coordinate
	 */
	public static LockedAxisData getLockedAxis(RegistryKey<World> worldKey) {
		ClientWorld world = MinecraftClient.getInstance().world;
		if (world != null) {
            return CLIENT_LOCKED_AXES.get(worldKey);
		}
		return null;
	}
}
