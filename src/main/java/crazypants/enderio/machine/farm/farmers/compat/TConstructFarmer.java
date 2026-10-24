package crazypants.enderio.machine.farm.farmers.compat;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;

public class TConstructFarmer {

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("TConstruct:slime.sapling"), Block.getBlockFromName("TConstruct:slime.gel")));
    }
}
