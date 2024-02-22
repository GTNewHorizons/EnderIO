package crazypants.enderio.conduit.redstone;

import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.DyeColor;

import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.render.ConduitRenderer;

public interface IInsulatedRedstoneConduit extends IRedstoneConduit {

    String KEY_INS_CONDUIT_ICON = "enderio:redstoneInsulatedConduit";
    String KEY_INS_CORE_OFF_ICON = "enderio:redstoneInsulatedConduitCoreOff";
    String KEY_INS_CORE_ON_ICON = "enderio:redstoneInsulatedConduitCoreOn";

    String COLOR_CONTROLLER_ID = "ColorController";

    @Override
    default ConduitRenderer getRenderer() {
        return InsulatedRedstoneConduitRenderer.instance.get();
    }

    void onInputsChanged(ForgeDirection side, int[] inputValues);

    void onInputChanged(ForgeDirection side, int inputValue);

    void forceConnectionMode(ForgeDirection dir, ConnectionMode mode);

    void setSignalColor(ForgeDirection dir, DyeColor col);

    boolean isSpecialConnection(ForgeDirection dir);

    boolean isOutputStrong(ForgeDirection dir);

    void setOutputStrength(ForgeDirection dir, boolean isStrong);
}
