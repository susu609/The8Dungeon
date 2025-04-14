package net.ss.sudungeon.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;

import java.util.EnumSet;
import java.util.HashMap;

public class DungeonStructureManager {

    public static void generateArena (ServerLevel world) {
        BlockPos startPos = new BlockPos(-32, 1, -32);
        Log.i("▶ Bắt đầu tạo Arena tại: " + startPos);

        // Đặt 4 phòng kiểu arena
        placeStructure(world, "1", startPos);
        placeStructure(world, "2", startPos.offset(32, 0, 0));
        placeStructure(world, "3", startPos.offset(0, 0, 32));
        placeStructure(world, "4", startPos.offset(32, 0, 32));

        // ✅ Thêm RoomData START để cho DungeonCommand biết phòng bắt đầu
        RoomData startRoom = new RoomData(startPos, RoomType.START, EnumSet.noneOf(Direction.class), true);

        DrunkardWalk.dungeonRoomsByDimension
                .computeIfAbsent(world.dimension(), d -> new HashMap<>())
                .put(startPos, startRoom);
    }

    public static void placeStructure (ServerLevel world, String structureID, BlockPos pos) {
        ResourceLocation id = new ResourceLocation(SsMod.MODID, structureID); // chỉ cần tên thôi, không "structures/"
        StructureTemplate template = world.getStructureManager().get(id).orElse(null);

        if (template != null) {
            template.placeInWorld(world, pos, pos, new StructurePlaceSettings(), world.random, 2);
        } else {
            Log.w("Không tìm thấy cấu trúc: " + id);
        }
    }

}
