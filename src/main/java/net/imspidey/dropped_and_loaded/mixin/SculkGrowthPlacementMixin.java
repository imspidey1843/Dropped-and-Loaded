package net.imspidey.dropped_and_loaded.mixin;

import net.imspidey.dropped_and_loaded.init.DroppedAndLoadedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class SculkGrowthPlacementMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onPlace", at = @At("TAIL"), remap = false)
    private void droppedAndLoaded$placeSculkGrowth(
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston,
            CallbackInfo ci
    ) {
        BlockState placedState = (BlockState) (Object) this;
        if (level.isClientSide || !placedState.is(Blocks.SCULK) || oldState.is(Blocks.SCULK))
            return;

        float roll = level.getRandom().nextFloat();
        if (roll < 0.1f && droppedAndLoaded$tryPlaceOvergrownGrowths(level, pos))
            return;
        if (roll < 0.3f)
            droppedAndLoaded$tryPlaceSmallGrowths(level, pos);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private boolean droppedAndLoaded$hasReplaceableSpace(Level level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced();
    }

    @SuppressWarnings("UnusedReturnValue")
    @Unique
    private boolean droppedAndLoaded$tryPlaceSmallGrowths(Level level, BlockPos pos) {
        BlockPos growthPos = pos.above();
        BlockState growth = DroppedAndLoadedModBlocks.SCULK_GROWTHS.get().defaultBlockState();
        if (!droppedAndLoaded$hasReplaceableSpace(level, pos) || !growth.canSurvive(level, growthPos))
            return false;
        return level.setBlock(growthPos, growth, Block.UPDATE_ALL);
    }

    @Unique
    private boolean droppedAndLoaded$tryPlaceOvergrownGrowths(Level level, BlockPos pos) {
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
