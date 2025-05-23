package crazypants.enderio.material;

import static crazypants.enderio.material.Alloy.DARK_STEEL;
import static crazypants.enderio.material.Alloy.END_STEEL;
import static crazypants.enderio.material.Alloy.ENERGETIC_ALLOY;
import static crazypants.enderio.material.Alloy.PHASED_GOLD;
import static crazypants.enderio.material.Alloy.PHASED_IRON;
import static crazypants.enderio.material.Alloy.SOULARIUM;
import static crazypants.enderio.material.Material.BINDER_COMPOSITE;
import static crazypants.enderio.material.Material.CONDUIT_BINDER;
import static crazypants.enderio.material.Material.PRECIENT_CRYSTAL;
import static crazypants.enderio.material.endergy.AlloyEndergy.CRYSTALLINE_ALLOY;
import static crazypants.enderio.material.endergy.AlloyEndergy.ENERGETIC_SILVER;
import static crazypants.enderio.material.endergy.AlloyEndergy.MELODIC_ALLOY;
import static crazypants.enderio.material.endergy.AlloyEndergy.STELLAR_ALLOY;
import static crazypants.enderio.material.endergy.AlloyEndergy.VIVID_ALLOY;
import static crazypants.enderio.power.Capacitors.ACTIVATED_CAPACITOR;
import static crazypants.enderio.power.Capacitors.BASIC_CAPACITOR;
import static crazypants.enderio.power.Capacitors.CRYSTALLINE_CAPACITOR;
import static crazypants.enderio.power.Capacitors.ENDERGETIC_CAPACITOR;
import static crazypants.enderio.power.Capacitors.ENDERGISED_CAPACITOR;
import static crazypants.enderio.power.Capacitors.ENDER_CAPACITOR;
import static crazypants.enderio.power.Capacitors.MELODIC_CAPACITOR;
import static crazypants.enderio.power.Capacitors.SILVER_CAPACITOR;
import static crazypants.enderio.power.Capacitors.STELLAR_CAPACITOR;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.enderio.core.common.util.OreDictionaryHelper;

import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.EnderIO;
import crazypants.enderio.config.Config;
import crazypants.enderio.item.ItemEnderFood.EnderFood;
import crazypants.enderio.material.endergy.AlloyEndergy;
import crazypants.enderio.power.Capacitors;
import crazypants.util.RecipeUtil;

public class MaterialRecipes {

    public static void registerDependantOresInDictionary() {
        // late registration for powders that only exist if the ingot from another
        // mod exists
        for (PowderIngot powder : PowderIngot.values()) {
            if (powder.hasDependancy() && powder.isDependancyMet()) {
                OreDictionary
                        .registerOre(powder.oreDictName, new ItemStack(EnderIO.itemPowderIngot, 1, powder.ordinal()));
                powder.setRegistered();
            }
        }
    }

