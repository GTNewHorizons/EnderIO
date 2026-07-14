package crazypants.enderio.etfuturum;

import net.minecraft.entity.player.EntityPlayer;

import ganymedes01.etfuturum.elytra.IElytraPlayer;

public final class EtFuturumCompat {

    private EtFuturumCompat() {}

    public static boolean isElytraFlying(EntityPlayer player) {
        return ((IElytraPlayer) player).etfu$isElytraFlying();
    }

    public static void stopElytraFlying(EntityPlayer player) {
        ((IElytraPlayer) player).etfu$setElytraFlying(false);
    }
}
