package crazypants.enderio.machine.farm.farmers.compat;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;

import com.enderio.core.common.util.BlockCoord;

import cpw.mods.fml.common.registry.GameData;
import crazypants.enderio.machine.farm.TileFarmStation;
import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.HarvestResult;
import crazypants.enderio.machine.farm.farmers.IHarvestResult;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;

public class PamsHarvestcraftFarmer {

    public static void init() {
        for (TreeType treetype : TreeType.values()) {
            treetype.registerTree();
        }
    }

    private enum TreeType {

        APPLE(Items.apple),
        ALMOND,
        APRICOT,
        AVOCADO,
        BANANA,
        CASHEW,
        CHERRY,
        CHESTNUT,
        CINNAMON(Block.getBlockFromName("harvestcraft:pamCinnamon"),
                GameData.getItemRegistry().getObject("harvestcraft:cinnamonItem")),
        COCONUT,
        DATE,
        DRAGONFRUIT,
        DURIAN,
        FIG,
        GRAPEFRUIT,
        LEMON,
        LIME,
        MAPLE(Block.getBlockFromName("harvestcraft:pamMaple"),
                GameData.getItemRegistry().getObject("harvestcraft:maplesyrupItem")),
        MANGO,
        NUTMEG,
        OLIVE,
        ORANGE,
        PAPAYA,
        PAPERBARK(Block.getBlockFromName("harvestcraft:pamPaperbark"), Items.paper),
        PEACH,
        PEAR,
        PECAN,
        PEPPERCORN,
        PERSIMMON,
        PISTACHIO,
        PLUM,
        POMEGRANATE,
        STARFRUIT,
        VANILLABEAN,
        WALNUT,
        GOOSEBERRY;

        final Block sapling;
        final Block log;
        final Item fruitItem;
        final Block fruitBlock;

        TreeType() {
            sapling = Block.getBlockFromName("harvestcraft:pam" + name().toLowerCase(Locale.ROOT) + "Sapling");
            log = Blocks.log;
            this.fruitItem = GameData.getItemRegistry()
                    .getObject("harvestcraft:" + name().toLowerCase(Locale.ROOT) + "Item");
            this.fruitBlock = Block.getBlockFromName(
                    "harvestcraft:pam" + name().charAt(0)
                            + name().toLowerCase(Locale.ROOT).substring(1, name().length()));
        }

        TreeType(Item fruit) {
            sapling = Block.getBlockFromName("harvestcraft:pam" + name().toLowerCase(Locale.ROOT) + "Sapling");
            log = Blocks.log;
            this.fruitItem = fruit;
            this.fruitBlock = Block.getBlockFromName(
                    "harvestcraft:pam" + name().charAt(0)
                            + name().toLowerCase(Locale.ROOT).substring(1, name().length()));
        }

        TreeType(Block log, Item fruit) {
            sapling = Block.getBlockFromName("harvestcraft:pam" + name().toLowerCase(Locale.ROOT) + "Sapling");
            this.log = log;
            this.fruitItem = fruit;
            this.fruitBlock = null;
        }

        private void registerTree() {
            if (sapling == null || log == null || fruitItem == null) {
                return;
            }
            if (this == CINNAMON || this == MAPLE || this == PAPERBARK) {
                FarmersCommune.joinCommune(new TreeScraperFarmer(sapling, fruitItem, log));
            } else {
                FarmersCommune.joinCommune(new TreeFarmer(sapling, log));
            }
        }
    }

    private static class TreeScraperFarmer extends TreeFarmer {

        private final Item fruit;

        public TreeScraperFarmer(Block sapling, Item fruit, Block... wood) {
            super(true, sapling, wood);
            this.fruit = fruit;
        }

        @Override
        public IHarvestResult harvestBlock(TileFarmStation farm, BlockCoord bc, Block block, int meta) {
            HarvestResult harvestResult = new HarvestResult();
            for (int y = bc.y; y < farm.getWorld().getActualHeight()
                    && bc.withY(y).getBlock(farm.getWorld()) == block; y++) {
                BlockCoord trunkBlock = bc.withY(y);
                block.onBlockActivated(
                        farm.getWorld(),
                        bc.x,
                        y,
                        bc.z,
                        farm.getFakePlayer(),
                        2 /* click side north NORTH */,
                        0f,
                        0f,
                        0f);
                for (EntityItem entityItem : farm.getWorld()
                        .getEntitiesWithinAABB(EntityItem.class, from(trunkBlock, 1))) {
                    if (entityItem.getEntityItem().getItem() == fruit && entityItem.age == 0
                            && !harvestResult.getDrops().contains(entityItem)) {
                        harvestResult.getDrops().add(entityItem);
                        harvestResult.getHarvestedBlocks().add(trunkBlock);
                    }
                }
            }
            return harvestResult;
        }

        private static AxisAlignedBB from(BlockCoord bc, float range) {
            return AxisAlignedBB
                    .getBoundingBox(bc.x - range, bc.y - range, bc.z - range, bc.x + range, bc.y + range, bc.z + range);
        }
    }
}
