package crazypants.enderio.machine.farm.farmers.compat;

import net.minecraft.block.Block;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;

public class TwilightFarmer {

    public static void init() {
        FarmersCommune.joinCommune(
                new TreeFarmer(
                        Block.getBlockFromName("TwilightForest:tile.TFSapling"),
                        Block.getBlockFromName("TwilightForest:tile.TFLog")));
    }
}
