package games.polarbearbytes.walktheline.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Interface to make it so different types of line classes can be made and changed to
 * @author PolarbearBytes
 * @version 1.0
 * @since 2025-07-24
 */
public interface ILine {
    boolean shouldUpdate(Entity entity, MinecraftClient client);
    void render(Vec3d cameraPos, Entity entity, MinecraftClient client);
    void update(Vec3d cameraPos, Entity entity, MinecraftClient client);
    void draw(Vec3d cameraPos);

    Vec3d getLastCameraPosition();
    void setLastCameraPosition(Vec3d lastPosition);

    void reset();
}
