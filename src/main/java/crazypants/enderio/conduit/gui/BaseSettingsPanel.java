package crazypants.enderio.conduit.gui;

import java.awt.*;
import java.util.List;

import com.enderio.core.client.gui.button.CheckBox;
import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.ITabPanel;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.MultiIconButton;
import com.enderio.core.client.render.ColorUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.packet.PacketConnectionMode;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.network.PacketHandler;


public class BaseSettingsPanel implements ITabPanel {

  static final int ID_INSERT_ENABLED = 327;
  static final int ID_EXTRACT_ENABLED = 328;
  protected static final int ID_INSERT_FILTER_OPTIONS = 329;
  protected static final int ID_EXTRACT_FILTER_OPTIONS = 330;
  protected final int ID_ENABLED = 331;

  protected final @Nonnull IWidgetIcon icon;
  protected final @Nonnull GuiExternalConnection gui;
  protected @Nonnull IConduit con;
  protected final @Nonnull String typeName;
  protected final @Nonnull ResourceLocation texture;

  protected ConnectionMode oldConnectionMode;

  private @Nonnull String inputHeading;
  private @Nonnull String outputHeading;

  private boolean insertEnabled = false;
  private boolean extractEnabled = false;

  private boolean enabled = false;

  private final @Nonnull CheckBox extractEnabledB;
  private final @Nonnull CheckBox insertEnabledB;

  private final @Nonnull CheckBox enabledB;

  private @Nonnull IconButton insertFilterOptionsB;
  private @Nonnull IconButton extractFilterOptionsB;
  private @Nonnull FakeButton functionUpgradeOptionsB;

  protected int left = 0;
  protected int top = 0;
  protected int width = 0;
  protected int height = 0;
  protected int rightColumn = 112;
  protected int leftColumn = 22;

  protected int gap = 5;

  protected int customTop = 0;

  private final @Nonnull GuiToolTip functionUpgradeTooltip;
  protected @Nonnull GuiToolTip filterExtractUpgradeTooltip;
  protected @Nonnull GuiToolTip filterInsertUpgradeTooltip;

  protected BaseSettingsPanel(@Nonnull IWidgetIcon icon, @Nonnull String typeName, @Nonnull GuiExternalConnection gui, @Nonnull IConduit con) {
    this(icon, typeName, gui, con, "externalConduitConnection");
  }