    public static void registerOresInDictionary() {
        // Ore Dictionary Registration
        for (PowderIngot powder : PowderIngot.values()) {
            if (!powder.hasDependancy()) {
                OreDictionary
                        .registerOre(powder.oreDictName, new ItemStack(EnderIO.itemPowderIngot, 1, powder.ordinal()));
            }
        }

        for (Capacitors capacitor : Capacitors.values()) {
            OreDictionary.registerOre(
                    capacitor.getOreTag(),
                    new ItemStack(EnderIO.itemBasicCapacitor, 1, capacitor.ordinal()));
        }

        for (Alloy alloy : Alloy.values()) {
            boolean isPrimaryName = true;
            for (String oreDictName : alloy.getOreIngots()) {
                OreDictionary.registerOre(oreDictName, alloy.getStackIngot());
                if (isPrimaryName) {
                    isPrimaryName = false;
                } else {
                    // Allow free conversion of additional/legacy oreDict name items into
                    // our item because we only register recipes for the primary oreDict
                    // name. Use a 2-to-2 recipe because the 1-to-n is already in use.
                    RecipeUtil.addShapeless(alloy.getStackIngot(2), oreDictName, oreDictName);
                }
            }
            isPrimaryName = true;
            for (String oreDictName : alloy.getOreBlocks()) {
                OreDictionary.registerOre(oreDictName, alloy.getStackBlock());
                if (isPrimaryName) {
                    isPrimaryName = false;
                } else {
                    RecipeUtil.addShapeless(alloy.getStackBlock(2), oreDictName, oreDictName);
                }
            }
        }

        for (AlloyEndergy alloy : AlloyEndergy.values()) {
            boolean isPrimaryName = true;
            for (String oreDictName : alloy.getOreIngots()) {
                OreDictionary.registerOre(oreDictName, alloy.getStackIngot());
                if (isPrimaryName) {
                    isPrimaryName = false;
                } else {
                    // Allow free conversion of additional/legacy oreDict name items into
                    // our item because we only register recipes for the primary oreDict
                    // name. Use a 2-to-2 recipe because the 1-to-n is already in use.
                    RecipeUtil.addShapeless(alloy.getStackIngot(2), oreDictName, oreDictName);
                }
            }
            isPrimaryName = true;
            for (String oreDictName : alloy.getOreBlocks()) {
                OreDictionary.registerOre(oreDictName, alloy.getStackBlock());
                if (isPrimaryName) {
                    isPrimaryName = false;
                } else {
                    RecipeUtil.addShapeless(alloy.getStackBlock(2), oreDictName, oreDictName);
                }
            }
        }

        OreDictionary.registerOre(
                "nuggetPulsatingIron",
                new ItemStack(EnderIO.itemMaterial, 1, Material.PHASED_IRON_NUGGET.ordinal()));
        OreDictionary.registerOre(
                "nuggetVibrantAlloy",
                new ItemStack(EnderIO.itemMaterial, 1, Material.VIBRANT_NUGGET.ordinal()));
        OreDictionary.registerOre(
                "nuggetEndSteel",
                new ItemStack(EnderIO.itemMaterial, 1, Material.END_STEEL_NUGGET.ordinal()));

        OreDictionary.registerOre("glass", Blocks.glass);
        OreDictionary.registerOre("stickWood", Items.stick);
        OreDictionary.registerOre("woodStick", Items.stick);
        OreDictionary.registerOre("sand", new ItemStack(Blocks.sand, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("ingotIron", Items.iron_ingot);
        OreDictionary.registerOre("ingotGold", Items.gold_ingot);

        ItemStack pureGlass = new ItemStack(EnderIO.blockFusedQuartz, 1, BlockFusedQuartz.Type.GLASS.ordinal());
        OreDictionary.registerOre("glass", pureGlass);
        OreDictionary.registerOre("blockGlass", pureGlass);
        OreDictionary.registerOre(
                "blockGlassHardened",
                new ItemStack(EnderIO.blockFusedQuartz, 1, BlockFusedQuartz.Type.FUSED_QUARTZ.ordinal()));

        // Skulls
        ItemStack skull = new ItemStack(Items.skull, 1, OreDictionary.WILDCARD_VALUE);
        OreDictionary.registerOre("itemSkull", skull);
        OreDictionary.registerOre("itemSkull", new ItemStack(EnderIO.blockEndermanSkull));

        // Glass stuff for compatibility
        RecipeUtil.addShaped(new ItemStack(Blocks.glass_pane, 16, 0), "eee", "eee", 'e', pureGlass);
        RecipeUtil.addShapeless(new ItemStack(Blocks.glass), pureGlass);
        RecipeUtil.addShaped(new ItemStack(Items.glass_bottle, 3, 0), "g g", " g ", 'g', pureGlass);

        Material.registerOres(EnderIO.itemMaterial);
        MachinePart.registerOres(EnderIO.itemMachinePart);
    }

    public static void addRecipes() {

        // Common Ingredients
        String conduitBinder = CONDUIT_BINDER.oreDict;

        ItemStack fusedQuartzFrame = new ItemStack(EnderIO.itemFusedQuartzFrame, 1, 0);

        String energeticAlloy = ENERGETIC_ALLOY.getOreIngot();
        String phasedGold = PHASED_GOLD.getOreIngot();
        String phasedIron = PHASED_IRON.getOreIngot();
        String darkSteel = DARK_STEEL.getOreIngot();
        String soularium = SOULARIUM.getOreIngot();
        String endSteel = END_STEEL.getOreIngot();

        String energeticSilver = ENERGETIC_SILVER.getOreIngot();
        String vividAlloy = VIVID_ALLOY.getOreIngot();
        String crystallineAlloy = CRYSTALLINE_ALLOY.getOreIngot();
        String melodicAlloy = MELODIC_ALLOY.getOreIngot();
        String stellarAlloy = STELLAR_ALLOY.getOreIngot();

        String capacitor = Capacitors.BASIC_CAPACITOR.getOreTag();
        String capacitorEnder = Capacitors.ENDER_CAPACITOR.getOreTag();

        String ingotCopper = OreDictionary.doesOreNameExist("ingotCopper") && Config.useModMetals ? "ingotCopper"
                : "ingotIron";

        // Capacitors
        if (Config.useHardRecipes) {
            RecipeUtil.addShaped(
                    BASIC_CAPACITOR.getItemStack(),
                    " gr",
                    "gig",
                    "rg ",
                    'r',
                    "dustRedstone",
                    'g',
                    "ingotGold",
                    'i',
                    ingotCopper);
            RecipeUtil.addShaped(
                    ACTIVATED_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    energeticAlloy,
                    'c',
                    BASIC_CAPACITOR.getItemStack(),
                    'C',
                    "dustCoal");
            RecipeUtil.addShaped(
                    ENDER_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    phasedGold,
                    'c',
                    ACTIVATED_CAPACITOR.getItemStack(),
                    'C',
                    "glowstone");

            String prismarine = OreDictionary.doesOreNameExist("shardPrismarine") ? "shardPrismarine"
                    : "itemPulsatingCrystal";
            RecipeUtil.addShaped(
                    CRYSTALLINE_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    crystallineAlloy,
                    'c',
                    capacitorEnder,
                    'C',
                    prismarine);
            RecipeUtil.addShaped(
                    MELODIC_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    melodicAlloy,
                    'c',
                    CRYSTALLINE_CAPACITOR.getItemStack(),
                    'C',
                    endSteel);
            RecipeUtil.addShaped(
                    STELLAR_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    stellarAlloy,
                    'c',
                    MELODIC_CAPACITOR.getItemStack(),
                    'C',
                    PRECIENT_CRYSTAL.oreDict);

            RecipeUtil.addShaped(
                    SILVER_CAPACITOR.getItemStack(),
                    " sr",
                    "sis",
                    "rs ",
                    'r',
                    "dustRedstone",
                    's',
                    "ingotSilver",
                    'i',
                    "ingotLead");
            RecipeUtil.addShaped(
                    ENDERGETIC_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    energeticSilver,
                    'c',
                    SILVER_CAPACITOR.getItemStack(),
                    'C',
                    "dustCoal");
            RecipeUtil.addShaped(
                    ENDERGISED_CAPACITOR.getItemStack(),
                    "iii",
                    "cCc",
                    "iii",
                    'i',
                    vividAlloy,
                    'c',
                    ENDERGETIC_CAPACITOR.getItemStack(),
                    'C',
                    "glowstone");

        } else {
            RecipeUtil.addShaped(
                    BASIC_CAPACITOR.getItemStack(),
                    " gr",
                    "gig",
                    "rg ",
                    'r',
                    "dustRedstone",
                    'g',
                    "ingotGold",
                    'i',
                    ingotCopper);
            RecipeUtil.addShaped(
                    ACTIVATED_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    energeticAlloy,
                    'c',
                    BASIC_CAPACITOR.getItemStack(),
                    'C',
                    "dustCoal");
            RecipeUtil.addShaped(
                    ENDER_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    phasedGold,
                    'c',
                    ACTIVATED_CAPACITOR.getItemStack(),
                    'C',
                    "glowstone");

            String prismarine = OreDictionary.doesOreNameExist("shardPrismarine") ? "shardPrismarine"
                    : "itemPulsatingCrystal";
            RecipeUtil.addShaped(
                    CRYSTALLINE_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    crystallineAlloy,
                    'c',
                    capacitorEnder,
                    'C',
                    prismarine);
            RecipeUtil.addShaped(
                    MELODIC_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    melodicAlloy,
                    'c',
                    CRYSTALLINE_CAPACITOR.getItemStack(),
                    'C',
                    endSteel);
            RecipeUtil.addShaped(
                    STELLAR_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    stellarAlloy,
                    'c',
                    MELODIC_CAPACITOR.getItemStack(),
                    'C',
                    PRECIENT_CRYSTAL.oreDict);

            RecipeUtil.addShaped(
                    SILVER_CAPACITOR.getItemStack(),
                    " sr",
                    "sis",
                    "rs ",
                    'r',
                    "dustRedstone",
                    's',
                    "ingotSilver",
                    'i',
                    "ingotLead");
            RecipeUtil.addShaped(
                    ENDERGETIC_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    energeticSilver,
                    'c',
                    SILVER_CAPACITOR.getItemStack(),
                    'C',
                    "dustCoal");
            RecipeUtil.addShaped(
                    ENDERGISED_CAPACITOR.getItemStack(),
                    " i ",
                    "cCc",
                    " i ",
                    'i',
                    vividAlloy,
                    'c',
                    ENDERGETIC_CAPACITOR.getItemStack(),
                    'C',
                    "glowstone");
        }

        // Conduit Binder
        ItemStack cbc = BINDER_COMPOSITE.getStack(8);
        if (Config.useAlternateBinderRecipe) {
            RecipeUtil.addShaped(cbc, "gcg", "sgs", "gcg", 'g', Blocks.gravel, 's', "sand", 'c', Items.clay_ball);
        } else {
            RecipeUtil.addShaped(cbc, "ggg", "scs", "ggg", 'g', Blocks.gravel, 's', "sand", 'c', Items.clay_ball);
        }
        GameRegistry.addSmelting(BINDER_COMPOSITE.getStack(), CONDUIT_BINDER.getStack(4), 0);

        // Nuggets
        ItemStack phasedIronNugget = new ItemStack(EnderIO.itemMaterial, 9, Material.PHASED_IRON_NUGGET.ordinal());
        RecipeUtil.addShapeless(phasedIronNugget, phasedIron);
        phasedIronNugget = phasedIronNugget.copy();
        phasedIronNugget.stackSize = 1;
        RecipeUtil.addShaped(PHASED_IRON.getStackIngot(), "eee", "eee", "eee", 'e', phasedIronNugget);

        ItemStack vibrantNugget = new ItemStack(EnderIO.itemMaterial, 9, Material.VIBRANT_NUGGET.ordinal());
        RecipeUtil.addShapeless(vibrantNugget, phasedGold);
        vibrantNugget = vibrantNugget.copy();
        vibrantNugget.stackSize = 1;
        RecipeUtil.addShaped(PHASED_GOLD.getStackIngot(), "eee", "eee", "eee", 'e', vibrantNugget);

        ItemStack endSteelNugget = new ItemStack(EnderIO.itemMaterial, 9, Material.END_STEEL_NUGGET.ordinal());
        RecipeUtil.addShapeless(endSteelNugget, endSteel);
        endSteelNugget = endSteelNugget.copy();
        endSteelNugget.stackSize = 1;
        RecipeUtil.addShaped(END_STEEL.getStackIngot(), "eee", "eee", "eee", 'e', endSteelNugget);

        // Crystals
        ItemStack pulsCry = new ItemStack(EnderIO.itemMaterial, 1, Material.PULSATING_CYSTAL.ordinal());
        RecipeUtil.addShaped(pulsCry, "nnn", "ngn", "nnn", 'n', phasedIronNugget, 'g', "gemDiamond");

        ItemStack vibCry = new ItemStack(EnderIO.itemMaterial, 1, Material.VIBRANT_CYSTAL.ordinal());
        RecipeUtil.addShaped(vibCry, "nnn", "ngn", "nnn", 'n', vibrantNugget, 'g', "gemEmerald");

        // Smelting
        ItemStack dustIron = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.POWDER_IRON.ordinal());
        ItemStack dustGold = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.POWDER_GOLD.ordinal());
        ItemStack ingotIron = new ItemStack(Items.iron_ingot);
        ItemStack ingotGold = new ItemStack(Items.gold_ingot);

        GameRegistry.addSmelting(dustIron, ingotIron, 0);
        GameRegistry.addSmelting(dustGold, ingotGold, 0);

        // Ender Dusts
        ItemStack enderDust = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.POWDER_ENDER.ordinal());
        RecipeUtil.addShaped(new ItemStack(Items.ender_pearl), "eee", "eee", "eee", 'e', enderDust);

