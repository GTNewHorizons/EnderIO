package crazypants.enderio.machine.painter;

import java.util.List;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractPoweredTaskEntity;
import crazypants.enderio.machine.IMachineRecipe;
import crazypants.enderio.machine.MachineRecipeInput;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.SlotDefinition;

public class TileEntityPainter extends AbstractPoweredTaskEntity implements ISidedInventory {

    private static final int SLOT_INPUT = 0;
    private static final int SLOT_PAINT_SOURCE = 1;
    private static final int SLOT_OUTPUT = 2;

    public TileEntityPainter() {
        // 0 = input slot, 1 = paint source, 2 = output slot
        super(new SlotDefinition(2, 1));
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j) {
        return super.canExtractItem(i, itemstack, j)
                && PainterUtil.isMetadataEquivelent(itemstack, inventory[SLOT_OUTPUT]);
    }

    @Override
    public String getInventoryName() {
        return "Auto Painter";
    }

    @Override
    public boolean isMachineItemValidForSlot(int i, ItemStack itemStack) {
        if (i == SLOT_INPUT) {
            List<IMachineRecipe> recipes = MachineRecipeRegistry.instance
                    .getRecipesForInput(getMachineName(), MachineRecipeInput.create(i, itemStack));
            if (inventory[SLOT_PAINT_SOURCE] == null) {
                return !recipes.isEmpty();
            } else {
                for (IMachineRecipe rec : recipes) {
                    if (rec instanceof BasicPainterTemplate temp) {
                        if (temp.isValidPaintSource(inventory[SLOT_PAINT_SOURCE])) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else if (i == SLOT_PAINT_SOURCE) {
            if (inventory[SLOT_INPUT] == null) {
                Map<String, IMachineRecipe> recipes = MachineRecipeRegistry.instance
                        .getRecipesForMachine(getMachineName());
                for (IMachineRecipe rec : recipes.values()) {
                    if (rec instanceof BasicPainterTemplate temp) {
                        if (temp.isValidPaintSource(itemStack)) {
                            return true;
                        }
                    }
                }
                return PaintSourceValidator.instance.isValidSourceDefault(itemStack);
            } else {
                return MachineRecipeRegistry.instance.getRecipeForInputs(
                        getMachineName(),
                        targetInput(),
                        MachineRecipeInput.create(SLOT_PAINT_SOURCE, itemStack)) != null;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getMachineName() {
        return ModObject.blockPainter.unlocalisedName;
    }

    private MachineRecipeInput targetInput() {
        return MachineRecipeInput.create(SLOT_INPUT, inventory[SLOT_INPUT]);
    }

    private MachineRecipeInput paintSource() {
        return MachineRecipeInput.create(SLOT_PAINT_SOURCE, inventory[SLOT_PAINT_SOURCE]);
    }
}
