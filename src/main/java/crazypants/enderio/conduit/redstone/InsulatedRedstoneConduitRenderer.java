package crazypants.enderio.conduit.redstone;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import com.enderio.core.client.render.CubeRenderer;

import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.conduit.render.ConduitRenderer;
import crazypants.enderio.conduit.render.DefaultConduitRenderer;

public class InsulatedRedstoneConduitRenderer extends DefaultConduitRenderer {

    public static final ThreadLocal<ConduitRenderer> instance = ThreadLocal
            .withInitial(InsulatedRedstoneConduitRenderer::new);

    @Override
    protected void renderConduit(IIcon tex, IConduit conduit, CollidableComponent component, float selfIllum) {
        if (IInsulatedRedstoneConduit.COLOR_CONTROLLER_ID.equals(component.data)) {
            if (conduit.containsExternalConnection(component.dir)
                    && !((IInsulatedRedstoneConduit) conduit).isSpecialConnection(component.dir)) {
                int c = ((IInsulatedRedstoneConduit) conduit).getSignalColor(component.dir).getColor();
                Tessellator tessellator = Tessellator.instance;
                tessellator.setColorOpaque_I(c);
                CubeRenderer.get().render(component.bound, tex);
                tessellator.setColorOpaque(255, 255, 255);
            }
        } else {
            super.renderConduit(tex, conduit, component, selfIllum);
        }
    }
}
