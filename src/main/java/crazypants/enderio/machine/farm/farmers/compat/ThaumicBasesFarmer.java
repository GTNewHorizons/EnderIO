package crazypants.enderio.machine.farm.farmers.compat;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;

public class ThaumicBasesFarmer {

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("thaumicbases:goldenOakSapling"), Block.getBlockFromName("thaumicbases:genLogs")));
    }
}
