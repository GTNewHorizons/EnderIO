package crazypants.enderio.conduit.item.filter;

import java.util.List;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.enderio.core.client.gui.widget.GhostSlot;

import cofh.lib.util.helpers.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.conduit.gui.GuiExternalConnection;
import crazypants.enderio.conduit.gui.item.IItemFilterGui;
import crazypants.enderio.conduit.gui.item.ItemConduitFilterContainer;
import crazypants.enderio.conduit.gui.item.LimitedItemFilterGui;
import crazypants.enderio.conduit.item.IItemConduit;
import crazypants.enderio.conduit.item.NetworkedInventory;

public class ItemFilterLimited extends ItemFilter {

    public ItemFilterLimited() {
        this(10, true);
    }

    public ItemFilterLimited(int numItems, boolean isAdvanced) {
        super(numItems, isAdvanced);
    }

    @Override
    public String getInventoryName() {
        return "Limited Item Filter";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemFilterGui getGui(GuiExternalConnection gui, IItemConduit itemConduit, boolean isInput) {
        ItemConduitFilterContainer cont = new ItemConduitFilterContainer(itemConduit, gui.getDir(), isInput);
        LimitedItemFilterGui limitedItemFilterGui = new LimitedItemFilterGui(gui, cont, !isInput, isInput);
        limitedItemFilterGui.createFilterSlots();
        return limitedItemFilterGui;
    }

    @Override
    public void createGhostSlots(List<GhostSlot> slots, int xOffset, int yOffset, Runnable cb) {
        int index = 0;
        int numRows = (int) (double) (items.length / 5);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < 5; ++col) {
                int x = xOffset + col * 18;
                int y = yOffset + row * 20;
                slots.add(new ItemFilterGhostSlot(index, x, y, cb));
                index++;
            }
        }
    }

    @Override
    public int getMaxCountThatCanPassFilter(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack invStack = items[i];
            if (invStack != null && itemStackMatched(invStack, item, i)) {
                count += invStack.stackSize;
            }
        }
        return count;
    }

    @Override
    public boolean isLimited() {
        return true;
    }

    public int getInsertLimit(ISidedInventory inv, ItemStack item) {
        if (item == null || item.getItem() == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack invStack = inv.getStackInSlot(i);
            if (invStack != null && itemStackMatched(invStack, item, i)) {
                count += invStack.stackSize;
            }
        }
        return getMaxCountThatCanPassFilter(item) - count;
    }

    public int getInsertLimitInv(ISidedInventory inv, ItemStack item) {
        return -getInsertLimit(inv, item);
    }

    public boolean doesCountPassFilter(int maxCount) {
        return maxCount > 0;
    }

    @Override
    public boolean doesItemPassFilter(NetworkedInventory inv, ItemStack item) {
        return isValid() && itemMatched(item) && doesCountPassFilter(getInsertLimit(inv.getInventory(), item));

    }

    // Filter in extract slot
    public boolean doesItemPassFilterInv(NetworkedInventory inv, ItemStack item) {
        return isValid() && itemMatched(item) && doesCountPassFilter(getInsertLimitInv(inv.getInventory(), item));
    }

    protected boolean itemStackMatched(ItemStack item, ItemStack filter, int slot) {
        if (item == null) {
            return false;
        }

        boolean checkDamage = fuzzyMode != FuzzyMode.DISABLED;
        boolean damageMatched = false;
        if (checkDamage && item.getItem().isDamageable()) {
            damageMatched = fuzzyMode.compare(item);
        }

        if (isItemListEmpty()) {
            if (checkDamage) {
                return damageMatched;
            } else {
                return false;
            }
        }

        boolean matched = false;

        if (filter != null && item.getItem() == filter.getItem()) {
            matched = true;

            if (checkDamage && !damageMatched) {
                matched = false;
            } else if (matchMeta && item.getItemDamage() != filter.getItemDamage()) {
                matched = false;
            } else if (matchNBT && !isNBTMatch(item, filter)) {
                matched = false;
            }
        }

        if (!matched && useOreDict && isOreDicMatch(filter, item)) {
            matched = true;
        }
        return matched;
    }

    public boolean isOreDicMatch(ItemStack filter, ItemStack item) {
        int[] id_filter = OreDictionary.getOreIDs(filter);
        if (id_filter.length == 0) {
            return false;
        }
        int[] id_item = OreDictionary.getOreIDs(item);
        if (id_item.length == 0) {
            return false;
        }
        for (int id1 : id_filter) {
            for (int id2 : id_item) {
                if (id1 == id2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack decrStackSize(int fromSlot, int amount) {
        if ((fromSlot < 0) || (fromSlot >= items.length)) {
            return null;
        }
        oreIds.set(fromSlot, null);
        ItemStack item = items[fromSlot];
        items[fromSlot] = null;
        return item.splitStack(amount);
    }

    @Override
    public String toString() {
        return "Limited" + super.toString();
    }

    @Override
    public String getUnlocalizedName() {
        return "gui.limited_item_filter";
    }

    class ItemFilterGhostSlot extends GhostSlot {

        private final int slot;
        private final Runnable cb;

        private final int adjustOnShift;

        ItemFilterGhostSlot(int slot, int x, int y, Runnable cb) {
            this.x = x;
            this.y = y;
            this.slot = slot;
            this.cb = cb;
            this.displayStdOverlay = true;
            this.stackSizeLimit = 64;
            this.adjustOnShift = 10;
        }

        @Override
        public void putStack(ItemStack stack) {
            if (stack != null) {
                stack = stack.copy();
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    if (Mouse.isButtonDown(0) || Mouse.getEventDWheel() > 0) {
                        if (stack.stackSize - 1 == 1) {
                            stack.stackSize += adjustOnShift - 2;
                        } else {
                            stack.stackSize = MathHelper
                                    .clamp(stack.stackSize + adjustOnShift - 1, 1, stack.getMaxStackSize());
                        }
                    } else if (Mouse.isButtonDown(1) || Mouse.getEventDWheel() < 0) {
                        if (stack.stackSize + 1 == 1) {
                            stack.stackSize++;
                        } else {
                            stack.stackSize = MathHelper
                                    .clamp(stack.stackSize - adjustOnShift + 1, 1, stack.getMaxStackSize());
                        }
                    }
                }
            }
            items[slot] = stack;
            cb.run();
        }

        @Override
        public ItemStack getStack() {
            return items[slot];
        }
    }
}
