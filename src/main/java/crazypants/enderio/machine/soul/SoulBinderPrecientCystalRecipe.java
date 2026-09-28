package crazypants.enderio.machine.soul;

import net.minecraft.item.ItemStack;

import crazypants.enderio.EnderIO;
import crazypants.enderio.config.Config;
import crazypants.enderio.material.Material;

public class SoulBinderPrecientCystalRecipe extends AbstractSoulBinderRecipe {

    public static SoulBinderPrecientCystalRecipe instance = new SoulBinderPrecientCystalRecipe();

    private SoulBinderPrecientCystalRecipe() {
        super(
                Config.soulBinderPrecientCystalRF,
                Config.soulBinderPrecientCystalLevels,
                "SoulBinderPrecientCystalRecipe",
                "Enderman",
                "SpecialMobs.BlindingEnderman",
                "SpecialMobs.CursedEnderman",
                "SpecialMobs.IcyEnderman",
                "SpecialMobs.LightningEnderman",
                "SpecialMobs.MiniEnderman",
                "SpecialMobs.MirageEnderman",
                "SpecialMobs.ThiefEnderman");
    }

    @Override
    public ItemStack getInputStack() {
        return new ItemStack(EnderIO.itemMaterial, 1, Material.PULSATING_CYSTAL.ordinal());
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack(EnderIO.itemMaterial, 1, Material.PRECIENT_CRYSTAL.ordinal());
    }
}
