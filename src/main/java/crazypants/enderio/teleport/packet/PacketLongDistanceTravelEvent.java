package crazypants.enderio.teleport.packet;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.Util;
import com.enderio.core.common.vecmath.Vector3d;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.Log;
import crazypants.enderio.api.teleport.IItemOfTravel;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TeleportEntityEvent;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.teleport.ItemTeleportStaff;
import crazypants.enderio.teleport.TravelController;
import io.netty.buffer.ByteBuf;

public class PacketLongDistanceTravelEvent
        implements IMessage, IMessageHandler<PacketLongDistanceTravelEvent, IMessage> {

    boolean conserveMotion;
    int entityId;
    int source;
    // Below values are for also attempting a regular blink if the long-distance anchor check fails.
    boolean alsoDoTeleport;
    int tpX;
    int tpY;
    int tpZ;

    public PacketLongDistanceTravelEvent() {}

    public PacketLongDistanceTravelEvent(Entity entity, boolean conserveMotion, TravelSource source) {
        this(entity, conserveMotion, source, false, 0, 0, 0);
    }

    public PacketLongDistanceTravelEvent(Entity entity, boolean conserveMotion, TravelSource source,
            boolean alsoDoTeleport, int tpX, int tpY, int tpZ) {
        this.conserveMotion = conserveMotion;
        this.entityId = entity instanceof EntityPlayer ? -1 : entity.getEntityId();
        this.source = source.ordinal();
        this.alsoDoTeleport = alsoDoTeleport;
        this.tpX = tpX;
        this.tpY = tpY;
        this.tpZ = tpZ;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(conserveMotion);
        buf.writeInt(entityId);
        buf.writeInt(source);
        buf.writeBoolean(alsoDoTeleport);
        buf.writeInt(tpX);
        buf.writeInt(tpY);
        buf.writeInt(tpZ);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        conserveMotion = buf.readBoolean();
        entityId = buf.readInt();
        source = buf.readInt();
        alsoDoTeleport = buf.readBoolean();
        tpX = buf.readInt();
        tpY = buf.readInt();
        tpZ = buf.readInt();
    }

    @Override
    public IMessage onMessage(PacketLongDistanceTravelEvent message, MessageContext ctx) {
        if (message.entityId != -1) {
            // after checking the code base, this type of packet won't be sent at all,
            // so we can assume this to be an attempt to hack
            Log.LOGGER.warn(
                    Log.securityMarker,
                    "Player {} tried to illegally tp other entity {}.",
                    ctx.getServerHandler().playerEntity.getGameProfile(),
                    message.entityId);
            return null;
        }
        EntityPlayerMP toTp = ctx.getServerHandler().playerEntity;

        TravelSource source = TravelSource.values()[message.source];

        if (!validate(
                toTp,
                source,
                message.conserveMotion,
                message.alsoDoTeleport,
                message.tpX,
                message.tpY,
                message.tpZ)) {
            Log.LOGGER.warn(
                    Log.securityMarker,
                    "Player {} tried to tp without valid prereq.",
                    ctx.getServerHandler().playerEntity.getGameProfile());
            return null;
        }

        doServerTeleport(
                toTp,
                message.conserveMotion,
                source,
                message.alsoDoTeleport,
                message.tpX,
                message.tpY,
                message.tpZ);

        return null;
    }

    private static boolean validate(EntityPlayerMP toTp, TravelSource source, boolean conserveMotion,
            boolean alsoDoTeleport, int tpX, int tpY, int tpZ) {
        ItemStack equippedItem = toTp.getCurrentEquippedItem();
        switch (source) {
            case STAFF:
                // Not allowed to do teleport staff teleport with regular staff.
                if (alsoDoTeleport) {
                    return false;
                }
                return equippedItem != null && equippedItem.getItem() instanceof IItemOfTravel
                        && ((IItemOfTravel) equippedItem.getItem()).isActive(toTp, equippedItem);
            case TELEPORT_STAFF:
                // tp staff is creative version of traveling staff
                // no energy check or anything else needed
                // but the player must actually be equipped with one of these
                if (alsoDoTeleport) {
                    String error = TravelController.instance
                            .validatePacketTravelEvent(toTp, tpX, tpY, tpZ, 0, conserveMotion, source);
                    if (error != null) {
                        return false;
                    }
                }
                return equippedItem != null && equippedItem.getItem() instanceof ItemTeleportStaff;

            default:
                // all other types are not allowed
                return false;
        }
    }

    public static boolean doServerTeleport(Entity toTp, boolean conserveMotion, TravelSource source,
            boolean alsoDoTeleport, int tpX, int tpY, int tpZ) {
        if (!(toTp instanceof EntityPlayer)) {
            return false;
        }
        EntityPlayer player = (EntityPlayer) toTp;

        Optional<BlockCoord> travelDestination = TravelController.instance.findTravelDestination(player, source);
        if (!travelDestination.isPresent()) {
            if (alsoDoTeleport) {
                return PacketTravelEvent.doServerTeleport(toTp, tpX, tpY, tpZ, 0, conserveMotion, source);
            }
            return false;
        }

        BlockCoord targetBlock = travelDestination.get();
        TileEntity te = player.worldObj.getTileEntity(targetBlock.x, targetBlock.y, targetBlock.z);
        if (te instanceof ITravelAccessable) {
            ITravelAccessable ta = (ITravelAccessable) te;
            if (!ta.canBlockBeAccessed(player)) {
                TravelController
                        .showMessage(player, new ChatComponentTranslation("enderio.gui.travelAccessable.unauthorised"));
                return false;
            }
        }

        // We're teleporting to a block, so go one above.
        BlockCoord destination = targetBlock.getLocation(ForgeDirection.UP);
        if (!TravelController.instance.isValidTarget(player, destination, source)) {
            TravelController
                    .showMessage(player, new ChatComponentTranslation("enderio.blockTravelPlatform.invalidTarget"));
            return false;
        }

        int x = destination.x;
        int y = destination.y;
        int z = destination.z;
        int powerUse = TravelController.instance.getRequiredPower(player, source, destination);
        if (powerUse < 0) {
            return false;
        }
        if (player.getCurrentEquippedItem() != null
                && player.getCurrentEquippedItem().getItem() instanceof IItemOfTravel) {
            int used = ((IItemOfTravel) player.getCurrentEquippedItem().getItem())
                    .canExtractInternal(player.getCurrentEquippedItem(), powerUse);
            if (used != -1 && used != powerUse) {
                return false;
            }
        }

        TeleportEntityEvent evt = new TeleportEntityEvent(toTp, source, x, y, z);
        if (MinecraftForge.EVENT_BUS.post(evt)) {
            return false;
        }
        x = evt.targetX;
        y = evt.targetY;
        z = evt.targetZ;

        toTp.worldObj.playSoundEffect(toTp.posX, toTp.posY, toTp.posZ, source.sound, 1.0F, 1.0F);

        toTp.playSound(source.sound, 1.0F, 1.0F);

        player.setPositionAndUpdate(x + 0.5, y + 0.1, z + 0.5);

        player.worldObj.playSoundEffect(x, y, z, source.sound, 1.0F, 1.0F);
        player.fallDistance = 0;

        if (conserveMotion) {
            Vector3d velocityVex = Util.getLookVecEio(player);
            S12PacketEntityVelocity p = new S12PacketEntityVelocity(
                    toTp.getEntityId(),
                    velocityVex.x,
                    velocityVex.y,
                    velocityVex.z);
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(p);
        }

        if (powerUse > 0 && player.getCurrentEquippedItem() != null
                && player.getCurrentEquippedItem().getItem() instanceof IItemOfTravel) {
            ItemStack item = player.getCurrentEquippedItem().copy();
            ((IItemOfTravel) item.getItem()).extractInternal(item, powerUse);
            toTp.setCurrentItemOrArmor(0, item);
        }

        return true;
    }
}
