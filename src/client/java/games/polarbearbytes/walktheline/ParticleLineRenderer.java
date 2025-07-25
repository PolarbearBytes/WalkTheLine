package games.polarbearbytes.walktheline;

import games.polarbearbytes.walktheline.config.ConfigManager;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;

public class ParticleLineRenderer {
    private static final int LINE_LENGTH = 16;
    private static final double PARTICLE_SPACING = 0.3f;
    private static final ParticleEffect particleEffect = getParticleFromString(ConfigManager.getConfig().particleType);
    private static final double tolerance = ConfigManager.getConfig().coordinateTolerance;
    public static void register() {
        WorldRenderEvents.END.register(context -> {
            if(!WalkTheLineClient.modEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;

            if (world == null || client.player == null) return;

            LockedAxisData data = WalkTheLineClient.getLockedAxis(world.getRegistryKey());
            if(data == null) return;
            double axisCoord = data.coordinate();

            Axis axis = data.axis();

            int centerX = (int) client.player.getX();
            int centerY = (int) client.player.getY();
            int centerZ = (int) client.player.getZ();

            for (double offset = -LINE_LENGTH; offset <= LINE_LENGTH; offset += PARTICLE_SPACING) {
                double x = axis == Axis.X ? axisCoord : centerX + offset;
                double z = axis == Axis.Z ? axisCoord : centerZ + offset;
                double y = centerY + 0.2; // slight offset above ground

                world.addParticleClient(particleEffect, x + tolerance, y, z + tolerance, 0, 0, 0);
                world.addParticleClient(particleEffect, x - tolerance, y, z - tolerance, 0, 0, 0);
            }
        });
    }
    public static ParticleEffect getParticleFromString(String id) {
        Identifier particleId = Identifier.of(id);
        ParticleEffect effect = (ParticleEffect) Registries.PARTICLE_TYPE.get(particleId);

        return (effect != null) ? effect : ParticleTypes.END_ROD; // Fallback
    }
}