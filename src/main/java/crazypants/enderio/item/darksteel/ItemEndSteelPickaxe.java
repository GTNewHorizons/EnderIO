package crazypants.enderio.item.darksteel;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.item.darksteel.IDarkSteelItem.IEndSteelItem;
import crazypants.enderio.item.darksteel.upgrade.EnergyUpgrade;
import crazypants.enderio.item.darksteel.upgrade.TravelUpgrade;

public class ItemEndSteelPickaxe extends ItemDarkSteelPickaxe implements IEndSteelItem {

    public static boolean isEquipped(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack equipped = player.getCurrentEquippedItem();
        if (equipped == null) {
            return false;
        }
        return equipped.getItem() == DarkSteelItems.itemEndSteelPickaxe;
    }

    public static ItemEndSteelPickaxe create() {
        ItemEndSteelPickaxe res = new ItemEndSteelPickaxe();
        res.init();
        return res;
    }

    public ItemEndSteelPickaxe() {
        super("endSteel", ItemEndSteelSword.MATERIAL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        ItemStack is = new ItemStack(this);
        par3List.add(is);

        is = new ItemStack(this);
        EnergyUpgrade.EMPOWERED_FIVE.writeToItem(is);
        EnergyUpgrade.setPowerFull(is);
        TravelUpgrade.INSTANCE.writeToItem(is);
        par3List.add(is);
    }
}
