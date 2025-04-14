package net.ss.sudungeon.network;

import net.minecraft.world.item.Items;
import net.ss.sudungeon.SsMod;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModVariables {

    // ----- LOẠI BIẾN CHÍNH -----
    // BlockState → chỉ trạng thái khối (AIR, STONE, v.v.)
    // Direction  → hướng (NORTH, EAST, ...)
    // ItemStack  → vật phẩm (dùng copy() khi set để tránh tham chiếu)
    // boolean    → giá trị logic: true/false
    // double     → số thực
    // String     → chuỗi văn bản

    // Biến toàn cục tạm thời — không cần sync, reset khi restart server
/*    public static BlockState blockstate_GS = Blocks.AIR.defaultBlockState();
    public static Direction direction_GS = Direction.NORTH;
    public static ItemStack itemstack_GS = ItemStack.EMPTY;
    public static boolean logic_GS = true;
    public static double number_GS = 0;
    public static String string_GS = "\"\"";*/

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        SsMod.addNetworkMessage(SavedDataSyncMessage.class, SavedDataSyncMessage::buffer, SavedDataSyncMessage::new, SavedDataSyncMessage::handler);
        SsMod.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
    }

    @SubscribeEvent
    public static void init(RegisterCapabilitiesEvent event) {
        event.register(PlayerVariables.class);
    }

    @Mod.EventBusSubscriber
    public static class EventBusVariableHandlers {
        @SubscribeEvent
        public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void clonePlayer(PlayerEvent.Clone event) {
            event.getOriginal().revive();
            PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
            PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
/*            clone.blockstate_PP = original.blockstate_PP;
            clone.direction_PP = original.direction_PP;
            clone.itemstack_PP = original.itemstack_PP;
            clone.logic_PP = original.logic_PP;
            clone.number_PP = original.number_PP;
            clone.string_PP = original.string_PP;*/
            clone.character = original.character;
            clone.isSlim = original.isSlim;
            clone.isMorphed = original.isMorphed;
            clone.isAttacking = original.isAttacking;
            clone.right_hand = original.right_hand;
            clone.left_hand = original.left_hand;
            clone.critChance = original.critChance;
            clone.attack = original.attack;
            clone.defense = original.defense;
            clone.speed = original.speed;
            clone.health = original.health;
            clone.mana = original.mana;

            if (!event.isWasDeath()) {
/*                clone.blockstate_PL = original.blockstate_PL;
                clone.direction_PL = original.direction_PL;
                clone.itemstack_PL = original.itemstack_PL;
                clone.logic_PL = original.logic_PL;
                clone.number_PL = original.number_PL;
                clone.string_PL = original.string_PL;*/
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                SavedData mapdata = MapVariables.get(event.getEntity().level());
                SavedData worlddata = WorldVariables.get(event.getEntity().level());
                if (mapdata != null)
                    SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(0, mapdata));
                if (worlddata != null)
                    SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(1, worlddata));
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                SavedData worlddata = WorldVariables.get(event.getEntity().level());
                if (worlddata != null)
                    SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(1, worlddata));
            }
        }
    }

    public static class WorldVariables extends SavedData {
        public static final String DATA_NAME = "ss_worldvars";
        // Lưu cho từng dimension riêng biệt (thế giới Nether, Overworld riêng nhau)
        // Sử dụng: SsModVariables.WorldVariables.get(world)

/*        public BlockState blockstate_GW = Blocks.AIR.defaultBlockState();
        public Direction direction_GW = Direction.SOUTH;
        public ItemStack itemstack_GW = ItemStack.EMPTY;
        public boolean logic_GW = false;
        public double number_GW = 0;
        public String string_GW = "\"\"";*/

        public static WorldVariables load(CompoundTag tag) {
            WorldVariables data = new WorldVariables();
            data.read(tag);
            return data;
        }

        public void read(CompoundTag nbt) {
/*            blockstate_GW = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockstate_GW"));
            direction_GW = Direction.from3DDataValue(nbt.getInt("direction_GW"));
            itemstack_GW = ItemStack.of(nbt.getCompound("itemstack_GW"));
            logic_GW = nbt.getBoolean("logic_GW");
            number_GW = nbt.getDouble("number_GW");
            string_GW = nbt.getString("string_GW");*/
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
/*            nbt.put("blockstate_GW", NbtUtils.writeBlockState(blockstate_GW));
            nbt.putInt("direction_GW", direction_GW.get3DDataValue());
            nbt.put("itemstack_GW", itemstack_GW.save(new CompoundTag()));
            nbt.putBoolean("logic_GW", logic_GW);
            nbt.putDouble("number_GW", number_GW);
            nbt.putString("string_GW", string_GW);*/
            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level level && !level.isClientSide())
                SsMod.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new SavedDataSyncMessage(1, this));
        }

        static WorldVariables clientSide = new WorldVariables();

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(WorldVariables::load, WorldVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "ss_mapvars";
        // Dùng lưu các biến toàn map (có sync toàn server)
        // Sử dụng qua: SsModVariables.MapVariables.get(world)

/*         public BlockState blockstate_GM = Blocks.AIR.defaultBlockState();
        public Direction direction_GM = Direction.NORTH;
        public ItemStack itemstack_GM = ItemStack.EMPTY;
        public boolean logic_GM = false;
        public double number_GM = 1.0;
        public String string_GM = "\"\"";*/
        public int enemyCount;

        public static MapVariables load(CompoundTag tag) {
            MapVariables data = new MapVariables();
            data.read(tag);
            return data;
        }

        public void read(CompoundTag nbt) {
/*            blockstate_GM = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockstate_GM"));
            direction_GM = Direction.from3DDataValue(nbt.getInt("direction_GM"));
            itemstack_GM = ItemStack.of(nbt.getCompound("itemstack_GM"));
            logic_GM = nbt.getBoolean("logic_GM");
            number_GM = nbt.getDouble("number_GM");
            string_GM = nbt.getString("string_GM");*/
            enemyCount = nbt.getInt("enemyCount");
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag nbt) {
/*            nbt.put("blockstate_GM", NbtUtils.writeBlockState(blockstate_GM));
            nbt.putInt("direction_GM", direction_GM.get3DDataValue());
            nbt.put("itemstack_GM", itemstack_GM.save(new CompoundTag()));
            nbt.putBoolean("logic_GM", logic_GM);
            nbt.putDouble("number_GM", number_GM);
            nbt.putString("string_GM", string_GM);*/
            nbt.putInt("enemyCount",enemyCount);
            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level && !world.isClientSide())
                SsMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SavedDataSyncMessage(0, this));
        }

        static MapVariables clientSide = new MapVariables();

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return Objects.requireNonNull(serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(e -> MapVariables.load(e), MapVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

    public static class SavedDataSyncMessage {
        private final int type;
        private SavedData data;

        public SavedDataSyncMessage(FriendlyByteBuf buffer) {
            this.type = buffer.readInt();
            CompoundTag nbt = buffer.readNbt();
            if (nbt != null) {
                this.data = this.type == 0 ? new MapVariables() : new WorldVariables();
                if (this.data instanceof MapVariables mapVariables)
                    mapVariables.read(nbt);
                else if (this.data instanceof WorldVariables worldVariables)
                    worldVariables.read(nbt);
            }
        }

        public SavedDataSyncMessage(int type, SavedData data) {
            this.type = type;
            this.data = data;
        }

        public static void buffer(SavedDataSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.type);
            if (message.data != null)
                buffer.writeNbt(message.data.save(new CompoundTag()));
        }

        public static void handler(SavedDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (!context.getDirection().getReceptionSide().isServer() && message.data != null) {
                    if (message.type == 0)
                        MapVariables.clientSide = (MapVariables) message.data;
                    else
                        WorldVariables.clientSide = (WorldVariables) message.data;
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
    });

    @Mod.EventBusSubscriber
    private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
                event.addCapability(new ResourceLocation("ss", "player_variables"), new PlayerVariablesProvider());
        }

        private final PlayerVariables playerVariables = new PlayerVariables();
        private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
            return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT() {
            return playerVariables.writeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            playerVariables.readNBT(nbt);
        }
    }

    public static class PlayerVariables {
        // _PL: Mất khi chết (Life-time)
        // _PP: Giữ lại sau khi chết (Persistent)
/*        public BlockState blockstate_PL, blockstate_PP;
        public Direction direction_PL, direction_PP;
        public ItemStack itemstack_PL, itemstack_PP;
        public boolean logic_PL, logic_PP;
        public double number_PL, number_PP;
        public String string_PL, string_PP;*/

        // Character info
        public String character = "steve"; // mặc định
        public boolean isSlim = false; // mặc định
        public boolean isMorphed;
        public boolean isAttacking;
        public ItemStack right_hand = new ItemStack(Items.WOODEN_SWORD);
        public ItemStack left_hand = ItemStack.EMPTY;
        // RPG Stats (PP = giữ sau khi chết)
        public double critChance = 0.1;
        public double attack = 5;
        public double defense = 2;
        public double speed = 1;
        public double health = 20;
        public double mana = 100;



        public void syncPlayerVariables(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer)
                SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
        }

        public Tag writeNBT() {
            CompoundTag nbt = new CompoundTag();
/*            nbt.put("blockstate_PL", NbtUtils.writeBlockState(blockstate_PL));
            nbt.put("blockstate_PP", NbtUtils.writeBlockState(blockstate_PP));
            nbt.putInt("direction_PL", direction_PL.get3DDataValue());
            nbt.putInt("direction_PP", direction_PP.get3DDataValue());
            nbt.put("itemstack_PL", itemstack_PL.save(new CompoundTag()));
            nbt.put("itemstack_PP", itemstack_PP.save(new CompoundTag()));
            nbt.putBoolean("logic_PL", logic_PL);
            nbt.putBoolean("logic_PP", logic_PP);
            nbt.putDouble("number_PL", number_PL);
            nbt.putDouble("number_PP", number_PP);
            nbt.putString("string_PL", string_PL);
            nbt.putString("string_PP", string_PP);*/
            nbt.putString("character", character);
            nbt.putBoolean("isSlim", isSlim);
            nbt.putBoolean("isMorphed", isMorphed);
            nbt.putBoolean("isAttacking", isAttacking);
            nbt.put("left_hand", left_hand.save(new CompoundTag()));
            nbt.put("right_hand", right_hand.save(new CompoundTag()));
            nbt.putDouble("critChance", critChance);
            nbt.putDouble("attack", attack);
            nbt.putDouble("defense", defense);
            nbt.putDouble("speed", speed);
            nbt.putDouble("health", health);
            nbt.putDouble("mana", mana);

            return nbt;
        }

        public void readNBT(Tag tag) {
            CompoundTag nbt = (CompoundTag) tag;
/*            blockstate_PL = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockstate_PL"));
            blockstate_PP = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockstate_PP"));
            direction_PL = Direction.from3DDataValue(nbt.getInt("direction_PL"));
            direction_PP = Direction.from3DDataValue(nbt.getInt("direction_PP"));
            itemstack_PL = ItemStack.of(nbt.getCompound("itemstack_PL"));
            itemstack_PP = ItemStack.of(nbt.getCompound("itemstack_PP"));
            logic_PL = nbt.getBoolean("logic_PL");
            logic_PP = nbt.getBoolean("logic_PP");
            number_PL = nbt.getDouble("number_PL");
            number_PP = nbt.getDouble("number_PP");
            string_PL = nbt.getString("string_PL");
            string_PP = nbt.getString("string_PP");*/
            character = nbt.getString("character");
            isSlim = nbt.getBoolean("isSlim");
            isMorphed = nbt.getBoolean("isMorphed");
            isAttacking = nbt.getBoolean("isAttacking");
            left_hand = ItemStack.of(nbt.getCompound("left_hand"));
            right_hand = ItemStack.of(nbt.getCompound("right_hand"));
            critChance = nbt.getDouble("critChance");
            attack = nbt.getDouble("attack");
            defense = nbt.getDouble("defense");
            speed = nbt.getDouble("speed");
            health = nbt.getDouble("health");
            mana = nbt.getDouble("mana");

        }
    }

    public static class PlayerVariablesSyncMessage {
        private final PlayerVariables data;

        public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
            this.data = new PlayerVariables();
            this.data.readNBT(buffer.readNbt());
        }

        public PlayerVariablesSyncMessage(PlayerVariables data) {
            this.data = data;
        }

        public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeNbt((CompoundTag) message.data.writeNBT());
        }

        public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (!context.getDirection().getReceptionSide().isServer()) {
                    assert Minecraft.getInstance().player != null;
                    PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
/*                    variables.blockstate_PL = message.data.blockstate_PL;
                    variables.blockstate_PP = message.data.blockstate_PP;
                    variables.direction_PL = message.data.direction_PL;
                    variables.direction_PP = message.data.direction_PP;
                    variables.itemstack_PL = message.data.itemstack_PL;
                    variables.itemstack_PP = message.data.itemstack_PP;
                    variables.logic_PL = message.data.logic_PL;
                    variables.logic_PP = message.data.logic_PP;
                    variables.number_PL = message.data.number_PL;
                    variables.number_PP = message.data.number_PP;
                    variables.string_PL = message.data.string_PL;
                    variables.string_PP = message.data.string_PP;*/

                    variables.character = message.data.character;
                    variables.isSlim = message.data.isSlim;
                    variables.isMorphed = message.data.isMorphed;
                    variables.isAttacking = message.data.isAttacking;
                    variables.left_hand = message.data.left_hand;
                    variables.right_hand = message.data.right_hand;
                    variables.critChance = message.data.critChance;
                    variables.attack = message.data.attack;
                    variables.defense = message.data.defense;
                    variables.speed = message.data.speed;
                    variables.health = message.data.health;
                    variables.mana = message.data.mana;
                }
            });
            context.setPacketHandled(true);
        }
    }
}
