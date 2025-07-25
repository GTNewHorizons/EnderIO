package crazypants.enderio.machine.killera;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.button.ToggleButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.SoundUtil;
import com.google.common.collect.Lists;

import crazypants.enderio.EnderIO;
import crazypants.enderio.fluid.Fluids;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.machine.IoMode;
import crazypants.enderio.machine.gui.GuiMachineBase;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.xp.ExperienceBarRenderer;
import crazypants.enderio.xp.PacketGivePlayerXP;

public class GuiKillerJoe extends GuiMachineBase<TileKillerJoe> {

    private static final int RANGE_ID = 8738924;
    private final ToggleButton showRangeB;

    private static final int XP_ID = 3489;
    private static final int XP10_ID = 34892;

    private IconButton xpB;
    private IconButton xp10B;

    public GuiKillerJoe(InventoryPlayer inventory, final TileKillerJoe tileEntity) {
        super(tileEntity, new ContainerKillerJoe(inventory, tileEntity), "killerJoe");

        showRangeB = new ToggleButton(this, RANGE_ID, 155, 24 + 18, IconEIO.PLUS, IconEIO.MINUS);
        showRangeB.setSize(BUTTON_SIZE, BUTTON_SIZE);
        addToolTip(new GuiToolTip(showRangeB.getBounds(), "null") {

            @Override
            public List<String> getToolTipText() {
                return Lists.newArrayList(
                        EnderIO.lang.localize(
                                showRangeB.isSelected() ? "gui.spawnGurad.hideRange" : "gui.spawnGurad.showRange"));
            }
        });

        addToolTip(new GuiToolTip(new Rectangle(18, 11, 15, 47), "") {

            @Override
            protected void updateText() {
                text.clear();
                String heading = EnderIO.lang.localize("killerJoe.fuelTank");
                text.add(heading);
                text.add(Fluids.toCapactityString(getTileEntity().fuelTank));
                if (tileEntity.fuelTank.getFluidAmount() < tileEntity.getActivationAmount()) {
                    text.add(EnderIO.lang.localize("gui.fluid.minReq", tileEntity.getActivationAmount() + Fluids.MB()));
                }
            }
        });

        int size = 14;
        int x = guiLeft + 56 + (65 / 2);
        int y = guiTop + 62 + 6;

        xpB = new IconButton(this, XP_ID, x - 16, y, IconEIO.XP);
        xpB.setToolTip(EnderIO.lang.localize("killerJoe.giveXp.tooltip"));
        xpB.setSize(size, size);

        xp10B = new IconButton(this, XP10_ID, x + 2, y, IconEIO.XP_PLUS);
        xp10B.setToolTip(EnderIO.lang.localize("killerJoe.giveXp10.tooltip"));
        xp10B.setSize(size, size);
    }

    @Override
    public void initGui() {
        super.initGui();
        xpB.onGuiInit();
        xp10B.onGuiInit();

        showRangeB.onGuiInit();
        showRangeB.setSelected(getTileEntity().isShowingRange());
        ((ContainerKillerJoe) inventorySlots).createGhostSlots(getGhostSlots());
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        super.actionPerformed(b);
        if (b.id == XP_ID) {
            PacketHandler.INSTANCE.sendToServer(new PacketGivePlayerXP(getTileEntity(), 1));
            SoundUtil.playClientSoundFX("random.orb", getTileEntity());
        } else if (b.id == XP10_ID) {
            PacketHandler.INSTANCE.sendToServer(new PacketGivePlayerXP(getTileEntity(), 10));
            SoundUtil.playClientSoundFX("random.orb", getTileEntity());
        } else if (b.id == RANGE_ID) {
            getTileEntity().setShowRange(showRangeB.isSelected());
        }
    }

    @Override
    public void renderSlotHighlights(IoMode mode) {
        super.renderSlotHighlights(mode);

        if (mode == IoMode.PULL || mode == IoMode.PUSH_PULL) {
            int x = 16;
            int y = 9;
            int w = 15 + 4;
            int h = 47 + 4;
            renderSlotHighlight(PULL_COLOR, x, y, w, h);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindGuiTexture();
        int sx = (width - xSize) / 2;
        int sy = (height - ySize) / 2;
        drawTexturedModalRect(sx, sy, 0, 0, xSize, ySize);

        int x = guiLeft + 18;
        int y = guiTop + 11;
        TileKillerJoe joe = getTileEntity();
        if (joe.fuelTank.getFluidAmount() > 0) {
            RenderUtil.renderGuiTank(
                    joe.fuelTank.getFluid(),
                    joe.fuelTank.getCapacity(),
                    joe.fuelTank.getFluidAmount(),
                    x,
                    y,
                    zLevel,
                    16,
                    47);
        }
        ExperienceBarRenderer.render(this, sx + 56, sy + 62, 65, joe.getContainer());
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
    }
}
