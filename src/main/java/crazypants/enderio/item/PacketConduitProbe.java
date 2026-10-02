package crazypants.enderio.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.ChatUtil;
import com.gtnewhorizon.gtnhlib.chat.customcomponents.ChatComponentItemName;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.EnderIO;
import crazypants.enderio.Log;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.TileConduitBundle;
import crazypants.enderio.conduit.item.IItemConduit;
import crazypants.enderio.conduit.item.ItemConduitNetwork;
import crazypants.enderio.conduit.power.IPowerConduit;
import crazypants.enderio.conduit.power.NetworkPowerManager;
import crazypants.enderio.conduit.power.PowerConduitNetwork;
import crazypants.enderio.conduit.power.PowerTracker;
import crazypants.enderio.power.EnergyHandlerPI;
import crazypants.enderio.power.IInternalPowerReceiver;
import crazypants.enderio.power.IInternalPoweredTile;
import crazypants.enderio.power.PowerDisplayUtil;
import io.netty.buffer.ByteBuf;

public class PacketConduitProbe implements IMessage, IMessageHandler<PacketConduitProbe, IMessage> {

    public static boolean canCreatePacket(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null) {
            return false;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileConduitBundle) {
            TileConduitBundle tcb = (TileConduitBundle) te;
            return tcb.getConduit(IPowerConduit.class) != null || tcb.getConduit(IItemConduit.class) != null;
        }
        if (te instanceof IInternalPoweredTile) {
            return true;
        }
        return false;
    }

    private int x;
    private int y;
    private int z;
    private ForgeDirection side;

    public PacketConduitProbe() {}

    public PacketConduitProbe(int x2, int y2, int z2, int side2) {
        this(x2, y2, z2, ForgeDirection.getOrientation(side2));
    }

    public PacketConduitProbe(int x, int y, int z, ForgeDirection side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeShort(side.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        side = ForgeDirection.getOrientation(buffer.readShort());
    }

    @Override
    public IMessage onMessage(PacketConduitProbe message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        if (world == null) {
            Log.warn("MJReaderPacketHandler.sendInfoMessage: Could not handle packet as player world was null.");
            return null;
        }
        Block block = world.getBlock(message.x, message.y, message.z);
        if (block == null) {
            return null;
        }

        TileEntity te = world.getTileEntity(message.x, message.y, message.z);
        if (te instanceof TileConduitBundle) {

            sendInfoMessage(player, (TileConduitBundle) te);

        } else if (te instanceof IInternalPowerReceiver) {
            IInternalPowerReceiver pr = (IInternalPowerReceiver) te;
            sendPowerReciptorInfo(
                    player,
                    block,
                    pr.getEnergyStored(),
                    pr.getMaxEnergyStored(),
                    0,
                    0,
                    EnergyHandlerPI.getPowerRequest(ForgeDirection.NORTH, pr));
        }
        return null;
    }

    public static void sendInfoMessage(EntityPlayer player, TileConduitBundle tcb) {

        if (tcb.getConduit(IItemConduit.class) != null) {
            sendInfoMessage(player, tcb.getConduit(IItemConduit.class), null);
        }
        IPowerConduit conduit = tcb.getConduit(IPowerConduit.class);
        if (conduit != null) {
            sendInfoMessage(player, conduit);
        }
    }

    public static void sendInfoMessage(EntityPlayer player, IPowerConduit conduit) {
        PowerConduitNetwork pcn = (PowerConduitNetwork) conduit.getNetwork();
        NetworkPowerManager pm = pcn.getPowerManager();
        PowerTracker tracker = pm.getTracker(conduit);
        if (tracker != null) {
            sendPowerConduitInfo(player, conduit, tracker);
        } else {
            sendInfoMessage(player, pm);
        }
    }

    public static void sendInfoMessage(EntityPlayer player, IItemConduit conduit, ItemStack input) {
        List<IChatComponent> lines = new ArrayList<>();

        if (conduit.getExternalConnections().isEmpty()) {
            lines.add(
                    line(EnumChatFormatting.GREEN).appendSibling(tr("gui.mjReader.itemHeading")).appendText(" ")
                            .appendSibling(tr("gui.mjReader.itemNoConnections")));
        } else {
            for (ForgeDirection dir : conduit.getExternalConnections()) {
                ConnectionMode mode = conduit.getConnectionMode(dir);

                lines.add(
                        line(EnumChatFormatting.GREEN).appendSibling(tr("gui.mjReader.itemHeading")).appendText(" ")
                                .appendSibling(tr("gui.mjReader.connectionDir")).appendText(" " + dir));

                ItemConduitNetwork icn = (ItemConduitNetwork) conduit.getNetwork();
                if (icn != null && mode.acceptsInput()) {
                    IChatComponent line = line(EnumChatFormatting.BLUE);
                    if (input == null) {
                        line.appendSibling(tr("gui.mjReader.extractedItems"));
                    } else {
                        line.appendSibling(tr("gui.mjReader.extractedItem")).appendText(" ")
                                .appendSibling(new ChatComponentItemName(input));
                    }
                    line.appendText(" ");
                    List<IChatComponent> targets = icn
                            .getTargetsForExtraction(conduit.getLocation().getLocation(dir), conduit, input);
                    if (targets.isEmpty()) {
                        lines.add(line.appendSibling(tr("gui.mjReader.noOutputs")).appendText("."));
                    } else {
                        lines.add(line.appendSibling(tr("gui.mjReader.insertedInto")));
                        for (IChatComponent target : targets) {
                            lines.add(new ChatComponentText("  - ").appendSibling(target));
                        }
                    }
                }
                if (icn != null && mode.acceptsOutput()) {
                    IChatComponent line = line(EnumChatFormatting.BLUE);
                    List<IChatComponent> targets = icn.getInputSourcesFor(conduit, dir, input);
                    if (targets.isEmpty()) {
                        if (input == null) {
                            line.appendSibling(tr("gui.mjReader.noItems"));
                        } else {
                            line.appendSibling(tr("gui.mjReader.noItem")).appendText(" ")
                                    .appendSibling(new ChatComponentItemName(input));
                        }
                        lines.add(line);
                    } else {
                        if (input == null) {
                            line.appendSibling(tr("gui.mjReader.receiveItems"));
                        } else {
                            line.appendSibling(tr("gui.mjReader.receiveItem1"))
                                    .appendText(" ").appendSibling(new ChatComponentItemName(input)).appendText(" ")
                                    .appendSibling(tr("gui.mjReader.receiveItem2"));
                        }
                        lines.add(line);
                        for (IChatComponent target : targets) {
                            lines.add(new ChatComponentText("  - ").appendSibling(target));
                        }
                    }
                }
            }
        }
        ChatUtil.sendNoSpam(player, lines.toArray(new IChatComponent[0]));
    }

    private static IChatComponent tr(String key) {
        return new ChatComponentTranslation(EnderIO.lang.addPrefix(key));
    }

    private static IChatComponent line(EnumChatFormatting color) {
        IChatComponent line = new ChatComponentText(" ");
        line.getChatStyle().setColor(color);
        return line;
    }

    private static IChatComponent power(long amount, long max) {
        return new ChatComponentText(PowerDisplayUtil.formatPower(amount) + " ")
                .appendSibling(tr("gui.powerMonitor.of")).appendText(" " + PowerDisplayUtil.formatPower(max) + " ")
                .appendSibling(tr("power.rf"));
    }

    private static IChatComponent label(String key) {
        return new ChatComponentText(" ").appendSibling(tr(key)).appendText(": ");
    }

    public static void sendInfoMessage(EntityPlayer player, NetworkPowerManager pm) {
        PowerTracker tracker = pm.getNetworkPowerTracker();
        ChatUtil.sendNoSpam(
                player,
                line(EnumChatFormatting.GREEN).appendSibling(tr("gui.mjReader.networkHeading")),
                line(EnumChatFormatting.BLUE).appendSibling(label("gui.powerMonitor.monHeading1"))
                        .appendSibling(power(pm.getPowerInConduits(), pm.getMaxPowerInConduits())),
                label("gui.powerMonitor.monHeading2")
                        .appendSibling(power(pm.getPowerInCapacitorBanks(), pm.getMaxPowerInCapacitorBanks())),
                label("gui.powerMonitor.monHeading3")
                        .appendSibling(power(pm.getPowerInReceptors(), pm.getMaxPowerInReceptors())),
                label("gui.powerMonitor.monHeading4")
                        .appendText(PowerDisplayUtil.formatPowerFloat(tracker.getAverageRfTickSent())),
                label("gui.powerMonitor.monHeading5")
                        .appendText(PowerDisplayUtil.formatPowerFloat(tracker.getAverageRfTickRecieved())));
    }

    public static void sendPowerConduitInfo(EntityPlayer player, IPowerConduit con, PowerTracker tracker) {
        ChatUtil.sendNoSpam(
                player,
                line(EnumChatFormatting.GREEN).appendSibling(tr("itemPowerConduit.name")).appendSibling(
                        line(EnumChatFormatting.BLUE).appendSibling(label("gui.mjReader.conduitBuffer"))
                                .appendSibling(power(con.getEnergyStored(), con.getMaxEnergyStored()))),
                label("gui.powerMonitor.monHeading4")
                        .appendText(PowerDisplayUtil.formatPowerFloat(tracker.getAverageRfTickSent())),
                label("gui.powerMonitor.monHeading5")
                        .appendText(PowerDisplayUtil.formatPowerFloat(tracker.getAverageRfTickRecieved())));
    }

    private void sendPowerReciptorInfo(EntityPlayer player, Block block, int stored, int maxStored, int minRec,
            int maxRec, int request) {
        ChatUtil.sendNoSpam(
                player,
                line(EnumChatFormatting.GREEN)
                        .appendSibling(new ChatComponentTranslation(block.getUnlocalizedName() + ".name"))
                        .appendSibling(
                                line(EnumChatFormatting.BLUE).appendSibling(label("gui.mjReader.conduitBuffer"))
                                        .appendSibling(power(stored, maxStored))),
                label("gui.mjReader.requestRange").appendText(
                        PowerDisplayUtil.formatPower(minRec) + " - " + PowerDisplayUtil.formatPower(maxRec) + " ")
                        .appendSibling(tr("power.rf")),
                label("gui.mjReader.currentRequest").appendText(PowerDisplayUtil.formatPower(request) + " ")
                        .appendSibling(tr("power.rf")));
    }
}
