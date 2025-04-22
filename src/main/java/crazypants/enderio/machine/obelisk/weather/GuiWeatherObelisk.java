package crazypants.enderio.machine.obelisk.weather;

import java.awt.Color;
import java.awt.Rectangle;

import com.enderio.core.client.gui.button.ToggleButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.RenderUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.fluid.Fluids;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.machine.gui.GuiPoweredMachineBase;
import crazypants.enderio.machine.obelisk.weather.TileWeatherObelisk.WeatherTask;
import crazypants.enderio.network.PacketHandler;

public class GuiWeatherObelisk extends GuiPoweredMachineBase<TileWeatherObelisk> {

    private IconButton buttonStart;
    private ToggleButton buttonMode;

    public GuiWeatherObelisk(InventoryPlayer inventory, TileWeatherObelisk tileEntity) {
        super(tileEntity, new ContainerWeatherObelisk(inventory, tileEntity), "weatherObelisk");

        addProgressTooltip(79, 29, 18, 31);

        addToolTip(new GuiToolTip(new Rectangle(22, 11, 16, 63), "") {

            @Override
            protected void updateText() {
                text.clear();
                FluidTank tank = getTileEntity().getInputTank();
                String heading = EnderIO.lang.localize("tank.tank");
                if (tank.getFluid() != null) {
                    heading += ": " + tank.getFluid().getLocalizedName();
                }
                text.add(heading);
                text.add(Fluids.toCapactityString(getTileEntity().getInputTank()));
            }
        });
    }

    @Override
    public void initGui() {
        super.initGui();

        int x_mode = xSize - 5 - BUTTON_SIZE;
        int x_start = (xSize / 2) - (BUTTON_SIZE / 2);
        int y = 58;

        buttonMode = new ToggleButton(this, 1, x_mode, y, IconEIO.ROUND_ROBIN_OFF, IconEIO.REDSTONE_MODE_WITH_SIGNAL);

        buttonMode.setToolTip(EnderIO.lang.localize("gui.machine.weather.control"));
        buttonMode.setSelectedToolTip(EnderIO.lang.localize("gui.machine.weather.loop"));
        buttonMode.setUnselectedToolTip(EnderIO.lang.localize("gui.machine.weather.manual"));
        buttonMode.setSelected(getTileEntity().getPulseControl());
        addButton(buttonMode);
        buttonMode.onGuiInit();

        buttonStart = new IconButton(this, 0, x_start, y, IconEIO.TICK);
        buttonStart.setToolTip(EnderIO.lang.localize("gui.machine.weather.run"));

        addButton(buttonStart);
        buttonStart.onGuiInit();

        refreshButtons();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (getTileEntity().getWorldObj().getTotalWorldTime() % 20 == 0) {
            refreshButtons();
        }
    }

    private void refreshButtons() {
        boolean pulseControl = getTileEntity().getPulseControl();
        pulseControlAction(pulseControl);

        FluidStack fs = getTileEntity().getInputTank().getFluid();
        if (fs == null) {
            buttonStart.enabled = false;
            return;
        }
        WeatherTask task = WeatherTask.fromFluid(fs.getFluid());
        buttonStart.enabled = getTileEntity().canStartTask(task);
    }

    @Override
    protected boolean showRecipeButton() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        bindGuiTexture();

        this.drawTexturedModalRect(getGuiLeft(), getGuiTop(), 0, 0, getXSize(), getYSize());

        int x = getGuiLeft() + 22;
        int y = getGuiTop() + 11;
        RenderUtil.renderGuiTank(getTileEntity().getInputTank(), x, y, 0, 16, 63);

        bindGuiTexture();

        drawTexturedModalRect(x, y, 186, 33, 16, 63);

        if (shouldRenderProgress() && getTileEntity().getActiveTask() != null) {
            // TODO test
            int barHeight = getProgressScaled(ContainerWeatherObelisk.MAX_SCALE);
            Color color = getTileEntity().getActiveTask().color;
            GL11.glColor3f(
                    (float) color.getRed() / 255f,
                    (float) color.getGreen() / 255f,
                    (float) color.getBlue() / 255f);
            this.drawTexturedModalRect(
                    getGuiLeft() + 81,
                    getGuiTop() + 58 - barHeight,
                    getXSize(),
                    32 - barHeight,
                    12,
                    barHeight);
        }
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
    }

    @Override
    protected int getPowerHeight() {
        return 63;
    }

    @Override
    protected int getPowerU() {
        return super.getPowerU();
    }

    @Override
    protected int getPowerV() {
        return 33;
    }

    @Override
    protected int getPowerX() {
        return super.getPowerX() - 7;
    }

    @Override
    protected int getPowerY() {
        return super.getPowerY() - 3;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        super.actionPerformed(b);
         if (b.id == 0) {
             // Start Button
            getTileEntity().startTask();
            PacketHandler.INSTANCE.sendToServer(new PacketActivateWeather(getTileEntity(), true));
        } else if (b.id == 1) {
             // Control Mode Button
            boolean pulseControl = buttonMode.isSelected();
            getTileEntity().setPulseControl(pulseControl);
            pulseControlAction(pulseControl);
            PacketControlModeWeather packet = new PacketControlModeWeather(getTileEntity());
            PacketHandler.INSTANCE.sendToServer(packet);
        }
    }

    protected void pulseControlAction(boolean pulseControl) {
        redstoneButton.setVisible(!pulseControl);
        buttonStart.setVisible(!pulseControl);
    }
}
