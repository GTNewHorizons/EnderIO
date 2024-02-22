package crazypants.enderio.conduit.oc;

import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.DyeColor;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.render.ConduitRenderer;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputersAPI|Network"),
        @Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "OpenComputersAPI|Network") })
public interface IOCConduit extends IConduit, Environment, SidedEnvironment {

    String COLOR_CONTROLLER_ID = "ColorController";

    @Override
    default ConduitRenderer getRenderer() {
        return OCConduitRenderer.instance.get();
    }

    void invalidate();

    void setSignalColor(ForgeDirection dir, DyeColor col);

    DyeColor getSignalColor(ForgeDirection dir);
}
