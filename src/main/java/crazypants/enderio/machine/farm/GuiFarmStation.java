package crazypants.enderio.machine.farm;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.button.ToggleButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector4f;
import com.google.common.collect.Lists;

import crazypants.enderio.EnderIO;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.machine.gui.GuiPoweredMachineBase;

public class GuiFarmStation extends GuiPoweredMachineBase<TileFarmStation> {

    private static final int RANGE_ID = 8738924;
    private static final int LOCK_ID = 1234;

    private final ToggleButton showRangeB;

    public GuiFarmStation(InventoryPlayer par1InventoryPlayer, TileFarmStation machine) {
        super(machine, new FarmStationContainer(par1InventoryPlayer, machine), "farmStation");

        int x = getXSize() - 5 - BUTTON_SIZE / 2;
        int y = getGuiTop() + 5;
        getRedstoneBtn().setPosition(x, y);

        y += BUTTON_SIZE + 2;
        getConfigBtn().setPosition(x, y);

        y += BUTTON_SIZE + 2;
        showRangeB = new ToggleButton(this, RANGE_ID, x, y, IconEIO.PLUS, IconEIO.MINUS);
        showRangeB.setSize(BUTTON_SIZE, BUTTON_SIZE);
        addToolTip(new GuiToolTip(showRangeB.getBounds(), "null") {

            @Override
            public List<String> getToolTipText() {
                return Lists.newArrayList(
                        EnderIO.lang.localize(
                                showRangeB.isSelected() ? "gui.spawnGurad.hideRange" : "gui.spawnGurad.showRange"));
            }
        });

        setYSize(ySize + 3);
        setXSize(xSize + 8);

    }

    @Override
    protected int getPowerU() {
        return 184;
    }

    @Override
    public void initGui() {
        super.initGui();

        showRangeB.onGuiInit();
        showRangeB.setSelected(getTileEntity().isShowingRange());

        int x = getGuiLeft() + 36;
        int y = getGuiTop() + 43;

        buttonList.add(createLockButton(TileFarmStation.minSupSlot + 0, x, y));
        buttonList.add(createLockButton(TileFarmStation.minSupSlot + 1, x + 52, y));
        buttonList.add(createLockButton(TileFarmStation.minSupSlot + 2, x, y + 20));
        buttonList.add(createLockButton(TileFarmStation.minSupSlot + 3, x + 52, y + 20));

        ((FarmStationContainer) inventorySlots).createGhostSlots(getGhostSlots());
    }

    private IconButton createLockButton(int slot, int x, int y) {
        return new ToggleButton(this, LOCK_ID + slot, x, y, IconEIO.LOCK_UNLOCKED, IconEIO.LOCK_LOCKED)
                .setSelected(getTileEntity().isSlotLocked(slot));
    }

    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);
        if (!isConfigOverlayEnabled()) {
            for (int i = TileFarmStation.minSupSlot; i <= TileFarmStation.maxSupSlot; i++) {
                if (getTileEntity().isSlotLocked(i)) {
                    Slot slot = inventorySlots.getSlot(i);
                    GL11.glEnable(GL11.GL_BLEND);
                    RenderUtil.renderQuad2D(
                            slot.xDisplayPosition,
                            slot.yDisplayPosition,
                            0,
                            16,
                            16,
                            new Vector4f(0, 0, 0, 0.5));
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindGuiTexture();
        int sx = (width - xSize) / 2;
        int sy = (height - ySize) / 2;

        drawTexturedModalRect(sx, sy, 0, 0, this.xSize, this.ySize);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        GL11.glEnable(GL11.GL_BLEND);
        fr.drawString("SW", sx + 55, sy + 48, ColorUtil.getARGB(1f, 1f, 0.35f, 1f), true);
        fr.drawString("NW", sx + 55, sy + 66, ColorUtil.getARGB(1f, 1f, 0.35f, 1f), true);
        fr.drawString("SE", sx + 73, sy + 48, ColorUtil.getARGB(1f, 1f, 0.35f, 1f), true);
        fr.drawString("NE", sx + 73, sy + 66, ColorUtil.getARGB(1f, 1f, 0.35f, 1f), true);
        GL11.glDisable(GL11.GL_BLEND);

        bindGuiTexture();
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id >= LOCK_ID + TileFarmStation.minSupSlot && b.id <= LOCK_ID + TileFarmStation.maxSupSlot) {
            getTileEntity().toggleLockedState(b.id - LOCK_ID);
        } else if (b.id == RANGE_ID) {
            getTileEntity().setShowRange(showRangeB.isSelected());
        }

        super.actionPerformed(b);
    }

    @Override
    protected String getPowerOutputLabel() {
        return EnderIO.lang.localize("farm.gui.baseUse");
    }

    @Override
    protected int getPowerHeight() {
        return super.getPowerHeight() + 3;
    }
}
