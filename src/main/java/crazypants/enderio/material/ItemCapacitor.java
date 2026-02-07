package crazypants.enderio.material;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import com.enderio.core.client.handlers.SpecialTooltipHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.EnderIOTab;
import crazypants.enderio.ModObject;
import crazypants.enderio.config.Config;
import crazypants.enderio.power.BasicCapacitor;
import crazypants.enderio.power.Capacitors;
import crazypants.enderio.power.ICapacitor;
import crazypants.enderio.power.ICapacitorItem;

import static net.minecraft.util.EnumChatFormatting.RESET;

public class ItemCapacitor extends Item implements ICapacitorItem {

    private static final BasicCapacitor CAP = new BasicCapacitor();
    private static final String SPACE = "    ";

    public static ItemCapacitor create() {
        ItemCapacitor result = new ItemCapacitor();
        result.init();
        return result;
    }

    private final IIcon[] icons;

    protected ItemCapacitor() {
        setCreativeTab(EnderIOTab.tabEnderIO);
        setUnlocalizedName(ModObject.itemBasicCapacitor.unlocalisedName);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(64);

        icons = new IIcon[Capacitors.values().length];
    }

    protected void init() {
        GameRegistry.registerItem(this, ModObject.itemBasicCapacitor.unlocalisedName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        damage = MathHelper.clamp_int(damage, 0, Capacitors.values().length - 1);
        return icons[damage];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister IIconRegister) {
        for (int i = 0; i < Capacitors.values().length; i++) {
            icons[i] = IIconRegister.registerIcon(Capacitors.values()[i].iconKey);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, Capacitors.values().length - 1);
        return Capacitors.values()[i].unlocalisedName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        for (int j = 0; j < Capacitors.values().length; ++j) {
            par3List.add(new ItemStack(par1, 1, j));
            if (Capacitors.values()[j] == Capacitors.TOTEMIC_CAPACITOR) {
                ItemStack stack = new ItemStack(par1, 1, j);
                stack.addEnchantment(Enchantment.efficiency, 5);
                par3List.add(stack);
            }
        }
    }

    @Override
    public ICapacitor getCapacitor(ItemStack stack) {
        int damage = MathHelper.clamp_int(stack.getItemDamage(), 0, Capacitors.values().length - 1);

        if (Capacitors.values()[damage] == Capacitors.TOTEMIC_CAPACITOR) {
            damage = MathHelper.clamp_int(
                    EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) + 1,
                    1,
                    Capacitors.TOTEMIC_CAPACITOR.ordinal());
        }

        return Capacitors.values()[damage].capacitor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List,
            boolean par4) {
        if (par1ItemStack != null && par1ItemStack.getItemDamage() > 0 && par1ItemStack.getItemDamage() != 7) {
            ICapacitor capacitor = getCapacitor(par1ItemStack);
            par3List.add(EnumChatFormatting.GREEN + EnderIO.lang.localize("machine.tooltip.upgrade.name"));
            if (SpecialTooltipHandler.showAdvancedTooltips()) {
                addUpgrades(par3List, capacitor);
                addRangeUpgrade(par3List, capacitor);
            } else {
                SpecialTooltipHandler.addShowDetailsTooltip(par3List);
            }
        }
    }

    public void addUpgrades(List<String> list, ICapacitor capacitor) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String tierString = EnumChatFormatting.AQUA + EnderIO.lang.localize("machine.tooltip.upgrade.tier");
        String maxEnergyStorageString = EnumChatFormatting.AQUA
                + EnderIO.lang.localize("machine.tooltip.upgrade.energystorage");
        String maxEnergyReceivedString = EnumChatFormatting.AQUA
                + EnderIO.lang.localize("machine.tooltip.upgrade.energyreceived");
        String maxEnergyExtractedString = EnumChatFormatting.AQUA
                + EnderIO.lang.localize("machine.tooltip.upgrade.energyextracted");
        String upgrades = EnumChatFormatting.GOLD + EnderIO.lang.localize("machine.tooltip.upgrade");
        String maxEnergyStorage = formatter.format(capacitor.getMaxEnergyStored());
        int tier = capacitor.getTier();
        String maxEnergyReceived = formatter.format(capacitor.getMaxEnergyReceived());
        String maxEnergyExtracted = formatter.format(capacitor.getMaxEnergyExtracted());
        list.add(upgrades);
        list.add(SPACE + tierString + RESET + tier);
        list.add(SPACE + maxEnergyReceivedString + RESET + maxEnergyReceived);
        list.add(SPACE + maxEnergyExtractedString + RESET + maxEnergyExtracted);
        list.add(SPACE + maxEnergyStorageString + RESET + maxEnergyStorage);
    }

    public void addRangeUpgrade(List<String> list, ICapacitor capacitor) {
        String farmStation = EnumChatFormatting.AQUA + EnderIO.lang.localize("machine.tooltip.upgrade.farmstation");
        String rangeUpgrade = EnumChatFormatting.GOLD + EnderIO.lang.localize("machine.tooltip.upgrade.range");
        int range = Config.farmBonusSize * capacitor.getTier() - 1 + 2;
        list.add(rangeUpgrade);
        list.add(SPACE + farmStation + RESET + range);
    }
}
