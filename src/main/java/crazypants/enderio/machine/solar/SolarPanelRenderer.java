package crazypants.enderio.machine.solar;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.ConnectedTextureRenderer;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.CustomCubeRenderer;
import com.enderio.core.client.render.IconUtil;
import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import crazypants.enderio.EnderIO;

@ThreadSafeISBRH(perThread = true)
public class SolarPanelRenderer implements ISimpleBlockRenderingHandler {

    private final ConnectedTextureRenderer ctr;

    public SolarPanelRenderer() {
        ctr = new ConnectedTextureRenderer();
        ctr.setMatchMeta(true);
        ctr.setSidesToRender(EnumSet.of(ForgeDirection.UP));
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        RenderUtil.bindBlockTexture();
        Tessellator tes = Tessellator.instance;

        float offset = -0.5f;
        tes.addTranslation(offset, 0, offset);
        tes.startDrawingQuads();
        CubeRenderer.get().render(
                new BoundingBox(EnderIO.blockSolarPanel),
                RenderUtil.getBlockTextures(EnderIO.blockSolarPanel, metadata),
                false);
        tes.draw();

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);

        tes.startDrawingQuads();
        tes.setColorRGBA_F(1, 1, 1, 1);
        renderBorder(null, 0, 0, 0, metadata);
        tes.draw();

        tes.addTranslation(-offset, 0, -offset);

        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        CustomCubeRenderer ccr = CustomCubeRenderer.get();
        final Tessellator tessellator = Tessellator.instance;

        renderer.renderStandardBlock(block, x, y, z);
        tessellator.addTranslation(0, 0.0001f, 0);
        int meta = world.getBlockMetadata(x, y, z);
        meta = MathHelper.clamp_int(meta, 0, 1);
        ctr.setEdgeTexture(EnderIO.blockSolarPanel.getBorderIcon(0, meta));

        ccr.setOverrideTexture(IconUtil.blankTexture);
        if (!renderer.hasOverrideBlockTexture()) {
            ccr.renderBlock(world, block, x, y, z, ctr);
        } else {
            ccr.renderBlock(world, block, x, y, z);
        }
        ccr.setOverrideTexture(null);
        tessellator.addTranslation(0, -0.0001f, 0);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    private void renderBorder(IBlockAccess blockAccess, int x, int y, int z, int meta) {
        IIcon texture = EnderIO.blockSolarPanel.getBorderIcon(0, meta);
        RenderUtil.renderConnectedTextureFace(
                blockAccess,
                EnderIO.blockSolarPanel,
                x,
                y,
                z,
                ForgeDirection.UP,
                texture,
                blockAccess == null,
                false,
                false);
    }

    @Override
    public int getRenderId() {
        return BlockSolarPanel.renderId;
    }
}
