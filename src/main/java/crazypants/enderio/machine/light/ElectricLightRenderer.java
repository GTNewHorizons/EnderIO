package crazypants.enderio.machine.light;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

@ThreadSafeISBRH(perThread = false)
public class ElectricLightRenderer implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {

        BoundingBox bb = new BoundingBox(0, 0, 0, 1, 0.2, 1);
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        IIcon[] textures = RenderUtil.getBlockTextures(block, metadata);
        CubeRenderer.get().render(bb, textures, null, null);

        tessellator.draw();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        block.setBlockBoundsBasedOnState(world, x, y, z);
        BoundingBox bb = new BoundingBox(
                block.getBlockBoundsMinX(),
                block.getBlockBoundsMinY(),
                block.getBlockBoundsMinZ(),
                block.getBlockBoundsMaxX(),
                block.getBlockBoundsMaxY(),
                block.getBlockBoundsMaxZ());

        bb = bb.translate(x, y, z);
        RenderUtil.setTesselatorBrightness(world, x, y, z);

        IIcon[] textures = RenderUtil.getBlockTextures(world, x, y, z);
        if (renderer.hasOverrideBlockTexture()) {
            CubeRenderer.get().render(bb, renderer.overrideBlockTexture);
        } else {
            CubeRenderer.get().render(bb, textures, null, null);
        }

        return true;
    }

    @Override
    public int getRenderId() {
        return BlockElectricLight.renderId;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
