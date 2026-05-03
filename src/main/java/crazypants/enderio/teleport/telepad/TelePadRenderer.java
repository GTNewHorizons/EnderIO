package crazypants.enderio.teleport.telepad;

import java.util.Collection;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.TechneModelRenderer;
import com.enderio.core.client.render.TechneUtil;
import com.enderio.core.common.util.BlockCoord;
import com.google.common.collect.Lists;

import crazypants.enderio.EnderIO;
import org.lwjgl.opengl.GL11;

public class TelePadRenderer extends TechneModelRenderer implements IItemRenderer {

    private final Collection<GroupObject> strippedModel = Lists.newArrayList();

    public TelePadRenderer() {
        super(TechneUtil.getModel(EnderIO.DOMAIN, "models/telePad"), BlockTelePad.renderId);
        for (String s : this.model.keySet()) {
            if (!s.equals("glass") && !s.contains("blade")) {
                strippedModel.add(this.model.get(s));
            }
        }
    }

    protected Collection<GroupObject> getModel() {
        return strippedModel;
    }

    @Override
    protected Collection<GroupObject> getModel(Block block, int metadata) {
        return getModel();
    }

    @Override
    protected Collection<GroupObject> getModel(IBlockAccess world, int x, int y, int z) {
        return getModel();
    }

    Map<String, GroupObject> getFullModel() {
        return model;
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        TileTelePad te = (TileTelePad) world.getTileEntity(x, y, z);
        boolean ret = true;
        if (te.inNetwork()) {
            if (renderer.hasOverrideBlockTexture()) {
                IIcon icon = renderer.overrideBlockTexture;
                renderer.renderFaceYNeg(block, x, y, z, icon);
                renderer.renderFaceYPos(block, x, y, z, icon);
                BlockCoord bc = te.getLocation();
                if (!isTelepad(world, bc, ForgeDirection.EAST)) {
                    renderer.renderFaceXPos(block, x, y, z, icon);
                }
                if (!isTelepad(world, bc, ForgeDirection.WEST)) {
                    renderer.renderFaceXNeg(block, x, y, z, icon);
                }
                if (!isTelepad(world, bc, ForgeDirection.SOUTH)) {
                    renderer.renderFaceZPos(block, x, y, z, icon);
                }
                if (!isTelepad(world, bc, ForgeDirection.NORTH)) {
                    renderer.renderFaceZNeg(block, x, y, z, icon);
                }
            } else {
                if (te.isMaster()) {
                    super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
                } else {
                    ret = false;
                }
            }
        } else {
            renderer.renderStandardBlock(block, x, y, z);
        }
        return ret;
    }

    private boolean isTelepad(IBlockAccess world, BlockCoord pos, ForgeDirection dir) {
        return pos.getLocation(dir).getTileEntity(world) instanceof TileTelePad;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        CubeRenderer.get().render(block, metadata);
        tessellator.draw();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();

        try {
            renderInventoryBlock(
                Block.getBlockFromItem(item.getItem()),
                item.getItemDamage(),
                0,
                (RenderBlocks) data[0]);
        } finally {
            GL11.glPopMatrix();
        }
    }
}
