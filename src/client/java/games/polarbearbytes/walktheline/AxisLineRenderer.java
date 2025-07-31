package games.polarbearbytes.walktheline;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import games.polarbearbytes.walktheline.config.ConfigManager;
import games.polarbearbytes.walktheline.config.WalkTheLineConfig;
import games.polarbearbytes.walktheline.render.ILine;
import games.polarbearbytes.walktheline.render.RainbowLine;
import games.polarbearbytes.walktheline.render.RenderContext;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4fStack;

/**
 * Renderer responsible for rendering a line along the locked axis
 *
 * @author PolarbearBytes
 * @version 1.0
 * @since 2025-07-24
 */
public class AxisLineRenderer {
    private static final BlendFunction BLENDER = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

    /**
     * Custom pipeline so that line gets drawn and culled correctly
     */
    public static RenderPipeline AXIS_LINE_RENDERPIPELINE = RenderPipeline.builder()
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BLENDER)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withLocation(Identifier.of(WalkTheLine.MOD_ID, "pipeline"))
            .withCull(true)
            .withDepthWrite(true)
            .withColorWrite(true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .build();

    private static AxisLineRenderer INSTANCE = null;
    private final MinecraftClient mc;

    /**
     * Current block position of the player, used to calculate where to draw the line
     */
    private Vec3d blockPos;

    /**
     * The direction that the locked axis runs, used to rotate the global matrix to the correct facing direction
     */
    private Direction axisDirection;

    /**
     * Set to an instance of the line that is to be rendered
     */
    private final ILine line;

    /**
     * Register the event(s) needed to call {@link #render()}
     */
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> AxisLineRenderer.getInstance().render());
    }

    private AxisLineRenderer(){
        this.mc = MinecraftClient.getInstance();
        WalkTheLineConfig config = ConfigManager.getConfig();
        this.line = new RainbowLine(config.lineLength,config.lineColorRotateTiming);
    }

    public static AxisLineRenderer getInstance(){
        if(INSTANCE == null){
            INSTANCE = new AxisLineRenderer();
        }
        return INSTANCE;
    }

    public static float matrix4fRotateFix(float ang) {return (ang * 0.017453292F);}

    /**
     * Updates the various fields and calls the line updater
     */
    public void update() {
        if (mc.player == null) return;

        LockedAxisData lockedAxisData = WalkTheLineClient.getLockedAxis(mc.player.getWorld().getRegistryKey());
        if (lockedAxisData == null) return;

        //Need the opposite axis direction in order do the matrix rotation correctly
        axisDirection = lockedAxisData.axis() == Axis.X ? Axis.Z.getDirections()[1] : Axis.X.getDirections()[0];

        BlockPos pos = mc.player.getBlockPos();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        //Clamp the coordinates to align with the locked axis
        switch(lockedAxisData.axis()){
            case X-> pos = new BlockPos((int) Math.round(lockedAxisData.coordinate())-1,pos.getY(),pos.getZ());
            case Z-> pos = new BlockPos(pos.getX(),pos.getY(),(int) Math.round(lockedAxisData.coordinate())-1);
        }

        //Adjust the coordinates to be the center of the block coordinates
        double x = (pos.getX() + 0.5d - cameraPos.x);
        double y = (pos.getY() + 0.5d - cameraPos.y - 1);
        double z = (pos.getZ() + 0.5d - cameraPos.z);

        blockPos = new Vec3d(x,y,z);
        Axis k = lockedAxisData.axis();

        //Flip the axis if direction was flipped to the North (Z) direction (prevents improper culling?);
        if(axisDirection == Direction.NORTH) {
            k = lockedAxisData.axis() == Axis.X ? Axis.Z : Axis.X;
        }
        this.line.tick();
        this.line.updateVertexes(blockPos,k);
    }

    /**
     * Renders the line
     */
    public void render() {
        if(!WalkTheLineClient.modEnabled) return;

        this.update();

        Matrix4fStack matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        translateToFace(matrix, blockPos , Direction.UP, axisDirection);

        RenderContext ctx = new RenderContext(AXIS_LINE_RENDERPIPELINE, BufferUsage.STATIC_WRITE);
        BufferBuilder builder = ctx.getBuilder();

        this.line.addToBuffer(builder);

        BuiltBuffer meshData = builder.endNullable();
        if (meshData != null) {
            ctx.draw(meshData);
            meshData.close();
        }
        ctx.reset();
        matrix.popMatrix();
    }

    /**
     * Translates / Rotates the matrix to the correct block face
     *
     * @param matrixStack the Model View Stack
     * @param pos the position of the block that we will be drawing on
     * @param side the side of the block we want to draw on
     * @param facing the direction the drawing should happen
     */
    public void translateToFace(Matrix4fStack matrixStack, Vec3d pos, Direction side, Direction facing){
        matrixStack.translate((float) pos.x, (float) pos.y, (float) pos.z);

        switch (side)
        {
            case DOWN:
                matrixStack.rotateY(matrix4fRotateFix(180f - facing.getPositiveHorizontalDegrees()));
                matrixStack.rotateX(matrix4fRotateFix(90f));
                break;
            case UP:
                matrixStack.rotateY(matrix4fRotateFix(180f - facing.getPositiveHorizontalDegrees()));
                matrixStack.rotateX(matrix4fRotateFix(-90f));
                break;
            case NORTH:
                matrixStack.rotateY(matrix4fRotateFix(180f));
                break;
            case SOUTH:
                break;
            case WEST:
                matrixStack.rotateY(matrix4fRotateFix(-90f));
                break;
            case EAST:
                matrixStack.rotateY(matrix4fRotateFix(90f));
                break;
        }

        matrixStack.translate((float) (-pos.x), (float) (-pos.y), (float) ((-pos.z) + 0.510));
    }
}