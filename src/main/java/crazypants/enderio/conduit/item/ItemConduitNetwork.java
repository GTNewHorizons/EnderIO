package crazypants.enderio.conduit.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.BlockCoord;

import crazypants.enderio.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduit.item.NetworkedInventory.Target;
import crazypants.enderio.conduit.item.filter.IItemFilter;
import crazypants.enderio.machine.invpanel.server.InventoryDatabaseServer;

public class ItemConduitNetwork extends AbstractConduitNetwork<IItemConduit, IItemConduit> {

    final List<NetworkedInventory> inventories = new ArrayList<>();
    private final Map<BlockCoord, List<NetworkedInventory>> invMap = new HashMap<>();

    final Map<BlockCoord, IItemConduit> conMap = new HashMap<>();

    private boolean requiresSort = true;

    private int sortCursor = 0; // persistent amortization cursor into inventories
    private boolean sortDirty = false; // a trigger arrived mid-pass; do one more full clean pass
    private int sortEpoch = 1; // stamped onto each NetworkedInventory sorted this pass; gates the free skip below
    private final Deque<NetworkedInventory> pendingFirstSort = new ArrayDeque<>(); // never-sorted entries, always
                                                                                   // drained before the cursor sweep

    private int topologyVersion = 0; // bumped unconditionally by every addConduit() call; gates distanceCache
                                     // validity

    private final Map<BlockCoord, CachedDistances> distanceCache = new HashMap<>();

    private boolean doingSend = false;

    private int changeCount;

    private InventoryDatabaseServer database;

    public ItemConduitNetwork() {
        super(IItemConduit.class, IItemConduit.class);
    }

    @Override
    public void addConduit(IItemConduit con) {
        super.addConduit(con);
        topologyVersion++;
        conMap.put(con.getLocation(), con);

        TileEntity te = con.getBundle().getEntity();
        if (te != null) {
            for (ForgeDirection direction : con.getExternalConnections()) {
                IInventory extCon = con.getExternalInventory(direction);
                if (extCon != null) {
                    inventoryAdded(
                            con,
                            direction,
                            te.xCoord + direction.offsetX,
                            te.yCoord + direction.offsetY,
                            te.zCoord + direction.offsetZ,
                            extCon);
                }
            }
        }
    }

    public void inventoryAdded(IItemConduit itemConduit, ForgeDirection direction, int x, int y, int z,
            IInventory externalInventory) {
        BlockCoord bc = new BlockCoord(x, y, z);
        NetworkedInventory inv = new NetworkedInventory(this, externalInventory, itemConduit, direction, bc);
        inventories.add(inv);
        getOrCreate(bc).add(inv);
        pendingFirstSort.add(inv);
        markSortPending();
        changeCount++;
    }

    public NetworkedInventory getInventory(IItemConduit conduit, ForgeDirection dir) {
        for (NetworkedInventory inv : inventories) {
            if (inv.con == conduit && inv.conDir == dir) {
                return inv;
            }
        }
        return null;
    }

    public List<NetworkedInventory> getInventoryPanelSources() {
        ArrayList<NetworkedInventory> res = new ArrayList<>();
        for (NetworkedInventory inv : inventories) {
            if (inv.con.hasInventoryPanelUpgrade(inv.conDir)) {
                res.add(inv);
            }
        }
        return res;
    }

    private List<NetworkedInventory> getOrCreate(BlockCoord bc) {
        List<NetworkedInventory> res = invMap.get(bc);
        if (res == null) {
            res = new ArrayList<>();
            invMap.put(bc, res);
        }
        return res;
    }

    public void inventoryRemoved(ItemConduit itemConduit, int x, int y, int z) {
        BlockCoord bc = new BlockCoord(x, y, z);
        List<NetworkedInventory> invs = getOrCreate(bc);
        NetworkedInventory remove = null;
        for (NetworkedInventory ni : invs) {
            if (ni.con.getLocation().equals(itemConduit.getLocation())) {
                remove = ni;
                break;
            }
        }
        if (remove != null) {
            // Capture the pre-removal index so a mid-sweep removal at an already-swept slot can
            // shift sortCursor back by one; otherwise a not-yet-swept entry shifted below the
            // cursor by the removal would be skipped for the rest of the epoch.
            int idx = inventories.indexOf(remove);
            invs.remove(remove);
            inventories.remove(remove);
            if (idx >= 0 && idx < sortCursor) {
                sortCursor--;
            }
            pendingFirstSort.remove(remove);
            markSortPending();
            changeCount++;
        }
    }

    public void routesChanged() {
        markSortPending();
        changeCount++;
    }

    private void markSortPending() {
        if (requiresSort && sortCursor > 0) {
            sortDirty = true;
        } else {
            if (!requiresSort) {
                sortEpoch++; // a fresh pass begins; old sortedInEpoch stamps must no longer match
            }
            requiresSort = true;
        }
    }

