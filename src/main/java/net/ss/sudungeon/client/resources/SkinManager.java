package net.ss.sudungeon.client.resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkinManager {
    private static final Path SKIN_CONFIG_PATH = Paths.get("config", "sudungeon", "skin_config.json");
    private static final Map<String, String> playerSkinMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerStarting (ServerStartingEvent event) {
        // Load skin configuration on server start
        loadSkinConfig();
    }

    public static void saveSkinChoice(String playerUUID, String skinName) {
        playerSkinMap.put(playerUUID, skinName);
        saveSkinConfig();
    }

    public static String getSkinChoice (String playerUUID) {
        return playerSkinMap.getOrDefault(playerUUID, "default");
    }

    private static void loadSkinConfig () {
        try {
            if (Files.exists(SKIN_CONFIG_PATH)) {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> map = gson.fromJson(new FileReader(SKIN_CONFIG_PATH.toFile()), type);
                playerSkinMap.putAll(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSkinConfig () {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(playerSkinMap);
            try (FileWriter writer = new FileWriter(SKIN_CONFIG_PATH.toFile())) {
                writer.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
