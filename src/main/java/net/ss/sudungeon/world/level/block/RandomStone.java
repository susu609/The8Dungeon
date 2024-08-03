package net.ss.sudungeon.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.ss.sudungeon.init.SsModGameRules;
import org.jetbrains.annotations.NotNull;

public class RandomStone extends Block {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public static final BooleanProperty TRANSFORMED = BooleanProperty.create("transformed"); // Thuộc tính mới

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
        super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(-1, 3600000));
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.NORMAL).setValue(TRANSFORMED, false));
        // Đặt giá trị mặc định của TRANSFORMED là false
    }

    @Override
    protected void createBlockStateDefinition (StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, TRANSFORMED);
    }

    @Override
    public int getLightBlock (BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 15;
    }

    @Override
    public void onPlace (BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        if (!world.getGameRules().getBoolean(SsModGameRules.RULE_IS_TEMPLATE) && !blockstate.getValue(TRANSFORMED)) {
            transformBlock(blockstate, world, pos, oldState);


        }
    }

    private void transformBlock (BlockState blockstate, Level world, BlockPos pos, BlockState oldState) {
        RandomSource random = world.getRandom();
        int randomVariant = random.nextInt(3);
        BlockState newState = blockstate.setValue(VARIANT, Variant.values()[randomVariant]).setValue(TRANSFORMED, true);
        world.setBlock(pos, newState, 3);
        world.sendBlockUpdated(pos, oldState, newState, 3);
    }

}

