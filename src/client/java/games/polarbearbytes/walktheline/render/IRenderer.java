package games.polarbearbytes.walktheline.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;

public interface IRenderer {
    void render(Framebuffer framebuffer, Matrix4f positionMatrix, Matrix4f projectionMatrix, MinecraftClient client, FrameGraphBuilder frameGraphBuilder, DefaultFramebufferSet fbSet, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler);
}
