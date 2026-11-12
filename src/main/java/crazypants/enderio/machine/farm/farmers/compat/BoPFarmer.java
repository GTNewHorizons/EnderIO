package crazypants.enderio.machine.farm.farmers.compat;

import net.minecraft.block.Block;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;

public class BoPFarmer {

    public static void init() {
        Block[] logs = new Block[] { Block.getBlockFromName("BiomesOPlenty:bamboo"),
                Block.getBlockFromName("BiomesOPlenty:logs"), Block.getBlockFromName("BiomesOPlenty:logs1"),
                Block.getBlockFromName("BiomesOPlenty:logs2"), Block.getBlockFromName("BiomesOPlenty:logs3"),
                Block.getBlockFromName("BiomesOPlenty:logs4") };
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("BiomesOPlenty:saplings"), logs));
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("BiomesOPlenty:colorizedSaplings"), logs));
    }
}
