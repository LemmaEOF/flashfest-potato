package gay.lemmaeof.tatervator.hooks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface SubGridEngine {
    boolean shouldBeCounted(BlockState state, Direction captureDir);
}
