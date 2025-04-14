package util;

import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;

import java.util.HashMap;
import java.util.Map;

public class GetTextures {
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    /**
     * Lấy ResourceLocation theo thư mục và tên tệp.
     *
     * @param folder   Thư mục chứa tệp (ví dụ: "screens", "items", "entities")
     * @param filename Tên tệp hình ảnh (ví dụ: "template_gui.png")
     * @return ResourceLocation trỏ đến tài nguyên tương ứng
     */
    public static ResourceLocation get (String folder, String filename) {
        String fullPath = "textures/" + folder + "/" + filename;
        return CACHE.computeIfAbsent(fullPath, path -> new ResourceLocation(SsMod.MODID, path));
    }

    /**
     * Lấy ResourceLocation mặc định trong thư mục `screens`
     *
     * @param filename Tên tệp hình ảnh
     * @return ResourceLocation trỏ đến `textures/screens/filename`
     */
    public static ResourceLocation screen (String filename) {
        return get("gui", filename);
    }

    /**
     * Lấy ResourceLocation từ thư mục `items`
     *
     * @param filename Tên tệp hình ảnh
     * @return ResourceLocation trỏ đến `textures/items/filename`
     */
    public static ResourceLocation item (String filename) {
        return get("items", filename);
    }

    /**
     * Lấy ResourceLocation từ thư mục `entities`
     *
     * @param filename Tên tệp hình ảnh
     * @return ResourceLocation trỏ đến `textures/entities/filename`
     */
    public static ResourceLocation entity (String filename) {
        return get("entities", filename);
    }
}
