package crazypants.enderio.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.enderio.core.common.util.FluidUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.ItemStackMap;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.StackInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import crazypants.enderio.gui.GuiContainerBaseEIO;
import crazypants.enderio.machine.recipe.IRecipe;
import crazypants.enderio.machine.recipe.RecipeInput;
import crazypants.enderio.machine.vat.GuiVat;
import crazypants.enderio.machine.vat.VatRecipeManager;
import crazypants.enderio.power.PowerDisplayUtil;
import crazypants.util.ColorUtils;

public class VatRecipeHandler extends TemplateRecipeHandler {

    public VatRecipeHandler() {}

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("enderio.nei.vat");
    }

    @Override
    public String getGuiTexture() {
        return GuiContainerBaseEIO.getGuiTexture("vat").toString();
    }

    public PositionedStack getResult() {
        return null;
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return GuiVat.class;
    }

    @Override
    public String getOverlayIdentifier() {
        return "EnderIOVat";
    }

    @Override
    public void loadTransferRects() {
        transferRects.add(
                new TemplateRecipeHandler.RecipeTransferRect(
                        new Rectangle(70, 8, 28, 43),
                        "EnderIOVat",
                        new Object[0]));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("liquid")) {
            loadCraftingRecipes((FluidStack) results[0]);
        } else if (outputId.equals("EnderIOVat") && getClass() == VatRecipeHandler.class) {
            List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
            for (IRecipe recipe : recipes) {
                FluidStack output = recipe.getOutputs()[0].getFluidOutput();
                InnerVatRecipe res = new InnerVatRecipe(recipe.getEnergyRequired(), recipe.getInputs(), output);
                arecipes.add(res);
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack fluid = getFluidFromNeiItem(result);
        if (fluid != null) {
            loadCraftingRecipes(fluid);
        }
    }

    public void loadCraftingRecipes(FluidStack result) {
        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            FluidStack output = recipe.getOutputs()[0].getFluidOutput();
            if (output.isFluidEqual(result)) {
                InnerVatRecipe res = new InnerVatRecipe(recipe.getEnergyRequired(), recipe.getInputs(), output);
                arecipes.add(res);
            }
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (inputId.equals("liquid")) {
            loadUsageRecipes((FluidStack) ingredients[0]);
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        FluidStack fluid = getFluidFromNeiItem(ingredient);
        if (fluid != null) {
            loadUsageRecipes(fluid);
        }

        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            if (recipe.isValidInput(0, ingredient) || recipe.isValidInput(1, ingredient)) {
                FluidStack output = recipe.getOutputs()[0].getFluidOutput();
                InnerVatRecipe res = new InnerVatRecipe(recipe.getEnergyRequired(), recipe.getInputs(), output);
                res.setIngredientPermutation(res.inputs, ingredient);
                arecipes.add(res);
            }
        }
    }

    public void loadUsageRecipes(FluidStack ingredient) {
        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            if (recipe.isValidInput(ingredient)) {
                FluidStack output = recipe.getOutputs()[0].getFluidOutput();
                InnerVatRecipe res = new InnerVatRecipe(recipe.getEnergyRequired(), recipe.getInputs(), output);
                arecipes.add(res);
            }
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(22, 0, 27, 11, 123, 52);
    }

    @Override
    public void drawExtras(int recipeIndex) {
        InnerVatRecipe rec = (InnerVatRecipe) arecipes.get(recipeIndex);

        String energyString = PowerDisplayUtil.formatPower(rec.energy) + " " + PowerDisplayUtil.abrevation();
        GuiDraw.drawStringC(energyString, 86, 54, ColorUtils.neiEnergyString.getColor(), false);

        Fluid outputFluid = rec.resultFluid.getFluid();
        List<PositionedStack> stacks = rec.getIngredients();
        for (PositionedStack ps : stacks) {
            if (ps instanceof PositionedStack.Fluid) {
                continue;
            }
            float mult = VatRecipeManager.getInstance()
                    .getMultiplierForInput(rec.inFluid.getFluid(), ps.item, outputFluid);
            GuiDraw.drawStringC(
                    "x" + mult,
                    ps.relx + 8,
                    ps.rely + 19,
                    ColorUtils.neiMultiplierString.getColor(),
                    false);
        }

    }

    private static FluidStack getFluidFromNeiItem(ItemStack stack) {
        FluidStack fluid = StackInfo.getFluid(stack);
        return fluid != null ? fluid : FluidUtil.getFluidFromItem(stack);
    }

    private static PositionedStack.Fluid createTank(FluidStack fluid, int x, int y) {
        return new PositionedStack.Fluid(fluid, x, y, 15, 47, FluidContainerRegistry.BUCKET_VOLUME * 8);
    }

    private static FluidStack withAmount(FluidStack fluid, int amount) {
        FluidStack copy = fluid.copy();
        copy.amount = amount;
        return copy;
    }

    public List<ItemStack> getInputs(RecipeInput input) {
        List<ItemStack> result = new ArrayList<>();
        result.add(input.getInput());
        ItemStack[] eq = input.getEquivelentInputs();
        if (eq != null) {
            for (ItemStack st : eq) {
                result.add(st);
            }
        }
        return result;
    }

    public class InnerVatRecipe extends TemplateRecipeHandler.CachedRecipe {

        private final List<PositionedStack> inputs = new ArrayList<>();
        private final ItemStackMap<Float> firstItemMultiplier = new ItemStackMap<Float>();
        private final ItemStackMap<Float> secondItemMultiplier = new ItemStackMap<Float>();
        private final Map<FluidStack, Float> fluidMultiplier = new HashMap<>();
        private final FluidStack resultFluid;
        private final int energy;
        private FluidStack inFluid;

        private int inTankIndex = -1;
        private int lastInputAmount;
        private PositionedStack resultTank;
        private int lastResultAmount;

        public int getEnergy() {
            return energy;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (this.inTankIndex >= 0) {
                int amount = getInputFluidAmount();
                if (amount != this.lastInputAmount) {
                    this.lastInputAmount = amount;
                    this.inputs.set(this.inTankIndex, createTank(withAmount(this.inFluid, amount), 25, 1));
                }
            }
            return this.inputs;
        }

        public int getInputFluidAmount() {
            return (int) Math
                    .round(FluidContainerRegistry.BUCKET_VOLUME * getFirstItemMultiplier() * getSecondItemMultiplier());
        }

        public int getResultFluidAmount() {
            return (int) Math.round(
                    FluidContainerRegistry.BUCKET_VOLUME * getFirstItemMultiplier()
                            * getSecondItemMultiplier()
                            * getFluidMultiplier());
        }

        public float getFirstItemMultiplier() {
            return this.inputs.isEmpty() ? 1f : this.firstItemMultiplier.getOrDefault(this.inputs.get(0).item, 1f);
        }

        public float getSecondItemMultiplier() {
            return this.inputs.size() < 2 ? 1f : this.secondItemMultiplier.getOrDefault(this.inputs.get(1).item, 1f);
        }

        public float getFluidMultiplier() {
            return this.fluidMultiplier.getOrDefault(this.inFluid, 1f);
        }

        @Override
        public PositionedStack getResult() {
            int amount = getResultFluidAmount();
            if (amount != this.lastResultAmount || this.resultTank == null) {
                this.lastResultAmount = amount;
                this.resultTank = createTank(withAmount(this.resultFluid, amount), 127, 1);
            }
            return this.resultTank;
        }

        public InnerVatRecipe(int energy, RecipeInput[] ingredients, FluidStack result) {
            final List<ItemStack> inputsOne = new ArrayList<>();
            final List<ItemStack> inputsTwo = new ArrayList<>();

            for (RecipeInput input : ingredients) {
                float multi = input.getMulitplier();

                if (input.getInput() != null) {
                    final List<ItemStack> equivs = getInputs(input);

                    if (input.getSlotNumber() == 0) {
                        inputsOne.addAll(equivs);

                        for (ItemStack stack : equivs) {
                            this.firstItemMultiplier.put(stack, multi);
                        }
                    } else if (input.getSlotNumber() == 1) {
                        inputsTwo.addAll(equivs);

                        for (ItemStack stack : equivs) {
                            this.secondItemMultiplier.put(stack, multi);
                        }
                    }

                } else if (input.getFluidInput() != null) {
                    this.inFluid = input.getFluidInput();
                    this.fluidMultiplier.put(this.inFluid, multi);
                }

            }

            // Item stacks must stay at indices 0/1: getFirstItemMultiplier()/getSecondItemMultiplier() rely on it.
            if (!inputsOne.isEmpty()) {
                this.inputs.add(new PositionedStack(inputsOne, 51, 1));
            }
            if (!inputsTwo.isEmpty()) {
                this.inputs.add(new PositionedStack(inputsTwo, 100, 1));
            }
            this.energy = energy;
            this.resultFluid = result;

            if (this.inFluid != null) {
                this.inTankIndex = this.inputs.size();
                this.lastInputAmount = getInputFluidAmount();
                this.inputs.add(createTank(withAmount(this.inFluid, this.lastInputAmount), 25, 1));
            }

            this.lastResultAmount = getResultFluidAmount();
            this.resultTank = createTank(withAmount(this.resultFluid, this.lastResultAmount), 127, 1);
        }
    }
}
