package io.github.anjoismysign.furnitureapi.furniture.structuremanager;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import io.github.anjoismysign.furnitureapi.furniture.FurnitureStructure;
import io.github.anjoismysign.furnitureapi.furniture.FurnitureStructures;
import io.github.anjoismysign.furnitureapi.furniture.SimpleFurnitureStructure;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonStructureManager implements StructureManager{

    private final FurnitureApiPlugin plugin;
    private final File furnitureStructuresFile;
    private final FurnitureStructures furnitureStructures;

    public JsonStructureManager(@NotNull FurnitureApiPlugin plugin) {
        this.plugin = plugin;
        furnitureStructuresFile = new File(plugin.getDataFolder(), "furniture-structures.json");
        furnitureStructures = FurnitureStructures.READ(furnitureStructuresFile);
    }

    public FurnitureStructures getFurnitureStructures() {
        return furnitureStructures;
    }

    public void save(){
        furnitureStructures.serialize(furnitureStructuresFile);
    }

    @Override
    public void add(@NotNull FurnitureStructure furnitureStructure) {
        var simple = (SimpleFurnitureStructure) furnitureStructure;
        furnitureStructures.addStructure(
                simple.worldName(),
                simple.furnitureNamespacedKey(),
                simple.data()
        );
    }

    @Override
    public @Nullable FurnitureStructure getFurnitureStructure(@NotNull Block block) {
        var worldName = block.getWorld().getName();
        var vector = block.getLocation().toVector().toBlockVector();

        // O(1) Lookup
        var pointer = furnitureStructures.getAt(worldName, vector);
        if (pointer == null) return null;

        return new SimpleFurnitureStructure(pointer.key(), worldName, pointer.data());
    }

    @Override
    public void remove(@NotNull FurnitureStructure furnitureStructure) {
        var simple = (SimpleFurnitureStructure) furnitureStructure;
        furnitureStructures.removeStructure(
                simple.worldName(),
                simple.furnitureNamespacedKey(),
                simple.data()
        );
    }
}