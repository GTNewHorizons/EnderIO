package crazypants.enderio.machine;

import crazypants.enderio.machine.IMachineRecipe.ResultStack;
import crazypants.enderio.machine.recipe.RecipeBonusType;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;

public interface IPoweredTask {

    void update(float availableEnergy);

    boolean isComplete();

    float getProgress();

    ResultStack[] getCompletedResult();

    float getRequiredEnergy();

    float getChance();

    RecipeBonusType getBonusType();

    void writeToNBT(NBTTagCompound nbtRoot);

    @Nullable
    IMachineRecipe getRecipe();

    public abstract MachineRecipeInput[] getInputs();
}
