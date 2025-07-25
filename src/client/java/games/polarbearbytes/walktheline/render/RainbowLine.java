package games.polarbearbytes.walktheline.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Objects;

/**
 * RainbowLine creates a line with a rotating color
 *
 * @author PolarbearBytes
 * @version 1.0
 * @since 2025-07-24
 */
public class RainbowLine implements ILine {
    private final int numberOfColorSegments;
    private Vec3d currentPosition;
    private final int size;
    private final Vector3f[] vertexes;
    private int currentColorSegment;
    private int startColor;
    private int endColor;

    /**
     * Constructor
     *
     * @param size int size of the line (will be twice this as size is applied to both negative and positive axis)
     * @param numberOfColorSegments number of color segments to cycle through (higher number slower rotation)
     */
    public RainbowLine(int size, int numberOfColorSegments){
        this.size = size;
        this.vertexes = new Vector3f[8];
        this.currentColorSegment = 0;
        this.numberOfColorSegments = numberOfColorSegments;
    }

    /**
     * Method for updating the vertices of the line.
     *
     * @param position The center position of the line, {@link #size} is applied to the positive and negative axis
     * @param axis The axis to which the line will stretch
     */
    public void updateVertexes(Vec3d position, Axis axis){
        /*
         Coordinate systems between the game world and vertex space have a swapped Z and Y axis
         So when we apply size and other metrics we have to apply them to the opposite axis
         */

        if(position.equals(this.currentPosition)) return;

        this.currentPosition = position;
        Vector3f start;
        Vector3f end;

        if (Objects.requireNonNull(axis) == Axis.Z) {
            start = new Vector3f((float) position.getX(), (float) position.getY() + size, (float) position.getZ());
            end = new Vector3f((float) position.getX(), (float) position.getY() - size, (float) position.getZ());
        } else {
            start = new Vector3f((float) position.getX() + size, (float) position.getY(), (float) position.getZ());
            end = new Vector3f((float) position.getX() - size, (float) position.getY(), (float) position.getZ());
        }

        //clockwise
        vertexes[0] = new Vector3f( (float) (end.x - 0.50), (float) (end.y - 0.5), end.z );
        vertexes[1] = new Vector3f( (float) (end.x - 0.47), (float) (end.y - 0.5), end.z );
        vertexes[2] = new Vector3f( (float) (start.x - 0.47), (float) (start.y + 0.5), start.z );
        vertexes[3] = new Vector3f( (float) (start.x - 0.50), (float) (start.y + 0.5), start.z );

        //counterclockwise
        vertexes[4] = new Vector3f( (float) (start.x + 0.50), (float) (start.y + 0.5), start.z );
        vertexes[5] = new Vector3f( (float) (start.x + 0.47), (float) (start.y + 0.5), start.z );
        vertexes[6] = new Vector3f( (float) (end.x + 0.47), (float) (end.y - 0.5), end.z );
        vertexes[7] = new Vector3f( (float) (end.x + 0.50), (float) (end.y - 0.5), end.z );
    }

    /**
     * Calculates the next starting and ending colors for the next render cycle
     */
    public void nextColorSegment(){
        currentColorSegment = (currentColorSegment+1) % numberOfColorSegments;

        float startColorHue = (float) currentColorSegment / numberOfColorSegments;
        float endColorHue = (float) ((currentColorSegment+1) % numberOfColorSegments) / numberOfColorSegments;

        this.startColor = Color.HSBtoRGB(startColorHue,1.0f,1.0f);
        this.endColor = Color.HSBtoRGB(endColorHue,1.0f,1.0f);
        startColor = (startColor & 0x00FFFFFF) | (255 << 24);
        endColor = (endColor & 0x00FFFFFF) | (255 << 24);
    }

    /**
     * Add the vertices of the line to the builder
     *
     * @param builder BufferBuilder used for rendering the vertices
     */
    public void addToBuffer(BufferBuilder builder){
        nextColorSegment();

        builder.vertex(vertexes[0].x,vertexes[0].y,vertexes[0].z).color(endColor);
        builder.vertex(vertexes[1].x,vertexes[1].y,vertexes[1].z).color(endColor);
        builder.vertex(vertexes[2].x,vertexes[2].y,vertexes[2].z).color(startColor);
        builder.vertex(vertexes[3].x,vertexes[3].y,vertexes[3].z).color(startColor);

        builder.vertex(vertexes[4].x,vertexes[4].y,vertexes[4].z).color(startColor);
        builder.vertex(vertexes[5].x,vertexes[5].y,vertexes[5].z).color(startColor);
        builder.vertex(vertexes[6].x,vertexes[6].y,vertexes[6].z).color(endColor);
        builder.vertex(vertexes[7].x,vertexes[7].y,vertexes[7].z).color(endColor);
    }
}
