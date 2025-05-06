package crazypants.enderio.machine.obelisk.weather;

import com.enderio.core.common.network.MessageTileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.EnderIO;
import crazypants.enderio.network.PacketUtil;
import io.netty.buffer.ByteBuf;

public class PacketActivateWeather extends MessageTileEntity<TileWeatherObelisk>
        implements IMessageHandler<PacketActivateWeather, IMessage> {

    private boolean start;

    public PacketActivateWeather() {}

    public PacketActivateWeather(TileWeatherObelisk te, boolean start) {
        super(te);
        this.start = start;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(start);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        start = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketActivateWeather message, MessageContext ctx) {
        TileWeatherObelisk te = message
                .getTileEntity(ctx.side.isServer() ? message.getWorld(ctx) : EnderIO.proxy.getClientWorld());
        if (te != null) {
            if (ctx.side.isServer()) {
                if (PacketUtil.isInvalidPacketForGui(ctx, te, getClass())) return null;
            }
            if (message.start) {
                te.startTask();
            } else {
                te.stopTask();
            }
        }
        return null;
    }
}
