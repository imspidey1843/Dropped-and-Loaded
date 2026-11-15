package net.imspidey.dropped_and_loaded.mixin;

import net.imspidey.dropped_and_loaded.init.DroppedAndLoadedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkVeinBlock.class)
public class SculkGrowthPlacementMixin {
    @Unique
    private int droppedAndLoaded$existingSculkMask;

    @Inject(method = "attemptPlaceSculk", at = @At("HEAD"), remap = false)
    private void droppedAndLoaded$captureNeighbors(
            SculkSpreader spreader,
            LevelAccessor level,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        droppedAndLoaded$existingSculkMask = 0;
        for (Direction direction : Direction.values())
            if (level.getBlockState(pos.relative(direction)).is(Blocks.SCULK))
                droppedAndLoaded$existingSculkMask |= 1 << direction.ordinal();
    }

    @Inject(method = "attemptPlaceSculk", at = @At("RETURN"), remap = false)
    private void droppedAndLoaded$placeSculkGrowth(
            SculkSpreader spreader,
            LevelAccessor level,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (spreader.isWorldGeneration() || !Boolean.TRUE.equals(cir.getReturnValue()))
            return;
        for (Direction direction : Direction.values()) {
            if ((droppedAndLoaded$existingSculkMask & (1 << direction.ordinal())) != 0)
                continue;
            BlockPos sculkPos = pos.relative(direction);
            if (!level.getBlockState(sculkPos).is(Blocks.SCULK))
                continue;
            float roll = random.nextFloat();
            if (roll < 0.10F && droppedAndLoaded$tryPlaceOvergrownGrowths(level, sculkPos))
                return;
            if (roll < 0.30F)
                droppedAndLoaded$tryPlaceSmallGrowths(level, sculkPos);
            return;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private boolean droppedAndLoaded$hasReplaceableSpace(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced();
    }

    @SuppressWarnings("UnusedReturnValue")
    @Unique
    private boolean droppedAndLoaded$tryPlaceSmallGrowths(LevelAccessor level, BlockPos pos) {
        System.out.println("SPREAD SMALL");
        BlockPos growthPos = pos.above();
        BlockState growth = DroppedAndLoadedModBlocks.SCULK_GROWTHS.get().defaultBlockState();
        if (!droppedAndLoaded$hasReplaceableSpace(level, growthPos) || !growth.canSurvive(level, growthPos))
            return false;
        return level.setBlock(growthPos, growth, Block.UPDATE_ALL);
    }

    @Unique
    private boolean droppedAndLoaded$tryPlaceOvergrownGrowths(LevelAccessor level, BlockPos pos) {
        System.out.println("SPREAD LARGE");
        BlockPos growthPos = pos.above();
        BlockState growth = DroppedAndLoadedModBlocks.OVERGROWN_SCULK_GROWTHS.get().defaultBlockState();
        BlockState lower = growth.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        if (
                !droppedAndLoaded$hasReplaceableSpace(level, growthPos) ||
                !droppedAndLoaded$hasReplaceableSpace(level, growthPos.above()) ||
                !lower.canSurvive(level, growthPos)
        )
            return false;
        DoublePlantBlock.placeAt(level, growth, growthPos, Block.UPDATE_ALL);
        return level.getBlockState(growthPos).is(growth.getBlock());
    }
}
