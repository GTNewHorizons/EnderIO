package crazypants.enderio.machine.enchanter;

import static crazypants.enderio.EnderIO.hasAutomagy;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.geometry.CubeIterator;

import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.Log;
import crazypants.enderio.ModObject;
import crazypants.enderio.TileEntityEio;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.obelisk.xp.TileExperienceObelisk;
import crazypants.enderio.xp.ExperienceContainer;
import crazypants.enderio.xp.XpUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class TileEnchanter extends TileEntityEio implements ISidedInventory {

    private final ItemStack[] inv = new ItemStack[3];
    private byte[] stacksizes = new byte[2];

    private IntList cachedXPsources = new IntArrayList();

    private short facing = (short) ForgeDirection.NORTH.ordinal();

    public void setFacing(short s) {
        facing = s;
    }

    public short getFacing() {
        return facing;
    }

    // The *ACTUAL* updateEntity and canUpdate are final in TileEntityEnder. Great.

    @Override
    protected void doUpdate() {
        if (!shouldUpdate_()) return;
        if (inv[0].stackSize != stacksizes[0] || inv[1].stackSize != stacksizes[1]) updateOut();
    }

    // @Override
    public boolean shouldUpdate_() {
        return inv[0] != null && inv[1] != null;
    }

    @Override
    protected void writeCustomNBT(NBTTagCompound root) {
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null) {
                NBTTagCompound itemStackNBT = new NBTTagCompound();
                itemStackNBT.setByte("Slot", (byte) i);
                inv[i].writeToNBT(itemStackNBT);
                itemList.appendTag(itemStackNBT);
            }
        }
        root.setTag("Items", itemList);
        root.setByteArray("SizeCache", stacksizes);
        root.setShort("facing", facing);
        root.setIntArray("XpCache", (cachedXPsources.size() <= 200 ? cachedXPsources : cachedXPsources.subList(0, 200)).toIntArray());
    }

    @Override
    protected void readCustomNBT(NBTTagCompound root) {
        NBTTagList itemList = (NBTTagList) root.getTag("Items");
        if (itemList != null) {
            for (int i = 0; i < itemList.tagCount(); i++) {
                NBTTagCompound itemStack = itemList.getCompoundTagAt(i);
                byte slot = itemStack.getByte("Slot");
                if (slot >= 0 && slot < inv.length) {
                    inv[slot] = ItemStack.loadItemStackFromNBT(itemStack);
                }
            }
        }
        stacksizes = root.getByteArray("SizeCache");
        if (stacksizes == null || stacksizes.length < 2) stacksizes = new byte[2];
        facing = root.getShort("facing");
        cachedXPsources.clear();
        int[] xpcache = root.getIntArray("XpCache");
        if (xpcache != null) {
          cachedXPsources.addAll(IntList.of(xpcache));
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public int getSizeInventory() {
        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot > inv.length - 1) {
            return null;
        }
        if (slot == 2 && !doesCraftHaveXP()) return null;
        return inv[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return decrStackSize(slot, amount, true);
    }

    public ItemStack decrStackSize(int slot, int amount, boolean auto) {
        if (amount <= 0 || slot < 0 || slot >= inv.length || inv[slot] == null) {
            return null;
        }
        ItemStack fromStack = inv[slot];
        if (fromStack == null) {
            return null;
        }
        if (slot == 2 && auto && checkAndDrainXP(Math.min(amount, fromStack.stackSize))) return null;
        if (fromStack.stackSize <= amount) {
            inv[slot] = null;
            updateOut();
            return fromStack;
        }
        ItemStack result = new ItemStack(fromStack.getItem(), amount, fromStack.getItemDamage());
        if (fromStack.stackTagCompound != null) {
            result.stackTagCompound = (NBTTagCompound) fromStack.stackTagCompound.copy();
        }
        fromStack.stackSize -= amount;
        updateOut();
        return result;
    }

    public boolean doesCraftHaveXP() {
        if (inv[2] == null) return false;
        int LV = getCurrentEnchantmentCost();
        if (LV == 0) return true;
        return checkAndCacheXPSources(XpUtil.getExperienceForLevel(LV));
    }

    public boolean checkAndCacheXPSources(int xpCost) {
        if (drainFromCache(xpCost, false)) return true;
        cachedXPsources.clear();
        int xp;
        CubeIterator iter;
        iter = new CubeIterator(8);
        while (iter.hasNext()) {
            iter.next();
            TileEntity te;
            if ((te = worldObj.getTileEntity(
                    iter.n + xCoord,
                    iter.l + yCoord,
                    iter.m + zCoord)) instanceof TileExperienceObelisk obelisk) {
                ExperienceContainer cont = obelisk.getContainer();
                xp = cont.getExperienceTotal();
                 if (xp != 0)
                    cachedXPsources.add(((iter.n & 0xff) << 16) + ((iter.l & 0xff) << 8) + (iter.m & 0xff));
                if (xp >= xpCost) return true;
                xpCost -= xp;
            } else if (te instanceof TileEntityJarXP jar) {
                xp = jar.getXP();
                if (xp != 0)
                    cachedXPsources.add(((iter.n & 0xff) << 16) + ((iter.l & 0xff) << 8) + (iter.m & 0xff));
                if (xp >= xpCost) return true;
                xpCost -= xp;
            }
        }
        return false;
    }

    public boolean drainFromCache(int xpCost, boolean actual) {
        ExperienceContainer cont;
        int len = cachedXPsources.size();
        if (len == 0) return false;
        for (int ind = 0; ind < len; ind ++) {
            int nlm = cachedXPsources.get(ind);
            int z = (byte) nlm + zCoord;
            int y = (byte) (nlm >>= 8) + yCoord;
            int x = (byte) (nlm >>= 8) + xCoord;
            if (!worldObj.blockExists(x, y, z)) continue;
            TileEntity te = worldObj.getTileEntity(x, y, z);
            if (te instanceof TileExperienceObelisk obelisk) {
                cont = obelisk.getContainer();
                int xp = cont.getExperienceTotal();
                if (xp == 0) continue;
                if (actual) cont.drain(null, Integer.MAX_VALUE, true);
                int ebx = xp - xpCost;
                if (ebx < 0) {
                    xpCost = -ebx;
                    continue;
                }
                if (actual) cont.addExperience(ebx);
                return true;
            }
            if (hasAutomagy && te instanceof TileEntityJarXP jar) {
                int xp = jar.getXP();
                if (xp == 0) continue;
                int ebx = xp - xpCost; // 5head JIT optimization
                if (ebx < 0) {
                    xpCost = -ebx;
                    if (actual) jar.setXP(0);
                    continue;
                }
                if (actual) jar.setXP(ebx);
                return true;
            }
            cachedXPsources.removeInt(ind);
        }
        return false;
    }

    // part of the next method
    public boolean absorbXP(int amt) {
        if (inv[2] == null) return false;
        int LV = getCurrentEnchantmentCost();
        if (LV == 0) return true;
        int xpCost = XpUtil.getExperienceForLevel(LV * amt);
        if (!checkAndCacheXPSources(xpCost)) return false;
        return drainFromCache(xpCost, !worldObj.isRemote);
    }

    // checks AND drains XP; returns false if xp is not sufficient
    // also removes the items from the other two slots when automation does the recipe
    public boolean checkAndDrainXP(int amt) {
        if (!absorbXP(amt)) return false;
        EnchantmentData enchData = getCurrentEnchantmentData();
        EnchanterRecipe recipe = getCurrentEnchantmentRecipe();
        ItemStack curStack = inv[1];
        if (recipe == null || enchData == null || curStack == null || enchData.enchantmentLevel >= curStack.stackSize) {
            inv[1] = null;
        } else {
            curStack = curStack.copy();
            curStack.stackSize -= recipe.getItemsPerLevel() * enchData.enchantmentLevel;
            inv[1] = curStack.stackSize > 0 ? curStack : null;
            markDirty();
        }

        curStack = inv[0];
        if (curStack == null || curStack.stackSize <= 1) inv[0] = null;
        else inv[0].stackSize -= 1;

        if (!worldObj.isRemote) {
            worldObj.playSoundEffect(xCoord + 0.5d, yCoord + 0.5d, zCoord + 0.5d, "random.anvil_land", 0.2f, 1.0f);
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack contents) {
        if (slot == 2) {
            if (inv[2] == null || inv[2].stackSize <= 0 || contents != null && contents.stackSize > 0
                    && contents.getItem() == Items.enchanted_book
                    && contents.stackSize == inv[2].stackSize)
                return;
            if (checkAndDrainXP(1)) Log.warn("Potentially duped books at: " + xCoord + " " + yCoord + " " + zCoord);
            // return;
        } ;
        if (contents == null) {
            inv[slot] = contents;
        } else {
            inv[slot] = contents.copy();
        }
        if (contents != null && contents.stackSize > getInventoryStackLimit()) {
            contents.stackSize = getInventoryStackLimit();
        }
        updateOut();
    }

    public void updateOut() {
        ItemStack output = null;
        EnchantmentData enchantment = getCurrentEnchantmentData();
        if (enchantment != null) {
            output = new ItemStack(Items.enchanted_book);
            Items.enchanted_book.addEnchantment(output, enchantment);
        }

        if (inv[0] != null) stacksizes[0] = (byte) inv[0].stackSize;
        if (inv[1] != null) stacksizes[1] = (byte) inv[1].stackSize;

        setOutput(output);
    }

    @Override
    public String getInventoryName() {
        return ModObject.blockEnchanter.unlocalisedName;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (slot == 0) {
            return Items.writable_book == stack.getItem();
        }
        if (slot == 1) {
            return EnchanterRecipeManager.getInstance().getEnchantmentRecipeForInput(stack) != null;
        }
        return false;
    }

    public EnchanterRecipe getCurrentEnchantmentRecipe() {
        if (inv[0] == null) {
            return null;
        }
        if (inv[1] == null) {
            return null;
        }
        EnchanterRecipe ench = EnchanterRecipeManager.getInstance().getEnchantmentRecipeForInput(inv[1]);
        if (ench == null) {
            return null;
        }
        int level = ench.getLevelForStackSize(inv[1].stackSize);
        if (level <= 0) {
            return null;
        }
        return ench;
    }

    public EnchantmentData getCurrentEnchantmentData() {
        EnchanterRecipe rec = getCurrentEnchantmentRecipe();
        if (rec == null || inv[1] == null) {
            return null;
        }
        int level = rec.getLevelForStackSize(inv[1].stackSize);
        if (level <= 0) {
            return null;
        }
        return new EnchantmentData(rec.getEnchantment(), level);
    }

    // public static int getEnchantmentCost(EnchantmentData enchData) {
    // if(enchData == null) {
    // return 0;
    // }
    // int level = enchData.enchantmentLevel;
    // Enchantment enchantment = enchData.enchantmentobj;
    //
    // if(level > enchantment.getMaxLevel()) {
    // level = enchantment.getMaxLevel();
    // }
    //
    // int costPerLevel = 0;
    // switch (enchantment.getWeight()) {
    // case 1:
    // costPerLevel = 8;
    // //Stops silk touch and infinity being too cheap
    // if(enchantment.getMaxLevel() == 1) {
    // level = 2;
    // }
    // break;
    // case 2:
    // costPerLevel = 4;
    // case 3:
    // case 4:
    // case 6:
    // case 7:
    // case 8:
    // case 9:
    // default:
    // break;
    // case 5:
    // costPerLevel = 2;
    // break;
    // case 10:
    // costPerLevel = 1;
    // }
    //
    // int res = 4;
    // for (int i = 0; i < level; i++) {
    // res += costPerLevel * level;
    // }
    // return res;
    // }

    public int getCurrentEnchantmentCost() {
        return getEnchantmentCost(getCurrentEnchantmentRecipe());
    }

    public int getEnchantmentCost(EnchanterRecipe currentEnchantment) {
        ItemStack item = inv[1];
        if (item == null) {
            return 0;
        }
        if (currentEnchantment == null) {
            return 0;
        }
        int level = currentEnchantment.getLevelForStackSize(item.stackSize);
        return getEnchantmentCost(currentEnchantment, level);
    }

    public static int getEnchantmentCost(EnchanterRecipe recipe, int level) {
        if (level > recipe.getEnchantment().getMaxLevel()) {
            level = recipe.getEnchantment().getMaxLevel();
        }
        int costPerLevel = recipe.getCostPerLevel();
        int res = Config.enchanterBaseLevelCost;
        for (int i = 0; i < level; i++) {
            res += costPerLevel * level;
        }
        return res;
    }

    public void setOutput(ItemStack output) {
        inv[inv.length - 1] = output;
        markDirty();
    }

    public ItemStack getOutput() {
        return inv[2];
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side == 0 ? new int[] { 2 } : new int[] { 0, 1 };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack p_102007_2_, int p_102007_3_) {
        return slot == 0 || slot == 1;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack p_102008_2_, int p_102008_3_) {
        return slot == 2 ? isAllowedBlockBeneath() : true;
    }

    public boolean isAllowedBlockBeneath() {
        if (!Config.enchanterOutputEnableBlockRestriction) return true;
        var blockBeneath = worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
        for (String blocknam : Config.enchanterOutputFilterlist) {
            String[] splitname = blocknam.split(":", 1);
            if (GameRegistry.findBlock(splitname[0], splitname[1]) != blockBeneath) continue;
            return !Config.enchanterOutputAllowlistAsDenylist;
        }
        return Config.enchanterOutputAllowlistAsDenylist;
    }

}
