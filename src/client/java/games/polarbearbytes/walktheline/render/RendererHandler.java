package games.polarbearbytes.walktheline.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import games.polarbearbytes.walktheline.WalkTheLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class RendererHandler {
    private static final RendererHandler INSTANCE = new RendererHandler();
    private final ArrayList<IRenderer> renderers = new ArrayList<>();

    public static RendererHandler getInstance(){
        return INSTANCE;
    }

    public void register(IRenderer renderer){
        renderers.add(renderer);
    }
    public void clear(){
        renderers.clear();
    }

    public void renderLast(Matrix4f positionMatrix, Matrix4f projectionMatrix, MinecraftClient client,
                           FrameGraphBuilder frameGraphBuilder, DefaultFramebufferSet fbSet, Frustum frustum,
                           Camera camera, BufferBuilderStorage buffers, Profiler profiler){
        if(renderers.isEmpty()) return;

        FramePass pass = frameGraphBuilder.createPass(WalkTheLine.MOD_ID+"_worldrenderer_last_render");
        fbSet.mainFramebuffer = pass.transfer(fbSet.mainFramebuffer);

        Handle<Framebuffer> handleMain = fbSet.mainFramebuffer;

        pass.setRenderer(() ->{
            //Save shaderFog
            GpuBufferSlice fog = RenderSystem.getShaderFog();
            for (IRenderer renderer : renderers) {
                renderer.render(handleMain.get(), positionMatrix, projectionMatrix, client, frameGraphBuilder, fbSet, frustum, camera, buffers, profiler);
            }
            //Restore shaderFog
            RenderSystem.setShaderFog(fog);
        });
    }
}
