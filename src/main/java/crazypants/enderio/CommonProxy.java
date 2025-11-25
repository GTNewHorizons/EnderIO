package crazypants.enderio;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import crazypants.enderio.conduit.ConduitNetworkTickHandler;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.render.ConduitRenderer;
import crazypants.enderio.machine.hypercube.HyperCubeRegister;
import crazypants.enderio.machine.transceiver.ServerChannelRegister;

public class CommonProxy {

    private static final DecimalFormat FORMAT = new DecimalFormat("########0.000");

    protected long serverTickCount = 0;
    protected long clientTickCount = 0;
    protected final TickTimer tickTimer = new TickTimer();
    public ConduitNetworkTickHandler conduitNetworkTickHandler = null;

    public CommonProxy() {}

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    @Deprecated
    public ConduitRenderer getRendererForConduit(IConduit conduit) {
        return null;
    }

    public double getReachDistanceForPlayer(EntityPlayer entityPlayer) {
        return 5;
    }

    public void loadIcons() {

    }

    public void load() {
        FMLCommonHandler.instance().bus().register(tickTimer);
    }

    public long getTickCount() {
        return serverTickCount;
    }

    public boolean isNeiInstalled() {
        return false;
    }

    public void setInstantConfusionOnPlayer(EntityPlayer ent, int duration) {
        ent.addPotionEffect(new PotionEffect(Potion.confusion.getId(), duration, 1, true));
    }

    protected void onServerTick() {
        ++serverTickCount;
    }

    protected void onClientTick() {}

    private static final String TEXTURE_PATH = ":textures/gui/23/";
    private static final String TEXTURE_EXT = ".png";

    public ResourceLocation getGuiTexture(String name) {
        return new ResourceLocation(EnderIO.DOMAIN + TEXTURE_PATH + name + TEXTURE_EXT);
    }

    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        conduitNetworkTickHandler = new ConduitNetworkTickHandler();
        FMLCommonHandler.instance().bus().register(conduitNetworkTickHandler);
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        HyperCubeRegister.load();
        ServerChannelRegister.load();
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        HyperCubeRegister.unload();
        FMLCommonHandler.instance().bus().unregister(conduitNetworkTickHandler);
        conduitNetworkTickHandler = null;
    }

    public final class TickTimer {

        @SubscribeEvent
        public void onTick(ServerTickEvent evt) {
            if (evt.phase == Phase.END) {
                onServerTick();
            }
        }

        @SubscribeEvent
        public void onTick(ClientTickEvent evt) {
            if (evt.phase == Phase.END) {
                onClientTick();
            }
        }
    }
}
