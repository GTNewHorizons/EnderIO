package crazypants.enderio.conduit.render;

import net.minecraft.client.renderer.RenderBlocks;

import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;

public interface ConduitRenderer {

    void renderEntity(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle te, IConduit con, double x, double y,
            double z, float partialTick, float worldLight, RenderBlocks rb);

}
