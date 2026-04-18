package crazypants.enderio.machine.invpanel.server;

import java.util.ArrayList;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;

import cpw.mods.fml.common.Loader;
import crazypants.enderio.conduit.item.NetworkedInventory;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.tileentities.storage.MTEDigitalChestBase;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public abstract class InventoryFactory {

    private static final ArrayList<InventoryFactory> factories;

    static {
        factories = new ArrayList<InventoryFactory>();
        factories.add(new DSUFactory());
        if (Loader.isModLoaded("StorageDrawers")) {
            factories.add(new DrawerFactory());
        }
        if (Loader.isModLoaded("JABBA")) {
            factories.add(new JabbaFactory());
        }
        if (Loader.isModLoaded("gregtech")) {
            factories.add(new GregTechFactory());
        }
    }

    static AbstractInventory createInventory(NetworkedInventory ni) {
        for (InventoryFactory f : factories) {
            AbstractInventory ai = f.create(ni);
            if (ai != null) {
                return ai;
            }
        }
        return new NormalInventory(ni);
    }

    abstract AbstractInventory create(NetworkedInventory ni);

    static class DSUFactory extends InventoryFactory {

        @Override
        AbstractInventory create(NetworkedInventory ni) {
            ISidedInventory inv = ni.getInventory();
            if (inv instanceof IDeepStorageUnit dsu) {
                return new DSUInventory(dsu);
            }
            TileEntity te = ni.getConnectedTileEntity();
            if (te instanceof IDeepStorageUnit dsu) {
                return new DSUInventory(dsu);
            }
            return null;
        }
    }

    static class DrawerFactory extends InventoryFactory {

        @Override
        AbstractInventory create(NetworkedInventory ni) {
            ISidedInventory inv = ni.getInventory();
            if (inv instanceof IDrawerGroup dg) {
                return new DrawerGroupInventory(dg);
            }
            TileEntity te = ni.getConnectedTileEntity();
            if (te instanceof IDrawerGroup dg) {
                return new DrawerGroupInventory(dg);
            }
            return null;
        }
    }

    static class JabbaFactory extends InventoryFactory {

        @Override
        AbstractInventory create(NetworkedInventory ni) {
            ISidedInventory inv = ni.getInventory();
            if (inv instanceof TileEntityBarrel barrel) {
                return new JabbaBarrelInventory(barrel);
            }
            TileEntity te = ni.getConnectedTileEntity();
            if (te instanceof TileEntityBarrel barrel) {
                return new JabbaBarrelInventory(barrel);
            }
            return null;
        }
    }

    static class GregTechFactory extends InventoryFactory {

        @Override
        AbstractInventory create(NetworkedInventory ni) {
            ISidedInventory inv = ni.getInventory();
            if (inv instanceof IGregTechTileEntity gte) {
                if (gte.getMetaTileEntity() instanceof MTEDigitalChestBase chest) {
                    return new GregTechChestInventory(ni, chest);
                }
            }
            TileEntity te = ni.getConnectedTileEntity();
            if (te instanceof IGregTechTileEntity gte) {
                if (gte.getMetaTileEntity() instanceof MTEDigitalChestBase chest) {
                    return new GregTechChestInventory(ni, chest);
                }
            }
            return null;
        }
    }
}
