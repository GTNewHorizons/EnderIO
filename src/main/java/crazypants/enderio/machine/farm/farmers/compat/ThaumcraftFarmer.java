package crazypants.enderio.machine.farm.farmers.compat;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.farm.farmers.FarmersCommune;
import crazypants.enderio.machine.farm.farmers.ManaBeanFarmer;
import crazypants.enderio.machine.farm.farmers.TreeFarmer;

public class ThaumcraftFarmer {

    public static void init() {

        String mod = "Thaumcraft";
        String manaBean = "ItemManaBean";
        String manaPod = "blockManaPod";
        Block block = GameRegistry.findBlock(mod, manaPod);
        Item item = GameRegistry.findItem(mod, manaBean);
        Block saplings = GameRegistry.findBlock(mod, "blockCustomPlant");
        Block logs = GameRegistry.findBlock(mod, "blockMagicalLog");
        if (Config.farmManaBeansEnabled && block != null && item != null) {
            FarmersCommune.joinCommune(new ManaBeanFarmer(block, new ItemStack(item)));
        }
        if (saplings != null && logs != null) {
            FarmersCommune.joinCommune(new MetaDependentTreeFarmer(saplings, new boolean[] { true, true }, logs));
        }
    }

    private static class MetaDependentTreeFarmer extends TreeFarmer {

        private final boolean[] acceptableMetas;

        public MetaDependentTreeFarmer(Block sapling, boolean[] acceptableMetas, Block... wood) {
            super(sapling, wood);
            this.acceptableMetas = acceptableMetas;
        }

        @Override
        public boolean canPlant(ItemStack stack) {
            return super.canPlant(stack) && acceptableMetas.length > stack.getItemDamage()
                    && acceptableMetas[stack.getItemDamage()];
        }
    }
}
