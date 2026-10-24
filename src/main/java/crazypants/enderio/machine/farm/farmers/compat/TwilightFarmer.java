package crazypants.enderio.machine.farm.farmers.compat;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;

public class TwilightFarmer {

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("TwilightForest:tile.TFSapling"), Block.getBlockFromName("TwilightForest:tile.TFLog")));
    }
}
