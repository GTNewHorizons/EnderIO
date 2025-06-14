package crazypants.enderio.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.gas.GasUtil;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.alloy.GuiAlloySmelter;
import crazypants.enderio.machine.crusher.GuiCrusher;
import crazypants.enderio.machine.invpanel.GuiInventoryPanel;
import crazypants.enderio.machine.invpanel.client.InventoryPanelNEIOverlayHandler;
import crazypants.enderio.machine.painter.PainterUtil;
import crazypants.enderio.machine.slicensplice.GuiSliceAndSplice;
import crazypants.enderio.machine.soul.GuiSoulBinder;

public class NEIEnderIOConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerRecipeHandler(new AlloySmelterRecipeHandler());
        API.registerUsageHandler(new AlloySmelterRecipeHandler());
        API.setGuiOffset(GuiAlloySmelter.class, 4, 6);

        API.registerRecipeHandler(new SagMillRecipeHandler());
        API.registerUsageHandler(new SagMillRecipeHandler());
        API.setGuiOffset(GuiCrusher.class, 6, 11);

        API.registerRecipeHandler(new VatRecipeHandler());
        API.registerUsageHandler(new VatRecipeHandler());

        API.registerRecipeHandler(new EnchanterRecipeHandler());
        API.registerUsageHandler(new EnchanterRecipeHandler());

        API.registerRecipeHandler(new SliceAndSpliceRecipeHandler());
        API.registerUsageHandler(new SliceAndSpliceRecipeHandler());
        API.setGuiOffset(GuiSliceAndSplice.class, 13, 11);

        API.registerRecipeHandler(new SoulBinderRecipeHandler());
        API.registerUsageHandler(new SoulBinderRecipeHandler());
        API.setGuiOffset(GuiSoulBinder.class, 11, 11);

        API.registerGuiOverlayHandler(GuiInventoryPanel.class, new InventoryPanelNEIOverlayHandler(), "crafting");

        API.hideItem(new ItemStack(EnderIO.blockConduitFacade));
        API.hideItem(new ItemStack(EnderIO.blockLightNode));
        API.hideItem(new ItemStack(EnderIO.itemEnderface));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedCarpet)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedSlab)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedFence)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedFenceGate)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedGlowstone)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedStair)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedWall)));
        API.hideItem(PainterUtil.applyDefaultPaintedState(new ItemStack(EnderIO.blockPaintedDoubleSlab)));

        if (!Config.photovoltaicCellEnabled) {
            API.hideItem(new ItemStack(EnderIO.blockSolarPanel));
        }
        if (!Config.travelAnchorEnabled) {
            API.hideItem(new ItemStack(EnderIO.itemTravelStaff));
            API.hideItem(new ItemStack(EnderIO.itemTeleportStaff));
        }
        if (!Config.reinforcedObsidianEnabled) {
            API.hideItem(new ItemStack(EnderIO.blockReinforcedObsidian));
        }
        if ((!Config.transceiverEnabled || !Config.enderRailEnabled) && EnderIO.blockEnderRail != null) {
            API.hideItem(new ItemStack(EnderIO.blockEnderRail));
        }
        if (!Config.transceiverEnabled && EnderIO.blockTransceiver != null) {
            API.hideItem(new ItemStack(EnderIO.blockTransceiver));
        }
        if (!Config.reservoirEnabled) {
            API.hideItem(new ItemStack(EnderIO.blockReservoir));
        }
        if (!GasUtil.isGasConduitEnabled()) {
            API.hideItem(new ItemStack(EnderIO.itemGasConduit));
        }
        API.hideItem(new ItemStack(EnderIO.blockHyperCube));
    }

    @Override
    public String getName() {
        return "Ender IO NEI Plugin";
    }

    @Override
    public String getVersion() {
        return EnderIO.VERSION;
    }
}
