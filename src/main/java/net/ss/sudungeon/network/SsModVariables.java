package net.ss.sudungeon.network;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.packet.DungeonDataPacket;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModVariables {
    @SubscribeEvent
    public static void init (FMLCommonSetupEvent event) {
        SsMod.addNetworkMessage(SavedDataSyncMessage.class, SavedDataSyncMessage::buffer, SavedDataSyncMessage::new, SavedDataSyncMessage::handler);
        SsMod.addNetworkMessage(DungeonDataPacket.class, DungeonDataPacket::encode, DungeonDataPacket::new, DungeonDataPacket::handle);
    }

    @Mod.EventBusSubscriber
    public static class EventBusVariableHandlers {
        @SubscribeEvent
        public static void onPlayerLoggedIn (PlayerEvent.PlayerLoggedInEvent event) {
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
        public static void onPlayerChangedDimension (PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                SavedData worlddata = WorldVariables.get(event.getEntity().level());
                if (worlddata != null)
                    SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SavedDataSyncMessage(1, worlddata));
            }
        }
    }

    public static class WorldVariables extends SavedData {
        public static final String DATA_NAME = "ss_worldvars";
        public double GLOBAL_WORLD = 0.5;
        public int challengePoints = 0;
        public int maxChallengePoints = 20;
        public List<RoomData> roomDataList = new ArrayList<>(); // Danh sách phòng


        public static WorldVariables load (CompoundTag tag) {
            WorldVariables data = new WorldVariables();
            data.read(tag);
            return data;
        }

        public void read (CompoundTag nbt) {
            GLOBAL_WORLD = nbt.getDouble("GLOBAL_WORLD");
            challengePoints = nbt.getInt("challengePoints");
            maxChallengePoints = nbt.getInt("maxChallengePoints");
        }

        @Override
        public @NotNull CompoundTag save (CompoundTag nbt) {
            nbt.putDouble("GLOBAL_WORLD", GLOBAL_WORLD);
            nbt.putInt("challengePoints", challengePoints);
            nbt.putInt("maxChallengePoints", maxChallengePoints);
            return nbt;
        }

        public void syncData (LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level level && !level.isClientSide())
                SsMod.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new SavedDataSyncMessage(1, this));
        }

        static WorldVariables clientSide = new WorldVariables();

        public static WorldVariables get (LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(WorldVariables::load, WorldVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }

    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "ss_mapvars";
        public BlockState blockstate = Blocks.AIR.defaultBlockState();
        public Direction direction = Direction.DOWN;
        public ItemStack itemstack = ItemStack.EMPTY;
        public boolean logic = false;
        public double number = 0;
        public String string = "\"\"";

        public static MapVariables load (CompoundTag tag) {
            MapVariables data = new MapVariables();
            data.read(tag);
            return data;
        }

        public void read (CompoundTag nbt) {
            blockstate = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockstate"));
            direction = Direction.from3DDataValue(nbt.getInt("direction"));
            itemstack = ItemStack.of(nbt.getCompound("itemstack"));
            logic = nbt.getBoolean("logic");
            number = nbt.getDouble("number");
            string = nbt.getString("string");
        }

        @Override
        public @NotNull CompoundTag save (CompoundTag nbt) {
            nbt.put("blockstate", NbtUtils.writeBlockState(blockstate));
            nbt.putInt("direction", direction.get3DDataValue());
            nbt.put("itemstack", itemstack.save(new CompoundTag()));
            nbt.putBoolean("logic", logic);
            nbt.putDouble("number", number);
            nbt.putString("string", string);
            return nbt;
        }

        public void syncData (LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level && !world.isClientSide())
                SsMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SavedDataSyncMessage(0, this));
        }

        static MapVariables clientSide = new MapVariables();

        public static MapVariables get (LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return Objects.requireNonNull(serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(MapVariables::load, MapVariables::new, DATA_NAME);
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
                ServerLevel level = null; // Biến tạm
                this.data = this.type == 0 ? new MapVariables() : new WorldVariables();
                if (this.data instanceof MapVariables mapVariables) {
                    mapVariables.read(nbt);
                } else if (this.data instanceof WorldVariables worldVariables) {
                    worldVariables.read(nbt);
                }
            }
        }

        public SavedDataSyncMessage (int type, SavedData data) {
            this.type = type;
            this.data = data;
        }

        public static void buffer (SavedDataSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.type);
            if (message.data != null)
                buffer.writeNbt(message.data.save(new CompoundTag()));
        }

        public static void handler (SavedDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
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
}

