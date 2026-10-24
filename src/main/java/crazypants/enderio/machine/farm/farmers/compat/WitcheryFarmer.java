package crazypants.enderio.machine.farm.farmers.compat;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;

public class WitcheryFarmer {

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("witchery:witchsapling"), Block.getBlockFromName("witchery:witchlog")));
    }
}
