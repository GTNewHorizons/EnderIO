package crazypants.enderio.machine.ranged;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.vecmath.Vector3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RangeEntity extends Entity {

    int totalLife = 20;
    int lifeSpan = totalLife;
    private Vector3d lastRange;
    private AxisAlignedBB lastBounds;
    private final IRanged spawnGuard;
    private int color;
    private AxisAlignedBB rangeBounds;
    private Vector3d range;

    public RangeEntity(IRanged sg) {
        super(sg.getWorld());
        spawnGuard = sg;
        BlockCoord bc = spawnGuard.getLocation();
        setPosition(bc.x - 0.01, bc.y - 0.01, bc.z - 0.01);
        ignoreFrustumCheck = true;
        color = sg.getColor();
        lastRange = spawnGuard.getRange();
        lastBounds = spawnGuard.getBounds();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return true;
    }

    @Override
    protected void entityInit() {}

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return spawnGuard.getBounds() != null ? spawnGuard.getBounds()
                : AxisAlignedBB.getBoundingBox(
                        posX - lastRange.x,
                        posY - lastRange.y,
                        posZ - lastRange.z,
                        posX + lastRange.x + 1,
                        posY + lastRange.y + 1,
                        posZ + lastRange.z + 1);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        lifeSpan--;
        BlockCoord bc = spawnGuard.getLocation();
        updateRange();

        if (!(worldObj.getTileEntity(bc.x, bc.y, bc.z) instanceof IRanged)) {
            setDead();
        }
        if (!spawnGuard.isShowingRange()) {
            setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {}

    private void updateRange() {
        if (lastRange != null && !lastRange.equals(spawnGuard.getRange())) {
            lastRange = spawnGuard.getRange();
        } else if (lastBounds != null && !lastBounds.equals(spawnGuard.getBounds())) {
            lastBounds = spawnGuard.getBounds();
        }
    }

    public float[] getColor() {
        return new float[] { (float) (this.color >> 16 & 255) / 255.0F, (float) (this.color >> 8 & 255) / 255.0F,
                (float) (this.color & 255) / 255.0F, (float) (this.color >> 24 & 255) / 255.0F };
    }
}
