package crazypants.enderio.conduit.power;

import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IExtractor;
import crazypants.enderio.conduit.render.ConduitRenderer;
import crazypants.enderio.power.ICapacitor;
import crazypants.enderio.power.IInternalPowerHandler;
import crazypants.enderio.power.IPowerInterface;

public interface IPowerConduit extends IConduit, IInternalPowerHandler, IExtractor {

    String ICON_KEY = "enderio:powerConduit";
    String ICON_KEY_INPUT = "enderio:powerConduitInput";
    String ICON_KEY_OUTPUT = "enderio:powerConduitOutput";
    String ICON_CORE_KEY = "enderio:powerConduitCore";
    String ICON_TRANSMISSION_KEY = "enderio:powerConduitTransmission";

    String COLOR_CONTROLLER_ID = "ColorController";

    @Override
    default ConduitRenderer getRenderer() {
        return PowerConduitRenderer.instance.get();
    }

    IPowerInterface getExternalPowerReceptor(ForgeDirection direction);

    ICapacitor getCapacitor();

    int getMaxEnergyExtracted(ForgeDirection dir);

    @Override
    int getMaxEnergyRecieved(ForgeDirection dir);

    IIcon getTextureForInputMode();

    IIcon getTextureForOutputMode();

    // called from NetworkPowerManager
    void onTick();

    boolean getConnectionsDirty();
}
