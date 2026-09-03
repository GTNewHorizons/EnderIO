package crazypants.enderio.teleport.telepad;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import com.enderio.core.common.network.MessageTileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.EnderIO;
import crazypants.enderio.network.PacketUtil;
import io.netty.buffer.ByteBuf;

public class PacketTeleport extends MessageTileEntity<TileTelePad>
        implements IMessageHandler<PacketTeleport, IMessage> {

    enum Type {
        BEGIN,
        END,
        TELEPORT
    }

    public PacketTeleport() {
        super();
    }

    private int entityId;
    private Type type;
    private boolean wasBlocked;

    public PacketTeleport(Type type, TileTelePad te, int entityId) {
        super(te);
        this.entityId = entityId;
        this.type = type;
    }

    public PacketTeleport(Type type, TileTelePad te, boolean wasBlocked) {
        super(te);
        this.wasBlocked = wasBlocked;
        this.type = type;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(entityId);
        buf.writeInt(type.ordinal());
        buf.writeBoolean(wasBlocked);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        entityId = buf.readInt();
        type = Type.values()[buf.readInt()];
        wasBlocked = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketTeleport message, MessageContext ctx) {
        boolean isClient = ctx.side.isClient();
        World world = isClient ? EnderIO.proxy.getClientWorld() : message.getWorld(ctx);
        TileTelePad te = message.getTileEntity(world);
        if (te == null) return null;
        if (!isClient) {
            // BEGIN is the only type of PacketTeleport that originates from a TE GUI
            if (message.type == Type.BEGIN && PacketUtil.isInvalidPacketForGui(ctx, te, getClass())) {
                return null;
            }
            // TELEPORT is never sent to server
            if (message.type == Type.TELEPORT) return null;
            // other types are sent in background without a gui, so no checks
        }
        Entity e = world.getEntityByID(message.entityId);
        switch (message.type) {
            case BEGIN:
                te.enqueueTeleport(e, false);
                break;
            case END:
                te.dequeueTeleport(e, false);
                break;
            case TELEPORT:
                te.wasBlocked = message.wasBlocked;
                break;
        }
        return null;
    }
}
