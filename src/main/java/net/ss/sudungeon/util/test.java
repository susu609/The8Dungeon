//package net.ss.sudungeon.util;
//
//import com.mojang.serialization.Lifecycle;
//import net.minecraft.client.Minecraft;
//import net.minecraft.world.Difficulty;
//import net.minecraft.world.level.GameRules;
//import net.minecraft.world.level.GameType;
//import net.minecraft.world.level.LevelSettings;
//import net.minecraft.world.level.WorldDataConfiguration;
//import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
//import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
//import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
//import net.minecraft.world.level.dimension.LevelStem;
//import net.minecraft.world.level.levelgen.*;
//import net.minecraft.world.level.levelgen.presets.WorldPresets;
//import net.minecraft.world.level.storage.LevelStorageSource;
//import net.minecraft.world.level.storage.PrimaryLevelData;
//
//import java.io.IOException;
//import java.util.Map;
//
//public class test {
//    public void test1 () {
//        Minecraft mc = Minecraft.getInstance();
//        String worldName = "MyNewWorld"; // Đặt tên thế giới
//
//        try {
//            // Tạo thư mục lưu trữ thế giới
//            LevelStorageSource levelStorage = mc.getLevelSource();
//            LevelStorageSource.LevelStorageAccess worldAccess = levelStorage.createAccess(worldName);
//
//            // Tạo cài đặt thế giới
//            LevelSettings levelSettings = new LevelSettings(
//                    worldName,
//                    GameType.SURVIVAL, // Chế độ chơi
//                    false, // Hardcore?
//                    Difficulty.NORMAL, // Độ khó
//                    false, // Allow Cheats
//                    new GameRules(),
//                    WorldDataConfiguration.DEFAULT
//            );
//
//            // Không cần RegistryAccess, tạo preset thế giới trực tiếp
//            WorldOptions worldOptions = WorldOptions.defaultWithRandomSeed();
//            WorldGenSettings worldGenSettings = new WorldGenSettings(worldOptions, new WorldDimensions(Map.of(
//                    LevelStem.OVERWORLD, new LevelStem(
//                            BuiltinDimensionTypes.OVERWORLD,
//                            new NoiseBasedChunkGenerator(
//                                    MultiNoiseBiomeSource.create(MultiNoiseBiomeSourceParameterLists.OVERWORLD),
//                                    NoiseGeneratorSettings.OVERWORLD
//                            )
//                    )
//            )));
//
//            // Load thế giới mới
//            PrimaryLevelData worldData = new PrimaryLevelData(levelSettings, worldOptions, PrimaryLevelData.SpecialWorldProperty.NONE, Lifecycle.stable());
//            mc.createWorldOpenFlows().createLevelFromExistingSettings(worldAccess, null, LayeredRegistryAccess.empty(), worldData);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}