package gay.lemmaeof.tatervator.block;

import com.mojang.serialization.MapCodec;
import gay.lemmaeof.tatervator.hooks.SubGridEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.grid.FlyingTickable;
import net.minecraft.world.grid.GridCarrier;
import net.minecraft.world.grid.SubGridBlocks;
import net.minecraft.world.grid.SubGridCapture;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class TatervatorBlock extends Block implements FlyingTickable, SubGridEngine {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final MapCodec<TatervatorBlock> CODEC = simpleCodec(TatervatorBlock::new);

    public TatervatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<TatervatorBlock> codec() {
        return TatervatorBlock.CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        boolean powered = shouldTriggerNoQC(DoorBlock.shouldTrigger(level, blockPos), level, blockPos);
        boolean isPowered = blockState.getValue(TRIGGERED);
        if (powered != isPowered) {
            if (powered) {
                level.scheduleTick(blockPos, this, 1);
                level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
            } else {
                level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(false)), 2);
            }
        }
    }

    private static boolean shouldTriggerNoQC(boolean powered, Level level, BlockPos pos) {
        return powered && level.hasNeighborSignal(pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING);
        SubGridCapture capture = SubGridCapture.scan(world, pos, direction);
        int power = world.getBestNeighborSignal(pos);
        if (capture != null) {
            GridCarrier carrier = new GridCarrier(EntityType.GRID_CARRIER, world);
            BlockPos capturePos = capture.minPos();
            carrier.moveTo(capturePos.getX(), capturePos.getY(), capturePos.getZ());
            carrier.grid().setBlocks(capture.blocks());
            carrier.grid().setBiome(world.getBiome(pos));
            carrier.setMovement(power % 2 == 0? Direction.DOWN : Direction.UP, (float)capture.engines() * 0.1F);
            capture.remove(world);
            world.addFreshEntity(carrier);
        }
    }

    @Override
    public void flyingTick(Level level, SubGridBlocks subGridBlocks, BlockState blockState, BlockPos blockPos, Vec3 vec3, Direction direction) {
        if (level.isClientSide) {
            Direction direction2 = blockState.getValue(FACING);
            if (direction == direction2 && blockState.getValue(TRIGGERED) && level.getRandom().nextBoolean()) {
                Direction direction3 = direction2.getOpposite();
                if (subGridBlocks.getBlockState(blockPos.relative(direction3)).isAir()) {
                    double d = 0.5;
                    vec3 = vec3.add(0.5, 0.5, 0.5).add((double)direction3.getStepX() * 0.5, (double)direction3.getStepY() * 0.5, (double)direction3.getStepZ() * 0.5);
                    level.addParticle(ParticleTypes.CLOUD, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public boolean shouldBeCounted(BlockState state, Direction captureDir) {
        return state.getValue(TRIGGERED);
    }
}