        // Dark Iron Bars
        ItemStack diBars = new ItemStack(EnderIO.blockDarkIronBars, 16, 0);
        RecipeUtil.addShaped(diBars, "ddd", "ddd", 'd', darkSteel);

        // Dark Iron Bars
        ItemStack soBars = new ItemStack(EnderIO.blockSoulariumBars, 16, 0);
        RecipeUtil.addShaped(soBars, "ddd", "ddd", 'd', soularium);

        // Dark Iron Bars
        ItemStack esBars = new ItemStack(EnderIO.blockEndSteelBars, 16, 0);
        RecipeUtil.addShaped(esBars, "ddd", "ddd", 'd', endSteel);

        // Fused Quartz Frame
        RecipeUtil.addShaped(fusedQuartzFrame, "bsb", "s s", "bsb", 'b', conduitBinder, 's', "stickWood");
        RecipeUtil.addShaped(fusedQuartzFrame, "bsb", "s s", "bsb", 'b', conduitBinder, 's', "woodStick");

        // Machine Chassis

        ArrayList<ItemStack> steelIngots = OreDictionary.getOres("ingotSteel");

        ItemStack chassis = new ItemStack(EnderIO.itemMachinePart, 1, MachinePart.MACHINE_CHASSI.ordinal());
        String mat = Config.useSteelInChassi == true && steelIngots != null && !steelIngots.isEmpty() ? "ingotSteel"
                : "ingotIron";
        RecipeUtil.addShaped(chassis, "fif", "ici", "fif", 'f', Blocks.iron_bars, 'i', mat, 'c', capacitor);

