package crazypants.enderio.item;

import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.config.Config;

/**
 * Lets players move, resize and hide the Conduit Probe and Yeta Wrench HUD in Dashboard's HUD editor. The only class
 * using Dashboard, so only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    private static final HudElement PROBE = new HudElement(
            "enderio:conduit_probe",
            (screenWidth, screenHeight) -> new HudBounds(screenWidth - 80, screenHeight - 48, 64, 32));

    /** Only with the default overlay, which shows it for a moment after switching. */
    private static final HudElement WRENCH_SELECTOR = new HudElement(
            "enderio:yeta_wrench_selector",
            (screenWidth, screenHeight) -> new HudBounds(screenWidth / 2 - 22, screenHeight / 2 - 24, 44, 16))
                    .setActiveCheck(() -> Config.yetaWrenchOverlayMode == 0);

    /** The corner icon, the column or the group of icons, depending on the overlay setting. */
    private static final HudElement WRENCH_MODE = new HudElement(
            "enderio:yeta_wrench_mode",
            DashboardHud::getWrenchModeBounds);

    private DashboardHud() {}

    public static void register() {
        PROBE.setPreview((screenWidth, screenHeight) -> HudRegistry.render(PROBE, () -> {
            ConduitProbeOverlayRenderer.draw(true, HudRegistry.getResolution());
            GL11.glColor4f(1, 1, 1, 1);
        }));
        WRENCH_SELECTOR.setPreview(
                (screenWidth, screenHeight) -> HudRegistry.render(
                        WRENCH_SELECTOR,
                        () -> YetaWrenchOverlayRenderer
                                .draw(ConduitDisplayMode.ALL, HudRegistry.getResolution(), true)));
        WRENCH_MODE.setPreview(
                (screenWidth, screenHeight) -> HudRegistry.render(
                        WRENCH_MODE,
                        () -> YetaWrenchOverlayRenderer
                                .draw(ConduitDisplayMode.ALL, HudRegistry.getResolution(), false)));
        HudRegistry.register(PROBE);
        HudRegistry.register(WRENCH_SELECTOR);
        HudRegistry.register(WRENCH_MODE);
    }

    /** False means the player hid it, so skip drawing it and do not call {@link #endRender()}. */
    static boolean beginProbe(ScaledResolution resolution) {
        return HudRegistry.beginRender(PROBE, resolution);
    }

    /** The selector while it shows, otherwise the corner icon, the column or the group of icons. */
    static boolean beginWrench(ScaledResolution resolution, boolean showSelector) {
        boolean selector = showSelector && Config.yetaWrenchOverlayMode == 0;
        return HudRegistry.beginRender(selector ? WRENCH_SELECTOR : WRENCH_MODE, resolution);
    }

    static void endRender() {
        HudRegistry.endRender();
    }

    /** Measured like YetaWrenchOverlayRenderer draws each overlay. */
    private static HudBounds getWrenchModeBounds(int screenWidth, int screenHeight) {
        int modeCount = ConduitDisplayMode.registrySize();
        return switch (Config.yetaWrenchOverlayMode) {
            case 1 -> new HudBounds(screenWidth - 23, screenHeight - modeCount * 18 - 6, 23, modeCount * 18 + 6);
            case 2 -> new HudBounds(screenWidth - modeCount * 8 - 16, screenHeight - 40, (modeCount + 1) / 2 * 16, 32);
            default -> new HudBounds(screenWidth - 20, screenHeight - 20, 16, 16);
        };
    }
}
