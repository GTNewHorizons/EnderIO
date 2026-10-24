package crazypants.enderio.machine.farm.farmers.compat;

import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;

public class GTPlusPlusFarmer {

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("miscutils:blockPineSapling"), Block.getBlockFromName("miscutils:blockPineLogLog")));
        FarmersCommune.joinCommune(new ExtraLargeTreeFarmer(Block.getBlockFromName("miscutils:blockRainforestOakSapling"), Block.getBlockFromName("miscutils:blockRainforestOakLog")));
    }

    private static class ExtraLargeTreeFarmer extends TreeFarmer {
        public ExtraLargeTreeFarmer(Block sapling, Block... wood) {
            super(sapling, wood);
        }

        @Override
        protected int extraTreeHeight() {
            return 100;
        }
    }
}
