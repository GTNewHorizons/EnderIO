package crazypants.enderio.machine.recipe;

import net.minecraft.item.ItemStack;

import com.ruling_0.materiallib.api.StackResolver;

/// Resolves the stack a `modID="ml"` config entry names. This is the only EnderIO class referencing MaterialLib
/// types, so callers gating on `Loader.isModLoaded("materiallib")` keep the soft dependency from classloading.
final class MaterialLibStacks {

    private MaterialLibStacks() {}

    /// The stack of the named material in the named shape, or null when either name matches nothing.
    static ItemStack getStack(String materialName, String shapeToken, int stackSize) {
        return StackResolver.getStack(materialName, shapeToken, stackSize);
    }
}
