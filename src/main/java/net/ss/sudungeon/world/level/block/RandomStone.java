package net.ss.sudungeon.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.ss.sudungeon.init.SsModGameRules;
import org.jetbrains.annotations.NotNull;

public class RandomStone extends Block {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
    public static final BooleanProperty TRANSFORMED = BooleanProperty.create("transformed");

    public enum Variant implements StringRepresentable {
        NORMAL("normal"), CRACKED("cracked"), MOSSY("mossy");

        private final String name;

        Variant (String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName () {
            return name;
        }
    }

    public RandomStone () {
        super(Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(-1, 3600000));
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.NORMAL).setValue(TRANSFORMED, false));
    }

    @Override
    protected void createBlockStateDefinition (StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, TRANSFORMED);
    }

    @Override
    public int getLightBlock (@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    public void onPlace (@NotNull BlockState blockstate, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        if (!world.getGameRules().getBoolean(SsModGameRules.RULE_IS_TEMPLATE) && !blockstate.getValue(TRANSFORMED)) {
            transformBlock(blockstate, world, pos);
        }
    }

    private void transformBlock (BlockState blockstate, Level world, BlockPos pos) {
        RandomSource random = world.getRandom();
        int randomVariant = random.nextInt(Variant.values().length);
        BlockState newState = blockstate.setValue(VARIANT, Variant.values()[randomVariant]).setValue(TRANSFORMED, true);

        // Đảm bảo chỉ cập nhật khi trạng thái khối thay đổi
        if (!newState.equals(blockstate)) {
            world.setBlock(pos, newState, 3);
            world.sendBlockUpdated(pos, blockstate, newState, 3);
        }
    }
}
