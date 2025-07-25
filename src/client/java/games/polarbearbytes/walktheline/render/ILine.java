package games.polarbearbytes.walktheline.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

/**
 * Interface to make it so different types of line classes can be made and changed to
 * @author PolarbearBytes
 * @version 1.0
 * @since 2025-07-24
 */
public interface ILine {
    void addToBuffer(BufferBuilder builder);
    void updateVertexes(Vec3d newPosition, Axis axis);
}
