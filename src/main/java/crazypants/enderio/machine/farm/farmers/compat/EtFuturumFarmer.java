package crazypants.enderio.machine.farm.farmers.compat;

import com.enderio.core.common.util.BlockCoord;
import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.machine.farm.TileFarmStation;
import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.StemFarmer;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EtFuturumFarmer {
    private static final Block CHORUS_FLOWER = Block.getBlockFromName("etfuturum:chorus_flower");
    private static final Block CHORUS_PLANT = Block.getBlockFromName("etfuturum:chorus_plant");

    public static void init() {
        FarmersCommune.joinCommune(new TreeFarmer(Block.getBlockFromName("etfuturum:sapling"), Block.getBlockFromName("etfuturum:cherry_log"))); //the mangrove saplings don't grow yet
        FarmersCommune.joinCommune(new StemFarmer(Block.getBlockFromName("etfuturum:bamboo"), GameRegistry.findItemStack("etfuturum", "bamboo",1)));
        FarmersCommune.joinCommune(new ChorusFarmer(CHORUS_FLOWER, CHORUS_FLOWER, CHORUS_PLANT));
    }

    private static class ChorusFarmer extends TreeFarmer {
        public ChorusFarmer(Block sapling, Block... wood) {
            super(true, sapling, wood);
        }

        @Override
        protected boolean canPlant(World worldObj, BlockCoord bc) {
            return worldObj.getBlock(bc.x, bc.y - 1, bc.z) == Blocks.end_stone;
        }

        @Override
        public boolean canHarvest(TileFarmStation farm, BlockCoord bc, Block block, int meta) {
                return super.canHarvest(farm, bc, block, meta) && !hasActiveFlower(farm.getWorld(), bc);
        }

        private static boolean hasActiveFlower(World world, BlockCoord bc) {
            Block block = bc.getBlock(world);
            int metadata = bc.getMetadata(world);
            if (block == CHORUS_FLOWER) {
                return metadata < 5;
            }
            if (block != CHORUS_PLANT) {
                return false;
            }
            BlockCoord coordAbove = bc.withY(bc.y + 1);
            Block blockAbove = coordAbove.getBlock(world);
            if (isChorusBlock(blockAbove)) {
                return hasActiveFlower(world, coordAbove);
            }
            for (EnumFacing facing: new EnumFacing[] {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST}) {
                BlockCoord offset = new BlockCoord(facing.getFrontOffsetX() + bc.x, bc.y, facing.getFrontOffsetZ() + bc.z);
                Block directionBlock = offset.getBlock(world);
                int metadataOffset = offset.getMetadata(world);
                if (directionBlock == CHORUS_FLOWER) {
                    if (metadataOffset < 5) {
                        return true;
                    }
                    continue;
                }
                if (directionBlock == CHORUS_PLANT && hasActiveFlower(world, offset.withY(offset.y + 1))) {
                    return true;

                }
            }
            return false;
        }

        private static boolean isChorusBlock(Block block) {
            return block == CHORUS_PLANT || block == CHORUS_FLOWER;
        }

        @Override
        protected boolean allowAllLogsToBeMined() {
            return true;
        }
    }
}
