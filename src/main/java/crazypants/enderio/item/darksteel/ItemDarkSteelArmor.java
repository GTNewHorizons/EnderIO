package crazypants.enderio.item.darksteel;

import java.util.Iterator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.common.util.ItemUtil;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.EnderIOTab;
import crazypants.enderio.config.Config;
import crazypants.enderio.item.darksteel.upgrade.ApiaristArmorUpgrade;
import crazypants.enderio.item.darksteel.upgrade.EnergyUpgrade;
import crazypants.enderio.item.darksteel.upgrade.IDarkSteelUpgrade;
import crazypants.enderio.item.darksteel.upgrade.NaturalistEyeUpgrade;
import crazypants.enderio.item.darksteel.upgrade.TrackmanGogglesUpgrade;
import crazypants.enderio.thaumcraft.GogglesOfRevealingUpgrade;
import forestry.api.apiculture.IArmorApiarist;
import forestry.api.core.IArmorNaturalist;
import mods.railcraft.api.core.items.IToolGoggles;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.items.ItemGoggles.GoggleAura;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;

@InterfaceList({ @Interface(iface = "thaumcraft.api.IGoggles", modid = "Thaumcraft"),
        @Interface(iface = "thaumcraft.api.IVisDiscountGear", modid = "Thaumcraft"),
        @Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = "Thaumcraft"),
        @Interface(iface = "mods.railcraft.api.core.items.IToolGoggles", modid = "Railcraft"),
        @Interface(iface = "forestry.api.apiculture.IArmorApiarist", modid = "Forestry"),
        @Interface(iface = "forestry.api.core.IArmorNaturalist", modid = "Forestry") })
