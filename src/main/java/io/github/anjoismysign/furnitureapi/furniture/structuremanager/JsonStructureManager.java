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

    public void add(@NotNull FurnitureStructure furnitureStructure){
        var simpleFurnitureStructure = (SimpleFurnitureStructure) furnitureStructure;
        var storage = furnitureStructures.storage();
        var worldName = simpleFurnitureStructure.worldName();
        var furnitureNamespacedKey = simpleFurnitureStructure.furnitureNamespacedKey();
        var data = simpleFurnitureStructure.data();
        var worldStructures = storage.computeIfAbsent(worldName, k -> new HashMap<>());
        worldStructures.computeIfAbsent(furnitureNamespacedKey, k -> new ArrayList<>())
                .add(data);
    }

    @Override
    public @Nullable FurnitureStructure getFurnitureStructure(@NotNull Block block) {
        var worldName = block.getWorld().getName();
        var structures = getFurnitureStructures();
        var blockVector = block.getLocation().toVector().toBlockVector();
        var worldStructures = structures.storage().get(worldName);
        if (worldStructures == null) {
            return null;
        }
        for (var entry : worldStructures.entrySet()) {
            var furnitureKey = entry.getKey();
            var dataList = entry.getValue();

            for (var data : dataList) {
                var cuboid = data.cuboid();
                if (cuboid.isIn(blockVector)) {
                    return new SimpleFurnitureStructure(furnitureKey, worldName, data);
                }
            }
        }
        return null;
    }

    @Override
    public void remove(@NotNull FurnitureStructure furnitureStructure) {
        var simpleFurnitureStructure = (SimpleFurnitureStructure) furnitureStructure;
        var worldName = simpleFurnitureStructure.worldName();
        var furnitureNamespacedKey = simpleFurnitureStructure.furnitureNamespacedKey();
        var structures = getFurnitureStructures();
        @Nullable var worldStructures = structures.storage().get(worldName);
        if (worldStructures == null) {
            return;
        }
        @Nullable var list = worldStructures.get(furnitureNamespacedKey);
        if (list == null) {
            return;
        }
        list.remove(simpleFurnitureStructure.data());
    }
}