package crazypants.enderio.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.FluidUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import crazypants.enderio.EnderIO;
import crazypants.enderio.gui.GuiContainerBaseEIO;
import crazypants.enderio.machine.recipe.IRecipe;
import crazypants.enderio.machine.recipe.RecipeInput;
import crazypants.enderio.machine.vat.GuiVat;
import crazypants.enderio.machine.vat.VatRecipeManager;
import crazypants.enderio.power.PowerDisplayUtil;
import crazypants.util.ColorUtils;
import gregtech.api.util.GTUtility;

public class VatRecipeHandler extends TemplateRecipeHandler {

    private final Rectangle inTankBounds = new Rectangle(25, 1, 15, 47);
    private final Rectangle outTankBounds = new Rectangle(127, 1, 15, 47);

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
                arecipes.addAll(of(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack fluid = FluidUtil.getFluidFromItem(result);
        if (fluid != null) {
            loadCraftingRecipes(fluid);
        }
    }

    public void loadCraftingRecipes(FluidStack result) {
        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            FluidStack output = recipe.getOutputs()[0].getFluidOutput();
            if (output.isFluidEqual(result)) {
                arecipes.addAll(of(recipe));
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
        FluidStack fluid = FluidUtil.getFluidFromItem(ingredient);
        if (fluid != null) {
            loadUsageRecipes(fluid);
        }

        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            if (recipe.isValidInput(0, ingredient) || recipe.isValidInput(1, ingredient)) {
                for (InnerVatRecipe rec : of(recipe)) {
                    if (rec.contains(rec.inputs, ingredient)) {
                        rec.setIngredientPermutation(rec.inputs, ingredient);
                        arecipes.add(rec);
                    }
                }
            }
        }
    }

    public void loadUsageRecipes(FluidStack ingredient) {
        List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
        for (IRecipe recipe : recipes) {
            if (recipe.isValidInput(ingredient)) {
                arecipes.addAll(of(recipe));
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
        if (rec.inFluid != null && rec.inFluid.getFluid() != null) {
            RenderUtil.renderGuiTank(
                    rec.inFluid,
                    FluidContainerRegistry.BUCKET_VOLUME * 8,
                    rec.getInputFluidAmount(),
                    inTankBounds.x,
                    inTankBounds.y,
                    0,
                    inTankBounds.width,
                    inTankBounds.height);
        }

        if (rec.result != null && rec.result.getFluid() != null) {
            RenderUtil.renderGuiTank(
                    rec.result,
                    FluidContainerRegistry.BUCKET_VOLUME * 8,
                    rec.getResultFluidAmount(),
                    outTankBounds.x,
                    outTankBounds.y,
                    0,
                    outTankBounds.width,
                    outTankBounds.height);
        }

        String energyString = PowerDisplayUtil.formatPower(rec.energy) + " " + PowerDisplayUtil.abrevation();
        GuiDraw.drawStringC(energyString, 86, 54, ColorUtils.neiEnergyString.getColor(), false);

        Fluid outputFluid = rec.result.getFluid();
        for (PositionedStack ps : rec.getItemInputs()) {
            float mult = VatRecipeManager.getInstance()
                    .getMultiplierForInput(rec.inFluid.getFluid(), ps.item, outputFluid);
            String str = "x" + mult;
            GuiDraw.drawStringC(str, ps.relx + 8, ps.rely + 19, ColorUtils.neiMultiplierString.getColor(), false);
        }

    }

    @Override
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipeIndex) {
        if (button == 0) {
            if (this.transferFluidTanks(gui, recipeIndex, false)) {
                return true;
            }
        } else if (button == 1) {
            if (this.transferFluidTanks(gui, recipeIndex, true)) {
                return true;
            }
        }
        return super.mouseClicked(gui, button, recipeIndex);
    }

    private boolean transferFluidTanks(GuiRecipe<?> gui, int recipeIndex, boolean usage) {
        InnerVatRecipe rec = (InnerVatRecipe) arecipes.get(recipeIndex);
        Point pos = GuiDraw.getMousePosition();
        Point offset = gui.getRecipePosition(recipeIndex);
        Point relMouse = new Point(pos.x - gui.guiLeft - offset.x, pos.y - gui.guiTop - offset.y);

        if (inTankBounds.contains(relMouse)) {
            transferFluidTank(rec.inFluid, usage);
        } else if (outTankBounds.contains(relMouse)) {
            transferFluidTank(rec.result, usage);
        }
        return false;
    }

    private boolean transferFluidTank(FluidStack tank, boolean usage) {
        if (tank != null && tank.amount > 0) {
            if (usage) {
                if (!GuiUsageRecipe.openRecipeGui("liquid", new Object[] { tank.copy() })) {
                    return false;
                }
            } else {
                if (!GuiCraftingRecipe.openRecipeGui("liquid", new Object[] { tank.copy() })) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static List<ItemStack> getInputs(RecipeInput input) {
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

    private static final class GroupedRecipe {

        final float multOne;
        final float multTwo;
        final List<ItemStack> one = new ArrayList<>();
        final List<ItemStack> two = new ArrayList<>();

        GroupedRecipe(float multOne, float multTwo) {
            this.multOne = multOne;
            this.multTwo = multTwo;
        }
    }

    private List<InnerVatRecipe> of(IRecipe recipe) {
        final FluidStack output = recipe.getOutputs()[0].getFluidOutput();
        final int energy = recipe.getEnergyRequired();

        final List<List<ItemStack>> optionsOne = new ArrayList<>();
        final List<Float> multsOne = new ArrayList<>();
        final List<List<ItemStack>> optionsTwo = new ArrayList<>();
        final List<Float> multsTwo = new ArrayList<>();

        FluidStack inFluid = null;
        float fluidMult = 1f;

        for (RecipeInput input : recipe.getInputs()) {
            if (input.isFluid()) {
                inFluid = input.getFluidInput();
                fluidMult = input.getMulitplier();
            } else {
                final List<ItemStack> equivs = getInputs(input);

                if (input.getSlotNumber() == 0) {
                    optionsOne.add(equivs);
                    multsOne.add(input.getMulitplier());
                } else {
                    optionsTwo.add(equivs);
                    multsTwo.add(input.getMulitplier());
                }
            }
        }

        if (optionsOne.isEmpty()) {
            optionsOne.add(Collections.emptyList());
            multsOne.add(1f);
        }
        if (optionsTwo.isEmpty()) {
            optionsTwo.add(Collections.emptyList());
            multsTwo.add(1f);
        }

        final LinkedHashMap<String, GroupedRecipe> groups = new LinkedHashMap<>();

        for (int i = 0; i < optionsOne.size(); i++) {
            final float multOne = multsOne.get(i);

            for (int j = 0; j < optionsTwo.size(); j++) {
                final float multTwo = multsTwo.get(j);
                final GroupedRecipe group = groups
                        .computeIfAbsent(multOne + ":" + multTwo, k -> new GroupedRecipe(multOne, multTwo));

                group.one.addAll(optionsOne.get(i));
                group.two.addAll(optionsTwo.get(j));
            }
        }

        final List<InnerVatRecipe> recipes = new ArrayList<>(groups.size());

        for (GroupedRecipe group : groups.values()) {
            recipes.add(
                    new InnerVatRecipe(
                            energy,
                            inFluid,
                            output,
                            group.one,
                            group.multOne,
                            group.two,
                            group.multTwo,
                            fluidMult));
        }

        return recipes;
    }

    private static ItemStack getItemStackFromFluid(Fluid fluid, int amount) {
        ItemStack stack = null;

        if (EnderIO.hasGT5) {
            stack = GTUtility.getFluidDisplayStack(new FluidStack(fluid, amount), false);
        }

        if (stack == null && fluid.getBlock() != null) {
            stack = new ItemStack(fluid.getBlock(), amount);
        }

        return stack;
    }

    public class InnerVatRecipe extends TemplateRecipeHandler.CachedRecipe {

        private final List<PositionedStack> inputs = new ArrayList<>();
        private final List<PositionedStack> ingredients = new ArrayList<>();
        private final int energy;
        private final FluidStack result;
        private final FluidStack inFluid;
        private final int inputAmount;
        private final int outputAmount;
        private PositionedStack inFluidStack;
        private PositionedStack resultStack;

        public int getEnergy() {
            return energy;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return this.ingredients;
        }

        public List<PositionedStack> getItemInputs() {
            return this.inputs;
        }

        public int getInputFluidAmount() {
            return this.inputAmount;
        }

        public int getResultFluidAmount() {
            return this.outputAmount;
        }

        @Override
        public PositionedStack getResult() {
            return this.resultStack;
        }

        public InnerVatRecipe(int energy, FluidStack inFluid, FluidStack result, List<ItemStack> optionsOne,
                float multOne, List<ItemStack> optionsTwo, float multTwo, float fluidMult) {
            this.energy = energy;
            this.inFluid = inFluid;
            this.result = result;
            this.inputAmount = Math.round(FluidContainerRegistry.BUCKET_VOLUME * multOne * multTwo);
            this.outputAmount = Math.round(this.inputAmount * fluidMult);

            if (!optionsOne.isEmpty()) {
                this.inputs.add(new PositionedStack(optionsOne, 51, 1));
            }
            if (!optionsTwo.isEmpty()) {
                this.inputs.add(new PositionedStack(optionsTwo, 100, 1));
            }

            if (this.inFluid != null && this.inFluid.getFluid() != null) {
                final ItemStack inStack = getItemStackFromFluid(this.inFluid.getFluid(), this.inputAmount);
                if (inStack != null) {
                    this.inFluidStack = new PositionedStack.Placeholder(inStack, inTankBounds.x, inTankBounds.y, false);
                    this.inFluidStack.width = inTankBounds.width - 1;
                    this.inFluidStack.height = inTankBounds.height - 1;
                }
            }
            if (this.result != null && this.result.getFluid() != null) {
                final ItemStack outStack = getItemStackFromFluid(this.result.getFluid(), this.outputAmount);
                if (outStack != null) {
                    this.resultStack = new PositionedStack.Placeholder(
                            outStack,
                            outTankBounds.x,
                            outTankBounds.y,
                            false);
                    this.resultStack.width = outTankBounds.width - 1;
                    this.resultStack.height = outTankBounds.height - 1;
                }
            }

            this.ingredients.addAll(this.inputs);
            if (this.inFluidStack != null) {
                this.ingredients.add(this.inFluidStack);
            }
        }
    }
}
