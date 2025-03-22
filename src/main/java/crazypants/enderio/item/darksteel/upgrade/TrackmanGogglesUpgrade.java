package crazypants.enderio.item.darksteel.upgrade;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.config.Config;
import crazypants.enderio.item.darksteel.DarkSteelItems;

public class TrackmanGogglesUpgrade extends AbstractUpgrade {

    private static String UPGRADE_NAME = "trackmansGoggles";

    public static final TrackmanGogglesUpgrade INSTANCE = new TrackmanGogglesUpgrade();

    public static ItemStack getGoggles() {
        Item i = GameRegistry.findItem("Railcraft", "armor.goggles");
        if (i != null) {
            return new ItemStack(i);
        }
        return null;
    }

    public static TrackmanGogglesUpgrade loadFromItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.stackTagCompound == null) {
            return null;
        }
        if (!stack.stackTagCompound.hasKey(KEY_UPGRADE_PREFIX + UPGRADE_NAME)) {
            return null;
        }
        return new TrackmanGogglesUpgrade(
                (NBTTagCompound) stack.stackTagCompound.getTag(KEY_UPGRADE_PREFIX + UPGRADE_NAME));
    }

    public static boolean isUpgradeEquipped(EntityPlayer player) {
        ItemStack helmet = player.getEquipmentInSlot(4);
        return TrackmanGogglesUpgrade.loadFromItem(helmet) != null;
    }

    public TrackmanGogglesUpgrade(NBTTagCompound tag) {
        super(UPGRADE_NAME, tag);
    }

    public TrackmanGogglesUpgrade() {
        super(
                UPGRADE_NAME,
                "enderio.darksteel.upgrade.trackmansGoggles",
                getGoggles(),
                Config.darkSteelTrackmanGogglesCost);
    }

    @Override
    public boolean canAddToItem(ItemStack stack) {
        if (stack == null || !DarkSteelItems.isArmorPart(stack.getItem(), 0) || getUpgradeItem() == null) {
            return false;
        }
        TrackmanGogglesUpgrade up = loadFromItem(stack);
        return up == null;
    }

    @Override
    public void writeUpgradeToNBT(NBTTagCompound upgradeRoot) {}

    @Override
    public ItemStack getUpgradeItem() {
        if (upgradeItem != null) {
            return upgradeItem;
        }
        upgradeItem = getGoggles();
        return upgradeItem;
    }

    @Override
    public String getUpgradeItemName() {
        if (getUpgradeItem() == null) {
            return "Trackman's Helmet";
        }
        return super.getUpgradeItemName();
    }
}
