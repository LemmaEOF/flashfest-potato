package gay.lemmaeof.tatervator.mixin;

import gay.lemmaeof.tatervator.Tatervator;
import gay.lemmaeof.tatervator.hooks.SubGridEngine;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.grid.SubGridBlocks;
import net.minecraft.world.grid.SubGridCapture;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SubGridCapture.class)
public class MixinSubGridCapture {
    private static final ThreadLocal<Integer> engineLocal = new ThreadLocal<>();

    @Inject(method = "scan", at = @At("HEAD"))
    private static void startScan(CallbackInfoReturnable<SubGridCapture> info) {
        engineLocal.set(0);
    }

    @Inject(method = "scan", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void hookEngines(Level level, BlockPos blockPos, Direction direction, CallbackInfoReturnable<SubGridCapture> info,
                                    Long2ObjectMap<BlockState> states, LongArrayFIFOQueue queue, int minX, int minY, int minZ,
                                    int maxX, int maxY, int maxZ, BlockPos.MutableBlockPos mut1,
                                    int width, int height, int depth, SubGridBlocks blocks, int engines,
                                    ObjectIterator<BlockState> iterator, Long2ObjectMap.Entry<BlockState> entry, BlockState targetState) {
        if (targetState.getBlock() instanceof SubGridEngine engine) {
            if (engine.shouldBeCounted(targetState, direction)) {
                engineLocal.set(engineLocal.get() + 1);
            }
        }
    }

    @ModifyArg(method = "scan", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/grid/SubGridCapture;<init>(Lnet/minecraft/world/grid/SubGridBlocks;Lit/unimi/dsi/fastutil/longs/LongSet;Lnet/minecraft/core/BlockPos;I)V"), index = 3)
    private static int boostEngines(int orig) {
        return orig + engineLocal.get();
    }

    @Inject(method = "isConnected", at = @At("HEAD"), cancellable = true)
    private static void injectNonStickyBlocks(Direction dir, VoxelShape shape1, VoxelShape shape2, BlockState state1, BlockState state2, CallbackInfoReturnable<Boolean> info) {
        if (state1.is(Tatervator.NON_STICKY) || state2.is(Tatervator.NON_STICKY)) info.setReturnValue(false);
    }
}
