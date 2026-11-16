package crazypants.enderio.betterbuilderswands;

import java.util.function.BiFunction;
import java.util.function.IntFunction;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.github.bsideup.jabel.Desugar;

import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.painter.BlockPaintedCarpet;
import crazypants.enderio.machine.painter.BlockPaintedFence;
import crazypants.enderio.machine.painter.BlockPaintedFenceGate;
import crazypants.enderio.machine.painter.BlockPaintedGlowstone;
import crazypants.enderio.machine.painter.BlockPaintedSlab;
import crazypants.enderio.machine.painter.BlockPaintedStair;
import crazypants.enderio.machine.painter.BlockPaintedWall;
import crazypants.enderio.machine.painter.TileEntityPaintedBlock;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.conversion.ICustomMapping;
import portablejim.bbw.shims.IPlayerShim;
import portablejim.bbw.shims.IWorldShim;

@Desugar
public record PaintedCustomMapping(Block lookBlock, BiFunction<Block, Integer, ItemStack> itemStackGetter, int meta)
        implements ICustomMapping {

    @Override
    public Block getLookBlock() {
        return lookBlock;
    }

    @Override
    public int getMeta() {
        return meta;
    }

    @Override
    public ItemStack getItems(IWorldShim world, IPlayerShim player, Point3d point) {
        if (world.getTile(point) instanceof TileEntityPaintedBlock painted) {
            return itemStackGetter.apply(painted.getSourceBlock(), painted.getSourceBlockMetadata());
        }
        return null;
    }

    @Override
    public boolean shouldCopyTileNBT() {
        return true;
    }

    public static void registerMappings() {
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedGlowstone,
                        BlockPaintedGlowstone::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedStair,
                        BlockPaintedStair::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedFence,
                        BlockPaintedFence::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedFenceGate,
                        BlockPaintedFenceGate::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedCarpet,
                        BlockPaintedCarpet::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedSlab,
                        BlockPaintedSlab::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedWall,
                        BlockPaintedWall::createItemStackForSourceBlock,
                        i));
        register(
                i -> new PaintedCustomMapping(
                        EnderIO.blockPaintedDoubleSlab,
                        BlockPaintedSlab::createItemStackForSourceBlock,
                        i));
    }

    private static void register(IntFunction<ICustomMapping> mapping) {
        for (int i = 0; i < 16; i++) {
            BetterBuildersWandsMod.instance.mappingManager.setMapping(mapping.apply(i));
        }
    }
}
