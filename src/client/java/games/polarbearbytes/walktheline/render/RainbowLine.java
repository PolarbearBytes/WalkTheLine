package games.polarbearbytes.walktheline.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.awt.*;
import java.time.LocalTime;

/**
 * RainbowLine creates a line with a rotating color
 *
 * @author PolarbearBytes
 * @version 1.0
 * @since 2025-07-24
 */
public class RainbowLine implements ILine {
    private Vec3d currentPosition;
    private final int size;
    private final Vector3f[] vertexes;
    private final int colorRotationTiming;
    private int lineColor;

    /**
     * Constructor
     *
     * @param size int size of the line (will be twice this as size is applied to both negative and positive axis)
     * @param colorRotationTiming number of seconds it takes to do a full color rotation
     */
    public RainbowLine(int size, int colorRotationTiming){
        this.size = size;
        this.vertexes = new Vector3f[8];
        this.colorRotationTiming = colorRotationTiming;
    }

    public void tick(){
        float delta = (float) LocalTime.now().toNanoOfDay() / 1000000000 % colorRotationTiming;
        this.updateColor(delta / colorRotationTiming);
    }

    /**
     * Method for updating the vertices of the line.
     *
     * @param position The center position of the line, {@link #size} is applied to the positive and negative axis
     * @param axis The axis to which the line will stretch
     */
    public void updateVertexes(Vec3d position, Axis axis,BufferBuilder buffer){
        /*
         Coordinate systems between the game world and vertex space have a swapped Z and Y axis
         So when we apply size and other metrics we have to apply them to the opposite axis
         */

        if(position.equals(this.currentPosition)) return;

        this.currentPosition = position;

        //clockwise
        vertexes[0] = new Vector3f( (float) (position.x - 0.50), (float) (position.y - 0.5)-size, (float) position.z);
        vertexes[1] = new Vector3f( (float) (position.x - 0.4), (float) (position.y - 0.5)-size, (float) position.z);
        vertexes[2] = new Vector3f( (float) (position.x - 0.4), (float) (position.y + 0.5)+size, (float) position.z);
        vertexes[3] = new Vector3f( (float) (position.x - 0.50), (float) (position.y + 0.5)+size, (float) position.z);

        //counterclockwise
        vertexes[4] = new Vector3f( (float) (position.x + 0.50), (float) (position.y + 0.5)+size, (float) position.z);
        vertexes[5] = new Vector3f( (float) (position.x + 0.4), (float) (position.y + 0.5)+size, (float) position.z);
        vertexes[6] = new Vector3f( (float) (position.x + 0.4), (float) (position.y - 0.5)-size, (float) position.z);
        vertexes[7] = new Vector3f( (float) (position.x + 0.50), (float) (position.y - 0.5)-size, (float) position.z);
    }

    /**
     * Calculates the next starting and ending colors for the next render cycle
     */
    public void updateColor(float hue){
        lineColor = Color.HSBtoRGB(hue,1.0f,1.0f);
        lineColor = (lineColor & 0x00FFFFFF) | (255 << 24);
    }

    /**
     * Add the vertices of the line to the builder
     *
     * @param builder BufferBuilder used for rendering the vertices
     */
    public void addToBuffer(BufferBuilder builder){
        int a = lineColor >> 24 & 0xFF;
        int r = lineColor >> 16 & 0xFF;
        int g = lineColor >> 8  & 0xFF;
        int b = lineColor & 0xFF;

        builder.vertex(vertexes[0].x,vertexes[0].y,vertexes[0].z).color(r, g, b, a).light(0xF000F0).next();
        builder.vertex(vertexes[1].x,vertexes[1].y,vertexes[1].z).color(r, g, b, a).next();
        builder.vertex(vertexes[2].x,vertexes[2].y,vertexes[2].z).color(r, g, b, a).next();
        builder.vertex(vertexes[3].x,vertexes[3].y,vertexes[3].z).color(r, g, b, a).next();

        builder.vertex(vertexes[4].x,vertexes[4].y,vertexes[4].z).color(r, g, b, a).next();
        builder.vertex(vertexes[5].x,vertexes[5].y,vertexes[5].z).color(r, g, b, a).next();
        builder.vertex(vertexes[6].x,vertexes[6].y,vertexes[6].z).color(r, g, b, a).next();
        builder.vertex(vertexes[7].x,vertexes[7].y,vertexes[7].z).color(r, g, b, a).next();
    }
}
