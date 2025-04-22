package crazypants.enderio.machine.obelisk.weather;

import com.enderio.core.common.network.MessageTileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.network.PacketUtil;
import io.netty.buffer.ByteBuf;

public class PacketControlModeWeather extends MessageTileEntity<TileWeatherObelisk>
        implements IMessageHandler<PacketControlModeWeather, IMessage> {

    private boolean pulse;

    public PacketControlModeWeather() {}

    public PacketControlModeWeather(TileWeatherObelisk te) {
        super(te);
        this.pulse = te.getLaunchOnRedstone();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(pulse);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        pulse = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketControlModeWeather message, MessageContext ctx) {
        TileWeatherObelisk te = message.getTileEntity(message.getWorld(ctx));
        if (te != null) {
            if (PacketUtil.isInvalidPacketForGui(ctx, te, getClass())) return null;
            te.setLaunchOnRedstone(message.pulse);
        }
        return null;
    }
}
