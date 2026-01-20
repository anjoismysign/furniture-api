package io.github.anjoismysign.furnitureapi.furniture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.anjoismysign.util.Cuboid;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FurnitureStructures(@NotNull Map<String, Map<NamespacedKey, List<FurnitureStructureData>>> storage,
                                  @NotNull Map<String, Map<BlockVector, StructureLookup>> spatialLookup) {

    private static final Gson GSON = new Gson();

    public record StructureLookup(@NotNull NamespacedKey key, @NotNull FurnitureStructureData data) {}

    private FurnitureStructures(@NotNull Map<String, Map<NamespacedKey, List<FurnitureStructureData>>> storage) {
        this(storage, new HashMap<>());
    }

    private static BlockVector blockVectorOf(@NotNull String string){
        var split = string.split(",");
        if (split.length != 3){
            throw new IllegalArgumentException("Not a BlockVector: " + string);
        }
        int x, y, z;
        try {
            x = Integer.parseInt(split[0].trim());
            y = Integer.parseInt(split[1].trim());
            z = Integer.parseInt(split[2].trim());
        } catch (NumberFormatException exception){
            throw new IllegalArgumentException("Not a BlockVector: " + string);
        }
        return new BlockVector(x, y, z);
    }

    private static String stringBlockVector(@NotNull BlockVector blockVector){
        return blockVector.getBlockX() + "," +
                blockVector.getBlockY() + "," +
                blockVector.getBlockZ();
    }

    @NotNull
    public static FurnitureStructures READ(@NotNull File file) {
        Map<String, Map<NamespacedKey, List<FurnitureStructureData>>> storage = new HashMap<>();
        FurnitureStructures structures = new FurnitureStructures(storage);

        if (!file.exists()) return structures;

        try (var reader = new FileReader(file)) {
            var type = new TypeToken<Map<String, Map<String, List<SerializedStructure>>>>(){}.getType();
            Map<String, Map<String, List<SerializedStructure>>> rawData = GSON.fromJson(reader, type);

            if (rawData != null) {
                rawData.forEach((worldName, furnitureMap) -> {
                    furnitureMap.forEach((keyString, serializedList) -> {
                        var key = NamespacedKey.fromString(keyString);
                        if (key == null) return;

                        for (SerializedStructure ser : serializedList) {
                            var min = blockVectorOf(ser.min);
                            var max = blockVectorOf(ser.max);
                            var data = new FurnitureStructureData(
                                    new Cuboid(min, max, worldName),
                                    UUID.fromString(ser.uuid)
                            );
                            // Use addStructure to populate BOTH maps at once during load
                            structures.addStructure(worldName, key, data);
                        }
                    });
                });
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return structures;
    }

    public void serialize(@NotNull File file) {
        Map<String, Map<String, List<SerializedStructure>>> rawData = new HashMap<>();
        storage.forEach((worldName, furnitureMap) -> {
            Map<String, List<SerializedStructure>> rawFurnitureMap = new HashMap<>();
            furnitureMap.forEach((key, structures) -> {
                List<SerializedStructure> serializedList = structures.stream()
                        .map(data -> new SerializedStructure(
                                data.uuid().toString(),
                                stringBlockVector(data.cuboid().serialPoint1().toBlockVector()),
                                stringBlockVector(data.cuboid().serialPoint2().toBlockVector())
                        )).toList();
                rawFurnitureMap.put(key.toString(), serializedList);
            });
            rawData.put(worldName, rawFurnitureMap);
        });
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(rawData, writer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static class SerializedStructure {
        String uuid;
        String min;
        String max;

        public SerializedStructure(String uuid, String min, String max) {
            this.uuid = uuid;
            this.min = min;
            this.max = max;
        }
    }

    @Nullable
    public FurnitureStructures.StructureLookup getAt(@NotNull String worldName, @NotNull BlockVector vector) {
        var worldMap = spatialLookup.get(worldName);
        return worldMap == null ? null : worldMap.get(vector);
    }

    public void addStructure(@NotNull String worldName, @NotNull NamespacedKey key, @NotNull FurnitureStructureData data) {
        // 1. Update Persistent Storage
        storage.computeIfAbsent(worldName, k -> new HashMap<>())
                .computeIfAbsent(key, k -> new ArrayList<>())
                .add(data);

        // 2. Update Spatial Lookup
        var worldLookup = spatialLookup.computeIfAbsent(worldName, k -> new HashMap<>());
        StructureLookup lookup = new StructureLookup(key, data);

        // Using blockList() from Cuboid as requested
        data.cuboid().blockList().forEach(block -> {
            worldLookup.put(block.getLocation().toVector().toBlockVector(), lookup);
        });
    }

    public void removeStructure(@NotNull String worldName, @NotNull NamespacedKey key, @NotNull FurnitureStructureData data) {
        // 1. Remove from Persistent Storage
        var worldMap = storage.get(worldName);
        if (worldMap != null) {
            var list = worldMap.get(key);
            if (list != null) list.remove(data);
        }

        // 2. Remove from Spatial Lookup
        var worldLookup = spatialLookup.get(worldName);
        if (worldLookup != null) {
            data.cuboid().blockList().forEach(block -> {
                worldLookup.remove(block.getLocation().toVector().toBlockVector());
            });
        }
    }
}