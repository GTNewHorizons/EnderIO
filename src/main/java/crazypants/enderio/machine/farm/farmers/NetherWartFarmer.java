package crazypants.enderio.machine.farm.farmers;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.enderio.core.common.util.BlockCoord;

import crazypants.enderio.machine.farm.TileFarmStation;

public class NetherWartFarmer extends CustomSeedFarmer {

    public NetherWartFarmer() {
        super(Blocks.nether_wart, 3, new ItemStack(Items.nether_wart));
    }

    @Override
    public boolean prepareBlock(TileFarmStation farm, BlockCoord bc, Block block, int meta) {

        if (!farm.isOpen(bc)) {
            return false;
        }

        return plantFromInventory(farm, bc);
    }
}
