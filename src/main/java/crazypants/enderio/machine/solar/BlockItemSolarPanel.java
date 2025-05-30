package crazypants.enderio.machine.solar;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.EnderIOTab;
import crazypants.enderio.config.Config;
import crazypants.enderio.power.PowerDisplayUtil;

public class BlockItemSolarPanel extends ItemBlockWithMetadata
        implements IAdvancedTooltipProvider, IResourceTooltipProvider {

    public BlockItemSolarPanel() {
        super(EnderIO.blockSolarPanel, EnderIO.blockSolarPanel);
        setHasSubtypes(true);
        setCreativeTab(EnderIOTab.tabEnderIO);
    }

    public BlockItemSolarPanel(Block block) {
        super(block, block);
        setHasSubtypes(true);
        setCreativeTab(EnderIOTab.tabEnderIO);
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int meta = par1ItemStack.getItemDamage();
        String result = super.getUnlocalizedName(par1ItemStack);
        if (meta == 1) result += ".advanced";
        if (meta == 2) result += ".vibrant";

        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        ItemStack stack = new ItemStack(this, 1, 0);
        par3List.add(stack);
        stack = new ItemStack(this, 1, 1);
        par3List.add(stack);
        stack = new ItemStack(this, 1, 2);
        par3List.add(stack);
    }

    @Override
    public void addCommonEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        SpecialTooltipHandler.addCommonTooltipFromResources(list, itemstack);
    }

    @Override
    public void addBasicEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {}

    @Override
    public void addDetailedEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        SpecialTooltipHandler.addDetailedTooltipFromResources(list, itemstack);
        int prod = Config.maxPhotovoltaicOutputRF;
        if (itemstack.getItemDamage() == 1) prod = Config.maxPhotovoltaicAdvancedOutputRF;
        if (itemstack.getItemDamage() == 2) prod = Config.maxPhotovoltaicVibrantOutputRF;

        list.add(EnderIO.lang.localize("maxSolorProduction") + " " + PowerDisplayUtil.formatPowerPerTick(prod));
    }

    @Override
    public String getUnlocalizedNameForTooltip(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack);
    }
}
