package crazypants.enderio.machine.invpanel.server;

import net.minecraft.item.ItemStack;

import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;

class JabbaBarrelInventory extends AbstractInventory {

    final TileEntityBarrel barrel;

    JabbaBarrelInventory(TileEntityBarrel barrel) {
        this.barrel = barrel;
        this.slotKeys = new SlotKey[1];
    }

    @Override
    int scanInventory(InventoryDatabaseServer db) {
        ItemStack stored = barrel.getStoredItemType();
        if (stored == null || stored.stackSize <= 0) {
            setEmpty(db);
            return 0;
        }
        updateSlot(db, 0, stored, stored.stackSize);
        return 1;
    }

    @Override
    int extractItem(InventoryDatabaseServer db, ItemEntry entry, int slot, int count) {
        ItemStack stored = barrel.getStoredItemType();
        if (stored == null || stored.stackSize <= 0) {
            return 0;
        }
        if (db.lookupItem(stored, entry, false) != entry) {
            return 0;
        }
        int available = stored.stackSize;
        if (count > available) {
            count = available;
        }
        int remaining = available - count;
        barrel.setStoredItemCount(remaining);
        updateCount(db, 0, entry, remaining);
        return count;
    }
}