    public void inventoryPanelSourcesChanged() {
        changeCount++;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public InventoryDatabaseServer getDatabase() {
        check: {
            if (database == null) {
                database = new InventoryDatabaseServer(this);
            } else if (database.isCurrent()) {
                break check;
            }
            database.updateNetworkSources();
        }
        return database;
    }

    @Override
    public void destroyNetwork() {
        super.destroyNetwork();
        if (database != null) {
            database.resetDatabase();
            database = null;
        }
    }

    public ItemStack sendItems(ItemConduit itemConduit, ItemStack item, ForgeDirection side) {
        if (doingSend) {
            return item;
        }

        if (item == null) {
            return item;
        }

        try {
            doingSend = true;
            BlockCoord loc = itemConduit.getLocation().getLocation(side);

            ItemStack result = item.copy();
            List<NetworkedInventory> invs = getOrCreate(loc);
            for (NetworkedInventory inv : invs) {

                if (inv.con.getLocation().equals(itemConduit.getLocation())) {
                    int numInserted = inv.insertIntoTargets(item.copy());
                    if (numInserted >= item.stackSize) {
                        return null;
                    }
                    result.stackSize -= numInserted;
                }
            }
            return result;
        } finally {
            doingSend = false;
        }
    }

    public List<String> getTargetsForExtraction(BlockCoord extractFrom, IItemConduit con, ItemStack input) {
        List<String> result = new ArrayList<>();

        List<NetworkedInventory> invs = getOrCreate(extractFrom);
        for (NetworkedInventory source : invs) {

            if (source.con.getLocation().equals(con.getLocation())) {
                if (source.sendPriority != null) {
                    for (Target t : source.sendPriority) {
                        IItemFilter f = t.inv.con.getOutputFilter(t.inv.conDir);
                        if (input == null || f == null || f.doesItemPassFilter(t.inv, input)) {
                            String s = t.inv.getLocalizedInventoryName() + " "
                                    + t.inv.location.chatString()
                                    + " Distance ["
                                    + t.distance
                                    + "] ";
                            result.add(s);
                        }
                    }
                }
            }
        }

        return result;
    }

    public List<String> getInputSourcesFor(IItemConduit con, ForgeDirection dir, ItemStack input) {
        List<String> result = new ArrayList<>();
        for (NetworkedInventory inv : inventories) {
            if (inv.hasTarget(con, dir)) {
                IItemFilter f = inv.con.getInputFilter(inv.conDir);
                if (input == null || f == null || f.doesItemPassFilter(inv, input)) {
                    result.add(inv.getLocalizedInventoryName() + " " + inv.location.chatString());
                }
            }
        }
        return result;
    }

    private static final class CachedDistances {

        final int topologyVersion;
        final Map<BlockCoord, Integer> distances;

        CachedDistances(int topologyVersion, Map<BlockCoord, Integer> distances) {
            this.topologyVersion = topologyVersion;
            this.distances = distances;
        }
    }

    Map<BlockCoord, Integer> getDistancesFrom(BlockCoord source) {
        CachedDistances cached = distanceCache.get(source);
        if (cached != null && cached.topologyVersion == topologyVersion) {
            return cached.distances;
        }
        Map<BlockCoord, Integer> distances = computeDistances(source);
        distanceCache.put(source, new CachedDistances(topologyVersion, distances));
        return distances;
    }

    private Map<BlockCoord, Integer> computeDistances(BlockCoord source) {
        Map<BlockCoord, Integer> distances = new HashMap<>();
        List<BlockCoord> steps = new ArrayList<>();
        steps.add(source);
        int distance = 0;
        while (!steps.isEmpty()) {
            List<BlockCoord> nextSteps = new ArrayList<>();
            for (BlockCoord bc : steps) {
                IItemConduit con = conMap.get(bc);
                if (con == null) {
                    continue; // no live conduit at this coordinate
                }
                Integer prevDist = distances.get(bc);
                if (prevDist != null && prevDist <= distance) {
                    continue;
                }
                distances.put(bc, distance);
                for (ForgeDirection dir : con.getConduitConnections()) {
                    nextSteps.add(bc.getLocation(dir));
                }
            }
            steps = nextSteps;
            distance++;
        }
        return distances;
    }

    // Sorts one NetworkedInventory and stamps it with the current sortEpoch. Both the
    // pendingFirstSort drain and the cursor sweep below must go through this helper exclusively,
    // so an entry drained here is recognized (and free-skipped) by the sweep instead of being
    // sorted a second time.
    private void sortOne(NetworkedInventory ni) {
        ni.updateInsertOrder();
        ni.sortedInEpoch = sortEpoch;
    }

    @Override
    public void doNetworkTick() {
        if (requiresSort) {
            int budget = MAX_INVENTORY_SORTS_PER_TICK;

            // Never-sorted entries always go first, so a freshly built or rebuilt network drains
            // within ceil(N / MAX_INVENTORY_SORTS_PER_TICK) ticks instead of a same-tick burst.
            while (budget > 0 && !pendingFirstSort.isEmpty()) {
                sortOne(pendingFirstSort.poll());
                budget--;
            }

            int size = inventories.size();
            while (sortCursor < size) {
                NetworkedInventory ni = inventories.get(sortCursor);
                if (ni.sortedInEpoch == sortEpoch) {
                    // Already sorted this epoch (e.g. drained above); skip for free, no budget cost.
                    sortCursor++;
                    continue;
                }
                if (budget == 0) {
                    break;
                }
                sortOne(ni);
                sortCursor++;
                budget--;
            }
            if (sortCursor >= size) {
                if (sortDirty) {
                    sortDirty = false; // one more clean pass starts next tick; requiresSort stays true
                    sortEpoch++; // fresh pass begins; old sortedInEpoch stamps no longer match
                    sortCursor = 0;
                } else {
                    requiresSort = false; // a full pass completed with nothing new arriving during it
                    sortCursor = 0;
                }
            }
        }
        for (NetworkedInventory ni : inventories) {
            ni.onTick();
        }
        if (database != null) {
            database.tick();
        }
    }

    static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    static int MAX_SLOT_CHECK_PER_TICK = 64;
    static int MAX_INVENTORY_SORTS_PER_TICK = 8; // peak updateInsertOrder() calls per tick; a full rebuild of N
                                                 // inventories costs exactly N sorts, converging within ceil(N/8)
                                                 // ticks; route-config changes propagate within ceil(N/8) ticks
}
