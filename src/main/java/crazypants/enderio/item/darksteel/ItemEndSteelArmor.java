package crazypants.enderio.item.darksteel;

import java.util.Iterator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.item.darksteel.IDarkSteelItem.IEndSteelItem;
import crazypants.enderio.item.darksteel.upgrade.EnergyUpgrade;
import crazypants.enderio.item.darksteel.upgrade.IDarkSteelUpgrade;
import gregtech.api.hazards.Hazard;
import gregtech.api.hazards.IHazardProtector;

@Optional.InterfaceList({ @Optional.Interface(iface = "gregtech.api.hazards.IHazardProtector", modid = "gregtech_nh") })
public class ItemEndSteelArmor extends ItemDarkSteelArmor implements IEndSteelItem, IHazardProtector {

    public static final ArmorMaterial MATERIAL = EnumHelper
            .addArmorMaterial("endSteel", 50, new int[] { 4, 7, 10, 5 }, 25);

    public ItemEndSteelArmor(int armorType) {
        super(MATERIAL, "endSteel", armorType);
    }

    public static ItemEndSteelArmor create(int armorType) {
        ItemEndSteelArmor res = new ItemEndSteelArmor(armorType);
        res.init();
        return res;
    }

    public static ItemEndSteelArmor forArmorType(int armorType) {
        switch (armorType) {
            case 0:
                return DarkSteelItems.itemEndSteelHelmet;
            case 1:
                return DarkSteelItems.itemEndSteelChestplate;
            case 2:
                return DarkSteelItems.itemEndSteelLeggings;
            case 3:
                return DarkSteelItems.itemEndSteelBoots;
        }
        return null;
    }

    public static int getPoweredProtectionIncrease(int armorType) {
        switch (armorType) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
            case 3:
                return 1;
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        ItemStack is = new ItemStack(this);
        par3List.add(is);

        is = new ItemStack(this);
        EnergyUpgrade.EMPOWERED_FIVE.writeToItem(is);
        EnergyUpgrade.setPowerFull(is);

        Iterator<IDarkSteelUpgrade> iter = DarkSteelRecipeManager.instance.recipeIterator();
        while (iter.hasNext()) {
            IDarkSteelUpgrade upgrade = iter.next();
            if (!(upgrade instanceof EnergyUpgrade) && upgrade.canAddToItem(is)) {
                upgrade.writeToItem(is);
            }
        }

        par3List.add(is);
    }

    /// GT5 Hazmat protection
    @Optional.Method(modid = "gregtech_nh")
    @Override
    public boolean protectsAgainst(ItemStack itemStack, Hazard hazard) {
        return true;
    }
}