public class ItemDarkSteelArmor extends ItemArmor
        implements IEnergyContainerItem, ISpecialArmor, IAdvancedTooltipProvider, IDarkSteelItem, IGoggles, IRevealer,
        IVisDiscountGear, IArmorApiarist, IArmorNaturalist, IToolGoggles {

    public static final ArmorMaterial MATERIAL = EnumHelper
            .addArmorMaterial("darkSteel", 35, new int[] { 2, 6, 5, 2 }, 15);

    public static final int[] CAPACITY = new int[] { Config.darkSteelPowerStorageBase, Config.darkSteelPowerStorageBase,
            Config.darkSteelPowerStorageBase * 2, Config.darkSteelPowerStorageBase * 2 };

    public static final int[] RF_PER_DAMAGE_POINT = new int[] { Config.darkSteelPowerStorageBase,
            Config.darkSteelPowerStorageBase, Config.darkSteelPowerStorageBase * 2,
            Config.darkSteelPowerStorageBase * 2 };

    public static final String[] NAMES = new String[] { "helmet", "chestplate", "leggings", "boots" };

    boolean gogglesUgradeActive = true;

    static {
        FMLCommonHandler.instance().bus().register(DarkSteelController.instance);
        MinecraftForge.EVENT_BUS.register(DarkSteelController.instance);
        MinecraftForge.EVENT_BUS.register(DarkSteelRecipeManager.instance);
    }

    public static ItemDarkSteelArmor forArmorType(int armorType) {
        switch (armorType) {
            case 0:
                return DarkSteelItems.itemDarkSteelHelmet;
            case 1:
                return DarkSteelItems.itemDarkSteelChestplate;
            case 2:
                return DarkSteelItems.itemDarkSteelLeggings;
            case 3:
                return DarkSteelItems.itemDarkSteelBoots;
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

    public static ItemDarkSteelArmor create(int armorType) {
        ItemDarkSteelArmor res = new ItemDarkSteelArmor(armorType);
        res.init();
        return res;
    }

    protected int powerPerDamagePoint;
    protected String name;

    public ItemDarkSteelArmor(int armorType) {
        this(MATERIAL, "darkSteel", armorType);
        powerPerDamagePoint = Config.darkSteelPowerStorageBase / MATERIAL.getDurability(armorType);
    }

    public ItemDarkSteelArmor(ArmorMaterial mat, String name, int armorType) {
        super(mat, 0, armorType);
        this.name = name;
        setCreativeTab(EnderIOTab.tabEnderIO);
        String str = name + "_" + NAMES[armorType];
        setUnlocalizedName(str);
        setTextureName(EnderIO.DOMAIN + ":" + str);
    }

    protected void init() {
        GameRegistry.registerItem(this, getUnlocalizedName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        ItemStack is = new ItemStack(this);
        par3List.add(is);

        is = new ItemStack(this);
        EnergyUpgrade.EMPOWERED_FOUR.writeToItem(is);
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

    @Override
    public int getIngotsRequiredForFullRepair() {
        switch (armorType) {
            case 0:
                return 5; // EnderIO.itemDarkSteelHelmet;
            case 1:
                return 8; // EnderIO.itemDarkSteelChestplate;
            case 2:
                return 7; // EnderIO.itemDarkSteelLeggings;
            case 3:
                return 4; // EnderIO.itemDarkSteelBoots;
        }
        return 4;
    }

    @Override
    public void addCommonEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        DarkSteelRecipeManager.instance.addCommonTooltipEntries(itemstack, entityplayer, list, flag);
    }

    @Override
    public void addBasicEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        DarkSteelRecipeManager.instance.addBasicTooltipEntries(itemstack, entityplayer, list, flag);
    }

    @Override
    public void addDetailedEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        if (!Config.addDurabilityTootip) {
            list.add(ItemUtil.getDurabilityString(itemstack));
        }
        String str = EnergyUpgrade.getStoredEnergyString(itemstack);
        if (str != null) {
            list.add(str);
        }
        if (EnergyUpgrade.itemHasAnyPowerUpgrade(itemstack)) {
            list.add(EnumChatFormatting.WHITE + EnderIO.lang.localize("item." + name + "_armor.tooltip.line1"));
            list.add(EnumChatFormatting.WHITE + EnderIO.lang.localize("item." + name + "_armor.tooltip.line2"));
            if (DarkSteelItems.isArmorPart(itemstack.getItem(), 3)) {
                list.add(EnumChatFormatting.WHITE + EnderIO.lang.localize("item." + name + "_boots.tooltip.line1"));
                list.add(EnumChatFormatting.WHITE + EnderIO.lang.localize("item." + name + "_boots.tooltip.line2"));
            }
        }
        DarkSteelRecipeManager.instance.addAdvancedTooltipEntries(itemstack, entityplayer, list, flag);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public String getArmorTexture(ItemStack itemStack, Entity entity, int slot, String layer) {
        if (armorType == 2) {
            return "enderio:textures/models/armor/" + name + "_layer_2.png";
        }
        return "enderio:textures/models/armor/" + name + "_layer_1.png";
    }

    public ItemStack createItemStack() {
        return new ItemStack(this);
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage,
            int slot) {
        if (source.isUnblockable()) {
            return new ArmorProperties(0, 0, armor.getMaxDamage() + 1 - armor.getItemDamage());
        }
        double damageRatio = damageReduceAmount
                + (getEnergyStored(armor) > 0 ? getPoweredProtectionIncrease(3 - slot) : 0);
        damageRatio /= 25D;
        ArmorProperties ap = new ArmorProperties(0, damageRatio, armor.getMaxDamage() + 1 - armor.getItemDamage());
        return ap;
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        ItemDarkSteelArmor arm = forArmorType(3 - slot);
        int powerBonus = getEnergyStored(armor) > 0 ? getPoweredProtectionIncrease(3 - slot) : 0;
        return arm.getArmorMaterial().getDamageReductionAmount(3 - slot) + powerBonus;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {

        EnergyUpgrade eu = EnergyUpgrade.loadFromItem(stack);
        if (eu != null && eu.isAbsorbDamageWithPower(stack) && eu.getEnergy() > 0) {
            eu.extractEnergy(damage * powerPerDamagePoint, false);

        } else {
            stack.damageItem(damage, entity);
        }
        if (eu != null) {
            eu.writeToItem(stack);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack i1, ItemStack i2) {
        return false;
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return EnergyUpgrade.receiveEnergy(container, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return EnergyUpgrade.extractEnergy(container, maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        return EnergyUpgrade.getEnergyStored(container);
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return EnergyUpgrade.getMaxEnergyStored(container);
    }

    // Thaumcraft

    @Override
    @Method(modid = "Thaumcraft")
    public boolean showNodes(ItemStack itemstack, EntityLivingBase player) {
        if (itemstack == null || itemstack.getItem() == null || !gogglesUgradeActive) {
            return false;
        }
        return GogglesOfRevealingUpgrade.loadFromItem(itemstack) != null;
    }

    @Override
    @Method(modid = "Thaumcraft")
    public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) {
        if (itemstack == null || itemstack.getItem() == null || !gogglesUgradeActive) {
            return false;
        }
        return GogglesOfRevealingUpgrade.loadFromItem(itemstack) != null;
    }

    @Override
    @Method(modid = "Thaumcraft")
    public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect) {
        if (stack == null || !DarkSteelItems.isArmorPart(stack.getItem(), 0)) {
            return 0;
        }
        return GogglesOfRevealingUpgrade.isUpgradeEquipped(player) ? 5 : 0;
    }

    public boolean isGogglesUgradeActive() {
        return gogglesUgradeActive;
    }

    public void setGogglesUgradeActive(boolean gogglesUgradeActive) {
        this.gogglesUgradeActive = gogglesUgradeActive;
    }

    // Forestry

    @Override
    public boolean protectEntity(EntityLivingBase entity, ItemStack armor, String cause, boolean doProtect) {
        return ApiaristArmorUpgrade.loadFromItem(armor) != null;
    }

    @Override
    @Method(modid = "Forestry")
    public boolean protectPlayer(EntityPlayer player, ItemStack armor, String cause, boolean doProtect) {
        return ApiaristArmorUpgrade.loadFromItem(armor) != null;
    }

    @Override
    @Method(modid = "Forestry")
    public boolean canSeePollination(EntityPlayer player, ItemStack armor, boolean doSee) {
        if (armor == null || !DarkSteelItems.isArmorPart(armor.getItem(), 0)) {
            return false;
        }
        return NaturalistEyeUpgrade.isUpgradeEquipped(player);
    }

    // Railcraft

    public void incrementAura(ItemStack goggles) {
        if (goggles != null && goggles.getItem() instanceof ItemDarkSteelArmor
                && DarkSteelItems.isArmorPart(goggles.getItem(), 0)) {
            NBTTagCompound data = goggles.getTagCompound();
            if (data == null) {
                data = new NBTTagCompound();
                goggles.setTagCompound(data);
            }
            byte aura = data.getByte("aura");
            aura++;
            if (aura >= GoggleAura.VALUES.length) aura = 0;
            data.setByte("aura", aura);

            if (this.getCurrentAura(goggles) == GoggleAura.TRACKING && !RailcraftConfig.isTrackingAuraEnabled()) {
                incrementAura(goggles);
            }
        }
    }

    @Override
    @Method(modid = "Railcraft")
    public GoggleAura getCurrentAura(ItemStack goggles) {
        if (TrackmanGogglesUpgrade.loadFromItem(goggles) != null) {
            NBTTagCompound data = goggles.getTagCompound();
            if (data != null) {
                return GoggleAura.VALUES[data.getByte("aura")];
            } else {
                return GoggleAura.NONE;
            }
        } else {
            return GoggleAura.NONE;
        }
    }
}
