package dev.ftb.ftbsbc.dimensions.prebuilt;

import dev.ftb.ftbsbc.FTBStoneBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PrebuiltStructure {
    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(FTBStoneBlock.MOD_ID, "textures/default_start.png");

    public final ResourceLocation id;
    public final Component name;
    public final String author;
    public ResourceLocation image;

    public PrebuiltStructure(ResourceLocation id, Component name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.image = new ResourceLocation(id.getNamespace(), "stoneblock_start/" + id.getPath() + ".png");
    }

    @Override
    public String toString() {
        return "PrebuiltStructure{" +
                "id=" + id +
                ", name=" + name +
                ", author='" + author + '\'' +
                ", image=" + image +
                '}';
    }
}
