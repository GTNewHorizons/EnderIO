package crazypants.enderio.machine.recipe;

import net.minecraft.item.ItemStack;

import com.ruling_0.materiallib.api.StackResolver;

/// Resolves the stack a `modID="ml"` config entry names. The only EnderIO class referencing MaterialLib types, so
/// the soft dependency classloads only behind a `Loader.isModLoaded` check.
final class MaterialLibStacks {

    private MaterialLibStacks() {}

    /// The stack of the named material in the named shape, or null when the pair resolves to nothing.
    static ItemStack getStack(String materialName, String shapeToken, int stackSize) {
        return StackResolver.getStack(materialName, shapeToken, stackSize);
    }
}
