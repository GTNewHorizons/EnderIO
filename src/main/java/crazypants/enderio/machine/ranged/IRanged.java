package crazypants.enderio.machine.ranged;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.common.vecmath.Vector2d;
import com.enderio.core.common.vecmath.Vector2f;
import com.enderio.core.common.vecmath.Vector3d;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.enderio.core.common.util.BlockCoord;

import javax.annotation.Nonnull;

public interface IRanged {

    World getWorld();

    BlockCoord getLocation();

    AxisAlignedBB getBounds();

    Vector3d getRange();

    boolean isShowingRange();

    int getColor();
}