  protected BaseSettingsPanel(@Nonnull IWidgetIcon icon, @Nonnull String typeName, @Nonnull GuiExternalConnection gui, @Nonnull IConduit con,
                              @Nonnull String texture) {
    this.icon = icon;
    this.typeName = typeName;
    this.gui = gui;
    this.con = con;
    this.texture = EnderIO.proxy.getGuiTexture(texture);

    inputHeading = getInputHeading();
    outputHeading = getOutputHeading();

    FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

    customTop = top + gap * 5 + fr.FONT_HEIGHT * 2;
    customTop -= 16;

    int x = leftColumn;
    int y = 6;

    insertEnabledB = new CheckBox(gui, ID_INSERT_ENABLED, x, y);

    enabledB = new CheckBox(gui, ID_ENABLED, x, y);

    x = rightColumn;

    extractEnabledB = new CheckBox(gui, ID_EXTRACT_ENABLED, x, y);

    x = leftColumn;
    y = 92;

    insertFilterOptionsB = new IconButton(gui, ID_INSERT_FILTER_OPTIONS, x, y, IconEIO.IO_CONFIG_UP);
    insertFilterOptionsB.setToolTip(EnderIO.lang.localize("gui.edit_item_filter"));

    x = rightColumn;

    extractFilterOptionsB = new IconButton(gui, ID_EXTRACT_FILTER_OPTIONS, x, y, IconEIO.IO_CONFIG_UP);
    extractFilterOptionsB.setToolTip(EnderIO.lang.localize("gui.edit_item_filter"));

    x = rightColumn + 18 + 1;

    functionUpgradeOptionsB = new FakeButton(gui, x, y, IconEIO.RECIPE) {
      @Override
      public boolean isVisible() {
        return hasUpgrades() && super.isVisible();
      }
    };
    functionUpgradeOptionsB.setToolTip(new GuiToolTip(new Rectangle(x, y, 18, 18), "") {
      @Override
      public boolean shouldDraw() {
        return hasUpgrades() && super.shouldDraw();
      }

      @Override
      public List<String> getToolTipText() {
        return gui.getContainer().getFunctionUpgradeToolTipText();
      }
    });

    filterExtractUpgradeTooltip = new GuiToolTip(new Rectangle(rightColumn, 70, 18, 18),  EnderIO.lang.localize("gui.conduit.item.filterupgrade")) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFilterUpgrades(false) && super.shouldDraw();
      }
    };

    filterInsertUpgradeTooltip = new GuiToolTip(new Rectangle(leftColumn, 70, 18, 18), EnderIO.lang.localize("gui.conduit.item.filterupgrade")) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFilterUpgrades(true) && super.shouldDraw();
      }
    };

    functionUpgradeTooltip = new GuiToolTip(new Rectangle(rightColumn + 18, customTop + 43, 18, 18), EnderIO.lang.localize("gui.conduit.item.speedupgrade"),
      EnderIO.lang.localize("gui.conduit.item.speedupgrade2")) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFunctionUpgrades() && super.shouldDraw();
      }
    };

    gui.getContainer().setInoutSlotsVisible(false, false);

  }

  protected void updateFilterButtons() {
    if (gui.getContainer().hasFilterUpgrades(true) && hasFilterGui(true)) {
      insertFilterOptionsB.setVisible(true);
    } else {
      insertFilterOptionsB.setVisible(false);
    }

    if (gui.getContainer().hasFilterUpgrades(false) && hasFilterGui(false)) {
      extractFilterOptionsB.setVisible(true);
    } else {
      extractFilterOptionsB.setVisible(false);
    }
  }

  protected boolean hasFilterGui(boolean input) {
    return true;
  }

  public boolean updateConduit(@Nonnull IConduit conduit) {
    this.con = conduit;
    if (oldConnectionMode != con.getConnectionMode(gui.getDir())) {
      connectionModeChanged(con.getConnectionMode(gui.getDir()));
    }
    return true;
  }

  @Override
  public void onGuiInit(int leftIn, int topIn, int widthIn, int heightIn) {
    this.left = leftIn;
    this.top = topIn;
    this.width = widthIn;
    this.height = heightIn;

    updateConduit(con);

    if (hasInOutModes()) {
      insertEnabledB.onGuiInit();
      extractEnabledB.onGuiInit();

      insertEnabledB.setSelected(insertEnabled);
      extractEnabledB.setSelected(extractEnabled);
    } else {
      enabledB.onGuiInit();
      enabledB.setSelected(enabled);
    }

    if (hasFilters()) {
      gui.addToolTip(filterExtractUpgradeTooltip);
      gui.addToolTip(filterInsertUpgradeTooltip);
      insertFilterOptionsB.onGuiInit();
      extractFilterOptionsB.onGuiInit();
      insertFilterOptionsB.setVisible(false);
      extractFilterOptionsB.setVisible(false);
    }
    if (hasUpgrades()) {
      gui.addToolTip(functionUpgradeTooltip);
      functionUpgradeOptionsB.onGuiInit();
    }

    initCustomOptions();
  }

  protected boolean hasUpgrades() {
    return false;
  }

  protected void initCustomOptions() {
  }

  @Override
  public void deactivate() {
    insertEnabledB.detach();
    extractEnabledB.detach();
    insertFilterOptionsB.detach();
    extractFilterOptionsB.detach();
    functionUpgradeOptionsB.detach();

    gui.removeToolTip(functionUpgradeTooltip);
    gui.removeToolTip(filterExtractUpgradeTooltip);
    gui.removeToolTip(filterInsertUpgradeTooltip);
  }

  @Override
  public void mouseClicked(int x, int y, int par3) {
  }

  @Override
  public void keyTyped(char par1, int par2) {
  }

  @Override
  public void updateScreen() {
  }

  @Override
  @Nonnull
  public IWidgetIcon getIcon() {
    return icon;
  }

  private void updateConnectionMode() {
    ConnectionMode mode = ConnectionMode.DISABLED;
    if (insertEnabled && extractEnabled || enabled) {
      mode = ConnectionMode.IN_OUT;
    } else if (insertEnabled) {
      mode = ConnectionMode.OUTPUT;
    } else if (extractEnabled) {
      mode = ConnectionMode.INPUT;
    }
    con.setConnectionMode(gui.getDir(), mode);
    PacketHandler.INSTANCE.sendToServer(new PacketConnectionMode(con, gui.getDir()));
  }

  @Override
  public void actionPerformed(@Nonnull GuiButton guiButton) {
    if (guiButton.id == ID_INSERT_ENABLED) {
      insertEnabled = !insertEnabled;
      updateConnectionMode();
    } else if (guiButton.id == ID_EXTRACT_ENABLED) {
      extractEnabled = !extractEnabled;
      updateConnectionMode();
    } else if (guiButton.id == ID_ENABLED) {
      enabled = !enabled;
      updateConnectionMode();
    }
  }

  protected void connectionModeChanged(@Nonnull ConnectionMode mode) {
    oldConnectionMode = mode;
    if (hasInOutModes()) {
      insertEnabled = mode.acceptsOutput();
      extractEnabled = mode.acceptsInput();
    } else {
      enabled = mode == ConnectionMode.IN_OUT;
    }
  }

  @Override
  public void render(float par1, int par2, int par3) {
    updateFilterButtons();

    FontRenderer fr = gui.getFontRenderer();

    int rgb = ColorUtil.getRGB(Color.darkGray);
    int x = left + 32;
    int y = gui.getGuiTop() + 10;

    if (hasInOutModes()) {
      fr.drawString(inputHeading, x, y, rgb);
      x += 92;
      fr.drawString(outputHeading, x, y, rgb);
    } else {
      String heading = enabled ? getEnabledHeading() : getDisabledHeading();
      fr.drawString(heading, x, y, rgb);
    }
    renderCustomOptions(y + gap + fr.FONT_HEIGHT + gap, par1, par2, par3);
  }

  protected void renderCustomOptions(int topIn, float par1, int par2, int par3) {

  }

  protected @Nonnull String getTypeName() {
    return typeName;
  }

  protected boolean hasFilters() {
    return false;
  }

  @Nonnull
  protected String getInputHeading() {
    return EnderIO.lang.localize("gui.conduit.ioMode.input");
  }

  @Nonnull
  protected String getOutputHeading() {
    return EnderIO.lang.localize("gui.conduit.ioMode.output");
  }

  @Nonnull
  protected String getEnabledHeading() {
    return EnderIO.lang.localize("gui.enabled");
  }

  @Nonnull
  protected String getDisabledHeading() {
    return EnderIO.lang.localize("gui.disabled");
  }

  protected boolean hasInOutModes() {
    return true;
  }

}
