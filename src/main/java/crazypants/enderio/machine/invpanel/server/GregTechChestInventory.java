package crazypants.enderio.machine.invpanel.server;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import crazypants.enderio.conduit.item.NetworkedInventory;
import gregtech.common.tileentities.storage.MTEDigitalChestBase;

class GregTechChestInventory extends AbstractInventory {

    final NetworkedInventory ni;
    final MTEDigitalChestBase chest;

    GregTechChestInventory(NetworkedInventory ni, MTEDigitalChestBase chest) {
        this.ni = ni;
        this.chest = chest;
        this.slotKeys = new SlotKey[1];
    }

    private int getTotalCount() {
        int count = chest.getItemCount();
        ItemStack extra = chest.getExtraItemStack();
        if (extra != null) {
            count += extra.stackSize;
        }
        return count;
    }

    @Override
    int scanInventory(InventoryDatabaseServer db) {
        ItemStack storedItem = chest.getItemStack();
        int totalCount = getTotalCount();
        if (storedItem == null || totalCount <= 0) {
            setEmpty(db);
            return 0;
        }
        updateSlot(db, 0, storedItem, totalCount);
        return 1;
    }

    @Override
    int extractItem(InventoryDatabaseServer db, ItemEntry entry, int slot, int count) {
        ISidedInventory inv = ni.getInventoryRecheck();
        int side = ni.getInventorySide();
        int[] slotIndices = inv.getAccessibleSlotsFromSide(side);
        if (slotIndices == null) {
            return 0;
        }
        for (int slotIndex : slotIndices) {
            ItemStack stack = inv.getStackInSlot(slotIndex);
            if (stack == null || !inv.canExtractItem(slotIndex, stack, side)) {
                continue;
            }
            if (db.lookupItem(stack, entry, false) != entry) {
                continue;
            }
            int available = stack.stackSize;
            if (count > available) {
                count = available;
            }
            ni.itemExtracted(slotIndex, count);
            int remaining = getTotalCount();
            updateCount(db, 0, entry, remaining);
            return count;
        }
        return 0;
    }
}
