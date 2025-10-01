package games.polarbearbytes.walktheline.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import games.polarbearbytes.walktheline.WalkTheLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Class to control the rendering pipeline
 * Stolen parts from the MaLiLib library
 * @see <a href="https://github.com/sakura-ryoko/malilib/blob/1.21.7-0.25.2/src/main/java/fi/dy/masa/malilib/render/RenderContext.java">github.com/sakura-ryoko/malilib</a>
 *
 * @author Sakura-Ryoko MaLiLib 1.21.5+
 * @author PolarbearBytes - Stripped down to only need for drawing lines
 */
public class RenderContext implements AutoCloseable {
    public int indexCount;
    public GpuBuffer vertexBuffer;
    public GpuBuffer indexBuffer;
    public VertexFormat.IndexType indexType;
    public final String name = "WalkTheLine.RenderContext";
    public RenderSystem.ShapeIndexBuffer shapeIndex;
    public int textureId;
    private BufferAllocator alloc;
    private BufferBuilder builder;
    private final RenderPipeline pipeline;

    public RenderContext(RenderPipeline pipeline) {
        this.pipeline = pipeline;
        this.alloc = new BufferAllocator(pipeline.getVertexFormat().getVertexSize() * 4);
        this.builder = new BufferBuilder(this.alloc, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        this.shapeIndex = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        this.indexType = this.shapeIndex.getIndexType();
        this.vertexBuffer = null;
        this.indexBuffer = null;
        this.indexCount = -1;
        this.textureId = -1;
    }

    public BufferBuilder getBuilder(){
        return this.builder;
    }

    public void draw(BuiltBuffer meshData){
        try
        {
            if (RenderSystem.isOnRenderThread())
            {
                if (meshData == null)
                {
                    this.indexCount = 0;
                }
                else
                {
                    if (this.indexCount < 1)
                    {
                        this.writeVertexToBuffer(meshData);
                    }
                }

                if (this.indexCount > 0)
                {
                    this.renderPass();
                }
            }
            if(meshData != null) {
                meshData.close();
            }


            this.reset();
        }
        catch (Exception err)
        {
            WalkTheLine.LOGGER.error("renderBlockTargetingOverlay():1: Draw Exception; {}", err.getMessage());
        }
    }

    public void writeVertexToBuffer(BuiltBuffer meshData){
        if (RenderSystem.isOnRenderThread() && meshData != null)
        {
            int expectedSize = meshData.getBuffer().remaining();
            if (this.vertexBuffer != null)
            {
                this.vertexBuffer.close();
            }

            if (this.indexBuffer != null)
            {
                this.indexBuffer.close();
                this.indexBuffer = null;
            }

            GpuDevice device = RenderSystem.tryGetDevice();

            if (device == null)
            {
                WalkTheLine.LOGGER.warn("RenderContext#upload: GpuDevice is null for renderer '{}'", this.name);
                return;
            }

            if (this.vertexBuffer == null)
            {
                this.vertexBuffer = device.createBuffer(() -> this.name+" VertexBuffer", 40, expectedSize);
            }
            else if (this.vertexBuffer.size() < expectedSize)
            {
                this.vertexBuffer.close();
                this.vertexBuffer = device.createBuffer(() -> this.name+" VertexBuffer", 40, expectedSize);
            }

            CommandEncoder encoder = device.createCommandEncoder();

            if (!this.vertexBuffer.isClosed())
            {
                encoder.writeToBuffer(this.vertexBuffer.slice(), meshData.getBuffer());
            }
            else
            {
                throw new RuntimeException("Vertex Buffer is closed!");
            }

            if (this.indexBuffer != null)
            {
                this.indexBuffer.close();
                this.indexBuffer = null;
            }

            this.indexCount = meshData.getDrawParameters().indexCount();
            this.indexType = meshData.getDrawParameters().indexType();
        }
    }

    public void renderPass(){
        if (RenderSystem.isOnRenderThread())
        {
            Vector4f colorMod = new Vector4f(1f, 1f, 1f, 1f);
            Vector3f modelOffset = new Vector3f();
            Matrix4f texMatrix = new Matrix4f();
            float line = 0.0f;

            GpuDevice device = RenderSystem.getDevice();

            if (device == null)
            {
                WalkTheLine.LOGGER.warn("RenderContext#drawInternal: GpuDevice is null for renderer '{}'", this.name);
                return;
            }

            Framebuffer mainFb = MinecraftClient.getInstance().getFramebuffer();
            GpuTextureView texture1;
            GpuTextureView texture2;

            texture1 = mainFb.getColorAttachmentView();
            texture2 = mainFb.useDepthAttachment ? mainFb.getDepthAttachmentView() : null;

            GpuBuffer indexBuffer = this.shapeIndex.getIndexBuffer(this.indexCount);

            GpuBufferSlice gpuSlice = RenderSystem.getDynamicUniforms()
                    .write(
                            RenderSystem.getModelViewMatrix(),
                            colorMod,
                            modelOffset,
                            texMatrix,
                            line);

            // Attach Frame buffers
            try(RenderPass pass = device.createCommandEncoder()
                    .createRenderPass(()->this.name, texture1, OptionalInt.empty(), texture2, OptionalDouble.empty()))
            {
                pass.setPipeline(this.pipeline);

                ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                if (scissorState.isEnabled())
                {
                    pass.enableScissor(scissorState.getX(), scissorState.getY(), scissorState.getWidth(), scissorState.getHeight());
                }

                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", gpuSlice);

                if (this.indexBuffer == null)
                {
                    pass.setIndexBuffer(indexBuffer, this.shapeIndex.getIndexType());
                }
                else
                {
                    pass.setIndexBuffer(this.indexBuffer, this.indexType);
                }

                pass.setVertexBuffer(0, this.vertexBuffer);
                pass.drawIndexed(0, 0, this.indexCount, 1);
            } catch (Exception ignore){}
        }
    }

    public void reset(){
        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }

        if (this.indexBuffer != null)
        {
            this.indexBuffer.close();
            this.indexBuffer = null;
        }

        if (this.builder != null)
        {
            try
            {
                BuiltBuffer meshData = this.builder.endNullable();
                if (meshData != null)
                {
                    meshData.close();
                }
            }
            catch (Exception ignored) { }
            this.builder = null;
        }

        if (this.alloc != null)
        {
            this.alloc.close();
            this.alloc = null;
        }

        this.builder = null;
        this.shapeIndex = null;
        this.indexType = null;
        this.vertexBuffer = null;
        this.indexBuffer = null;
        this.indexCount = -1;
        this.textureId = -1;
    }

    @Override
    public void close() {
        this.reset();
    }
}
