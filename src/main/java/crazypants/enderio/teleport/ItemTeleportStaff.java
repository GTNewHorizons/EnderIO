package crazypants.enderio.teleport;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.ModObject;
import crazypants.enderio.api.teleport.TravelSource;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemTeleportStaff extends ItemInfiniteTravelStaff {

    protected ItemTeleportStaff() {
        super();
        setUnlocalizedName(ModObject.itemTeleportStaff.name());
    }

    public static ItemTeleportStaff create() {
        ItemTeleportStaff result = new ItemTeleportStaff();
        result.init();
        return result;
    }

    public static boolean isEquipped(EntityPlayer ep) {
        if (ep == null || ep.getCurrentEquippedItem() == null) {
            return false;
        }
        return ep.getCurrentEquippedItem().getItem() == EnderIO.itemTeleportStaff;
    }

    @Override
    protected void init() {
        GameRegistry.registerItem(this, ModObject.itemTeleportStaff.unlocalisedName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister IIconRegister) {
        itemIcon = IIconRegister.registerIcon("enderio:itemTeleportStaff");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack equipped, World world, EntityPlayer player) {
        if (world.isRemote) {
            if (player.isSneaking()) {
                TravelController.instance.activateTravelAccessable(
                        equipped, world, player, TravelSource.TELEPORT_STAFF);
            } else {
                TravelController.instance.doTeleport(player);
            }
        }
        player.swingItem();
        return equipped;
    }

    @Override
    public boolean isActive(EntityPlayer ep, ItemStack equipped) {
        return isEquipped(ep);
    }
}
