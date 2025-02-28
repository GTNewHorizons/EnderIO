package crazypants.enderio.conduit.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.IconUtil;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.IBlockAccessWrapper;
import com.google.common.collect.Lists;
import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.BlockConduitBundle;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.conduit.ConduitUtil;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.IConduitBundle.FacadeRenderState;
import crazypants.enderio.conduit.RaytraceResult;
import crazypants.enderio.conduit.TileConduitBundle;
import crazypants.enderio.conduit.facade.BlockConduitFacade;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.conduit.geom.ConduitConnectorType;
import crazypants.enderio.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.config.Config;
import crazypants.util.RenderPassHelper;

@SideOnly(Side.CLIENT)
@ThreadSafeISBRH(perThread = false)
public class ConduitBundleRenderer implements ISimpleBlockRenderingHandler {

    public ConduitBundleRenderer(float conduitScale) {}

    public ConduitBundleRenderer() {}

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks rb) {

        final Tessellator tessellator = Tessellator.instance;
        int pass = RenderPassHelper.getBlockRenderPass();
        if (pass == 1) {
            // If the MC renderer is told that an alpha pass is required ( see
            // BlockConduitBundle.getRenderBlockPass() ) put nothing is actually added
            // to the tessellator in this pass then the renderer will crash. We can't
            // selectively enable the alpha pass based on state so the only work
            // around is to ensure we always render something in this pass. Throwing
            // in a polygon with a 0 area does the job
            // See: https://github.com/MinecraftForge/MinecraftForge/issues/981
            tessellator.addVertexWithUV(x, y, z, 0, 0);
            tessellator.addVertexWithUV(x, y, z, 0, 0);
            tessellator.addVertexWithUV(x, y, z, 0, 0);
            tessellator.addVertexWithUV(x, y, z, 0, 0);
        }

        IConduitBundle bundle = (IConduitBundle) world.getTileEntity(x, y, z);
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

        boolean renderedFacade = renderFacade(x, y, z, pass, rb, bundle, player);
        boolean renderConduit = !renderedFacade || ConduitUtil.isFacadeHidden(bundle, player);

        if (bundle != null && renderConduit && (pass == 0 || rb.overrideBlockTexture != null)) {
            BlockCoord loc = bundle.getLocation();
            float brightness;
            if (!Config.updateLightingWhenHidingFacades && bundle.hasFacade()
                    && ConduitUtil.isFacadeHidden(bundle, player)) {
                brightness = 15 << 20 | 15 << 4;
            } else {
                brightness = bundle.getEntity().getWorldObj().getLightBrightnessForSkyBlocks(loc.x, loc.y, loc.z, 0);
            }
            renderConduits(bundle, x, y, z, 0, brightness, rb);
            return true;
        }

        return renderedFacade || (bundle != null && bundle.hasFacade() && !bundle.getFacadeId().isOpaqueCube());
    }

    private static final ThreadLocal<BlockConduitFacade> facade = ThreadLocal.withInitial(() -> {
        try {
            return EnderIO.blockConduitFacade.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    });

    private boolean renderFacade(int x, int y, int z, int pass, RenderBlocks rb, IConduitBundle bundle,
            EntityClientPlayerMP player) {
        boolean res = false;
        if (bundle == null) {
            return false;
        }
        final Tessellator tessellator = Tessellator.instance;
        if (bundle.hasFacade()) {
            res = true;
            Block facadeId = bundle.getFacadeId();
            if (ConduitUtil.isFacadeHidden(bundle, player)) {
                tessellator.setColorOpaque_F(1, 1, 1);
                bundle.setFacadeId(null, false);
                bundle.setFacadeRenderAs(FacadeRenderState.WIRE_FRAME);

                BlockConduitFacade facb = facade.get();
                facb.setBlockOverride(bundle);
                facb.setBlockBounds(0, 0, 0, 1, 1, 1);
                if (!rb.hasOverrideBlockTexture()) {
                    rb.setRenderBoundsFromBlock(facb);
                    rb.renderStandardBlock(facb, x, y, z);
                }
                facb.setBlockOverride(null);
                bundle.setFacadeId(facadeId, false);
            } else if (facadeId != null) {
                bundle.setFacadeRenderAs(FacadeRenderState.FULL);
                boolean isFacadeOpaque = facadeId.isOpaqueCube();

                if ((isFacadeOpaque && pass == 0) || (rb.hasOverrideBlockTexture() || (!isFacadeOpaque && pass == 1))) {
                    IBlockAccess origBa = rb.blockAccess;
                    rb.blockAccess = new FacadeAccessWrapper(origBa);
                    try {
                        rb.renderBlockByRenderType(facadeId, x, y, z);
                    } catch (Exception e) {
                        // just in case the paint source wont render safely in this way
                        rb.setOverrideBlockTexture(IconUtil.errorTexture);
                        rb.renderStandardBlock(Blocks.stone, x, y, z);
                        rb.setOverrideBlockTexture(null);
                    }

                    rb.blockAccess = origBa;
                }

                res = isFacadeOpaque;
            }

        } else {
            bundle.setFacadeRenderAs(FacadeRenderState.NONE);
        }
        return res;
    }

    public void renderConduits(IConduitBundle bundle, double x, double y, double z, float partialTick, float brightness,
            RenderBlocks rb) {

        if (bundle == null) return;

        final Tessellator tessellator = Tessellator.instance;
        final CubeRenderer cr = CubeRenderer.get();
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.addTranslation((float) x, (float) y, (float) z);

        // Conduits
        Set<ForgeDirection> externals = new HashSet<>();
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

        List<BoundingBox> wireBounds = new ArrayList<>();

        for (IConduit con : bundle.getConduits()) {

            if (ConduitUtil.renderConduit(player, con)) {
                final ConduitRenderer renderer = con.getRenderer();
                renderer.renderEntity(this, bundle, con, x, y, z, partialTick, brightness, rb);
                Set<ForgeDirection> extCons = con.getExternalConnections();
                for (ForgeDirection dir : extCons) {
                    if (con.getConnectionMode(dir) != ConnectionMode.DISABLED
                            && con.getConnectionMode(dir) != ConnectionMode.NOT_SET) {
                        externals.add(dir);
                    }
                }
            } else if (con != null) {
                Collection<CollidableComponent> components = con.getCollidableComponents();
                for (CollidableComponent component : components) {
                    wireBounds.add(component.bound);
                }
            }
        }

        // Internal conectors between conduits
        List<CollidableComponent> connectors = bundle.getConnectors();
        List<CollidableComponent> rendered = Lists.newArrayList();
        for (int i = 0; i < connectors.size(); i++) {
            final CollidableComponent component = connectors.get(i);
            if (component.conduitType != null) {
                IConduit conduit = bundle.getConduit(component.conduitType);
                if (conduit != null) {
                    if (ConduitUtil.renderConduit(player, component.conduitType)) {
                        if (rb.hasOverrideBlockTexture()) {
                            List<RaytraceResult> results = EnderIO.blockConduitBundle.doRayTraceAll(
                                    bundle.getWorld(),
                                    MathHelper.floor_double(x),
                                    MathHelper.floor_double(y),
                                    MathHelper.floor_double(z),
                                    EnderIO.proxy.getClientPlayer());
                            for (RaytraceResult r : results) {
                                // the connectors can be rendered multiple times and this makes the break texture look
                                // funky
                                if (r.component.conduitType == component.conduitType
                                        && !rendered.contains(r.component)) {
                                    rendered.add(r.component);
                                    cr.render(component.bound, rb.overrideBlockTexture, true);
                                }
                            }
                        } else {
                            tessellator.setBrightness((int) (brightness));
                            cr.render(component.bound, conduit.getTextureForState(component), true);
                        }
                    } else {
                        wireBounds.add(component.bound);
                    }
                }

            } else if (ConduitUtil.getDisplayMode(player) == ConduitDisplayMode.ALL && !rb.hasOverrideBlockTexture()) {
                IIcon tex = EnderIO.blockConduitBundle.getConnectorIcon(component.data);
                cr.render(component.bound, tex);
            }
        }
        // render these after the 'normal' conduits so help with proper blending
        BlockConduitFacade facb = facade.get();
        for (int i = 0; i < wireBounds.size(); i++) {
            final BoundingBox wireBound = wireBounds.get(i);
            tessellator.setColorRGBA_F(1, 1, 1, 0.25f);
            cr.render(wireBound, facb.getIcon(0, 0));
        }

        tessellator.setColorRGBA_F(1, 1, 1, 1f);
        // External connection terminations
        if (rb.overrideBlockTexture == null) {
            for (ForgeDirection dir : externals) {
                renderExternalConnection(dir);
            }
        }
        tessellator.addTranslation(-(float) x, -(float) y, -(float) z);
    }

    private void renderExternalConnection(ForgeDirection dir) {
        IIcon tex = EnderIO.blockConduitBundle.getConnectorIcon(ConduitConnectorType.EXTERNAL);
        BoundingBox[] bbs = ConduitGeometryUtil.instance.getExternalConnectorBoundingBoxes(dir);
        final CubeRenderer cr = CubeRenderer.get();
        for (int i = 0; i < bbs.length; i++) {
            final BoundingBox bb = bbs[i];
            cr.render(bb, tex, true);
        }
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

    @Override
    public int getRenderId() {
        return BlockConduitBundle.rendererId;
    }

    public static class FacadeAccessWrapper extends IBlockAccessWrapper {

        public FacadeAccessWrapper(IBlockAccess ba) {
            super(ba);
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            Block res = super.getBlock(x, y, z);
            if (res == EnderIO.blockConduitBundle) {
                TileEntity te = getTileEntity(x, y, z);
                if (te instanceof TileConduitBundle) {
                    TileConduitBundle tcb = (TileConduitBundle) te;
                    Block fac = tcb.getFacadeId();
                    if (fac != null) {
                        res = fac;
                    }
                }
            }
            return res;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getLightBrightnessForSkyBlocks(int var1, int var2, int var3, int var4) {
            return wrapped.getLightBrightnessForSkyBlocks(var1, var2, var3, var4);
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            Block block = super.getBlock(x, y, z);
            if (block == EnderIO.blockConduitBundle) {
                TileEntity te = getTileEntity(x, y, z);
                if (te instanceof TileConduitBundle) {
                    TileConduitBundle tcb = (TileConduitBundle) te;
                    Block fac = tcb.getFacadeId();
                    if (fac != null) {
                        return tcb.getFacadeMetadata();
                    }
                }
            }
            return super.getBlockMetadata(x, y, z);
        }
    }
}
