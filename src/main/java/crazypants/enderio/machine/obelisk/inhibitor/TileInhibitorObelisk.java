package crazypants.enderio.machine.obelisk.inhibitor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.enderio.core.common.vecmath.Vector3d;

import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractPowerConsumerEntity;
import crazypants.enderio.machine.SlotDefinition;
import crazypants.enderio.machine.ranged.IRanged;

public class TileInhibitorObelisk extends AbstractPowerConsumerEntity implements IRanged {

    private float range = 32;

    public TileInhibitorObelisk() {
        super(new SlotDefinition(0, 0, 1));
    }

    @Override
    public String getMachineName() {
        return ModObject.blockInhibitorObelisk.unlocalisedName;
    }

    @Override
    protected boolean isMachineItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean isActive() {
        return hasPower();
    }

    @Override
    protected boolean processTasks(boolean redstoneCheckPassed) {
        return redstoneCheckPassed;
    }

    @Override
    public World getWorld() {
        return worldObj;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return null;
    }

    @Override
    public Vector3d getRange() {
        return new Vector3d(range, range, range);
    }

    public void setRange(float range) {
        this.range = range;
        BlockInhibitorObelisk.instance.activeInhibitors.put(getLocation(), range);
    }

    @Override
    public boolean isShowingRange() {
        return false;
    }

    @Override
    public int getColor() {
        return 0x66FFFFFF;
    }

    @Override
    public void validate() {
        super.validate();
        BlockInhibitorObelisk.instance.activeInhibitors.put(getLocation(), range);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        BlockInhibitorObelisk.instance.activeInhibitors.remove(getLocation());
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        BlockInhibitorObelisk.instance.activeInhibitors.remove(getLocation());
    }
}
