package crazypants.enderio.conduit.redstone;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.common.vecmath.Vector3d;

import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.conduit.geom.ConduitConnectorType;
import crazypants.enderio.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.conduit.render.ConduitBundleRenderer;
import crazypants.enderio.conduit.render.ConduitRenderer;
import crazypants.enderio.conduit.render.DefaultConduitRenderer;

public class RedstoneSwitchRenderer extends DefaultConduitRenderer {

    public static final ThreadLocal<ConduitRenderer> instance = ThreadLocal.withInitial(RedstoneSwitchRenderer::new);

    private final VertexTransform[] xForms;
    private final BoundingBox switchBounds;
    private final BoundingBox connectorBounds;

    public RedstoneSwitchRenderer() {
        xForms = RedstoneSwitchBounds.get().xForms;
        switchBounds = RedstoneSwitchBounds.get().switchBounds;
        connectorBounds = RedstoneSwitchBounds.get().connectorBounds;
    }

    @Override
    public void renderEntity(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle bundle, IConduit conduit,
            double x, double y, double z, float partialTick, float worldLight, RenderBlocks rb) {

        super.renderEntity(conduitBundleRenderer, bundle, conduit, x, y, z, partialTick, worldLight, rb);

        RedstoneSwitch sw = (RedstoneSwitch) conduit;
        int i;

        final Tessellator tessellator = Tessellator.instance;
        final CubeRenderer cr = CubeRenderer.get();
        float selfIllum = Math.max(worldLight, conduit.getSelfIlluminationForState(null));
        tessellator.setColorOpaque_F(selfIllum, selfIllum, selfIllum);

        IIcon[] icons = new IIcon[6];
        for (i = 0; i < icons.length; i++) {
            icons[i] = EnderIO.blockConduitBundle.getConnectorIcon(ConduitConnectorType.INTERNAL);
        }
        icons[3] = sw.getSwitchIcon();

        Vector3d trans = ConduitGeometryUtil.instance.getTranslation(
                ForgeDirection.UNKNOWN,
                bundle.getOffset(IRedstoneConduit.class, ForgeDirection.UNKNOWN));
        BoundingBox bb = switchBounds.translate(trans);

        for (i = 0; i < xForms.length; i++) {
            cr.render(bb, icons, xForms[i], null);
        }
        bb = connectorBounds.translate(trans);
        for (i = 0; i < xForms.length; i++) {
            cr.render(bb, icons[0], xForms[i]);
        }
    }

    @Override
    protected boolean renderComponent(CollidableComponent component) {
        return !RedstoneSwitch.SWITCH_TAG.equals(component.data);
    }
}
