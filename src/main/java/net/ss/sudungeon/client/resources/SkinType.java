package net.ss.sudungeon.client.resources;

import net.minecraft.resources.ResourceLocation;

public class SkinType {
    private final ResourceLocation texture;
    private final ModelType model;

    public SkinType(ResourceLocation texture, ModelType model) {
        this.texture = texture;
        this.model = model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ResourceLocation texture () {
        return texture;
    }

    // ... (Các phương thức khác nếu cần)
}