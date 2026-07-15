package crazypants.enderio.etfuturum;

import net.minecraft.entity.player.EntityPlayer;

import crazypants.enderio.EnderIO;
import ganymedes01.etfuturum.elytra.IElytraPlayer;

public final class EtFuturumCompat {

    private EtFuturumCompat() {}

    public static boolean isElytraFlying(EntityPlayer player) {
        return EnderIO.hasEtFuturum && player instanceof IElytraPlayer
                && ((IElytraPlayer) player).etfu$isElytraFlying();
    }

    public static void stopElytraFlying(EntityPlayer player) {
        if (EnderIO.hasEtFuturum && player instanceof IElytraPlayer) {
            ((IElytraPlayer) player).etfu$setElytraFlying(false);
        }
    }
}
