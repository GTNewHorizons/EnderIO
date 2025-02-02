package crazypants.enderio.machine.ranged;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.vecmath.Vector3d;

public interface IRanged {

    World getWorld();

    BlockCoord getLocation();

    AxisAlignedBB getBounds();

    Vector3d getRange();

    boolean isShowingRange();

    int getColor();
}