        ItemStack soulchassis = new ItemStack(EnderIO.itemMachinePart, 1, MachinePart.SOUL_MACHINE_CHASSIS.ordinal());
        RecipeUtil.addShaped(
                soulchassis,
                "fif",
                "ici",
                "fif",
                'f',
                EnderIO.blockSoulariumBars,
                'i',
                Alloy.SOULARIUM.getOreIngot(),
                'c',
                capacitor);

        ItemStack endchassis = new ItemStack(
                EnderIO.itemMachinePart,
                1,
                MachinePart.END_STEEL_MACHINE_CHASSIS.ordinal());
        RecipeUtil.addShaped(
                endchassis,
                "fif",
                "ici",
                "fif",
                'f',
                EnderIO.blockEndSteelBars,
                'i',
                Alloy.END_STEEL.getOreIngot(),
                'c',
                capacitor);

        // Basic Gear
        ItemStack gear = new ItemStack(EnderIO.itemMachinePart, 1, MachinePart.BASIC_GEAR.ordinal());
        RecipeUtil.addShaped(gear, "scs", "c c", "scs", 's', "stickWood", 'c', "cobblestone");
        RecipeUtil.addShaped(gear, "scs", "c c", "scs", 's', "woodStick", 'c', "cobblestone");

        // Weather Crystal
        ItemStack main = Config.useHardRecipes
                ? new ItemStack(EnderIO.itemMaterial, 1, Material.VIBRANT_CYSTAL.ordinal())
                : new ItemStack(Items.diamond);
        GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                        new ItemStack(EnderIO.itemMaterial, 1, Material.WEATHER_CRYSTAL.ordinal()),
                        main /* TODO figure out new weather crystal recipe */));

        if (Config.reinforcedObsidianEnabled) {
            ItemStack reinfObs = new ItemStack(EnderIO.blockReinforcedObsidian);
            String corners = darkSteel;
            if (Config.reinforcedObsidianUseDarkSteelBlocks) {
                corners = Alloy.DARK_STEEL.getOreBlock();
            }
            RecipeUtil.addShaped(
                    reinfObs,
                    "dbd",
                    "bob",
                    "dbd",
                    'd',
                    corners,
                    'b',
                    EnderIO.blockDarkIronBars,
                    'o',
                    Blocks.obsidian);
        }

        RecipeUtil.addShaped(
                EnderIO.blockDarkSteelAnvil,
                "bbb",
                " i ",
                "iii",
                'b',
                DARK_STEEL.getOreBlock(),
                'i',
                darkSteel);

        RecipeUtil.addShaped(
                new ItemStack(EnderIO.blockDarkSteelLadder, 12),
                "b",
                "b",
                "b",
                'b',
                EnderIO.blockDarkIronBars);

        for (Alloy alloy : Alloy.values()) {
            RecipeUtil.addShaped(alloy.getStackBlock(), "iii", "iii", "iii", 'i', alloy.getOreIngot());
            RecipeUtil.addShaped(alloy.getStackBall(5), " i ", "iii", " i ", 'i', alloy.getOreIngot());
            RecipeUtil.addShapeless(alloy.getStackIngot(9), alloy.getOreBlock());
        }

        for (AlloyEndergy alloy : AlloyEndergy.values()) {
            RecipeUtil.addShaped(alloy.getStackBlock(), "iii", "iii", "iii", 'i', alloy.getOreIngot());
            RecipeUtil.addShaped(alloy.getStackBall(5), " i ", "iii", " i ", 'i', alloy.getOreIngot());
            RecipeUtil.addShapeless(alloy.getStackIngot(9), alloy.getOreBlock());
        }

        // DS Rod
        ItemStack darkRod = new ItemStack(EnderIO.itemMaterial, 1, Material.DARK_STEEL_ROD.ordinal());
        RecipeUtil.addShaped(darkRod, " ns", "nsn", "sn ", 's', darkSteel, 'n', endSteelNugget);

        // Food
        ItemStack flour = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.FLOUR.ordinal());
        ItemStack bread = new ItemStack(Items.bread, 1, 0);

        GameRegistry.addSmelting(flour, bread, 0.35f);

        ItemStack enderios = EnderFood.ENDERIOS.getStack();
        RecipeUtil.addShapeless(enderios, Items.bowl, Items.milk_bucket, "cropWheat", "dustEnderPearl");

        if (OreDictionaryHelper.hasCopper()) {
            ItemStack dustCopper = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.POWDER_COPPER.ordinal());
            ItemStack ingotCoppper = OreDictionaryPreferences.instance.getPreferred(OreDictionaryHelper.INGOT_COPPER);
            GameRegistry.addSmelting(dustCopper, ingotCoppper, 0);
        }
        if (OreDictionaryHelper.hasTin()) {
            ItemStack dustTin = new ItemStack(EnderIO.itemPowderIngot, 1, PowderIngot.POWDER_TIN.ordinal());
            ItemStack ingotTin = OreDictionaryPreferences.instance.getPreferred(OreDictionaryHelper.INGOT_TIN);
            GameRegistry.addSmelting(dustTin, ingotTin, 0);
        }
    }
}
