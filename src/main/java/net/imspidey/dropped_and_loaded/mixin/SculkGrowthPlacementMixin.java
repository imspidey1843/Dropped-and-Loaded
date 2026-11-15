package net.imspidey.dropped_and_loaded.mixin;

import net.imspidey.dropped_and_loaded.init.DroppedAndLoadedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class SculkGrowthPlacementMixin {
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void droppedAndLoaded$placeSculkGrowth(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston,
            CallbackInfo ci
    ) {
        if (level.isClientSide || !state.is(Blocks.SCULK) || oldState.is(Blocks.SCULK))
            return;

        BlockPos growthPos = pos.above();
        BlockState growthState = DroppedAndLoadedModBlocks.SCULK_GROWTHS.get().defaultBlockState();
        if (level.isEmptyBlock(growthPos) && growthState.canSurvive(level, growthPos))
            level.setBlock(growthPos, growthState, Block.UPDATE_ALL);
    }
}
