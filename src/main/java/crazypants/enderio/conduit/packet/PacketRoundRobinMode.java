package crazypants.enderio.conduit.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.conduit.liquid.EnderLiquidConduit;
import crazypants.enderio.conduit.liquid.ILiquidConduit;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.util.ForgeDirection;

public class PacketRoundRobinMode extends AbstractConduitPacket<ILiquidConduit> implements IMessageHandler<PacketRoundRobinMode, IMessage> {
    private ForgeDirection dir;
    private boolean roundRobin;

    public PacketRoundRobinMode() {
    }

    public PacketRoundRobinMode(EnderLiquidConduit eConduit, ForgeDirection dir) {
        super(eConduit.getBundle().getEntity(), ConTypeEnum.FLUID);
        this.dir = dir;
        roundRobin = eConduit.isRoundRobin(dir);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeShort(dir.ordinal());
        buf.writeBoolean(roundRobin);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        dir = ForgeDirection.values()[buf.readShort()];
        roundRobin = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketRoundRobinMode message, MessageContext ctx) {
        final ILiquidConduit conduit = message.getTileCasted(ctx);
        if (conduit instanceof EnderLiquidConduit) {
            ((EnderLiquidConduit) conduit).setRoundRobin(message.dir, message.roundRobin);
            message.getWorld(ctx).markBlockForUpdate(message.x, message.y, message.z);
        }
        return null;
    }
}
