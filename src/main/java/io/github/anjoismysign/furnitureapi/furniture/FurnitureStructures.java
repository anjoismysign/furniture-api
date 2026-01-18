package io.github.anjoismysign.furnitureapi.furniture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.anjoismysign.util.Cuboid;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FurnitureStructures(@NotNull Map<String, Map<NamespacedKey, List<FurnitureStructureData>>> storage) {

    private static final Gson GSON = new Gson();

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
        if (!file.exists()) {
            return new FurnitureStructures(storage);
        }
        try (var reader = new FileReader(file)) {
            var type = new TypeToken<Map<String, Map<String, List<SerializedStructure>>>>(){}.getType();
            Map<String, Map<String, List<SerializedStructure>>> rawData = GSON.fromJson(reader, type);
            if (rawData != null) {
                rawData.forEach((worldName, furnitureMap) -> {
                    Map<NamespacedKey, List<FurnitureStructureData>> worldStorage = new HashMap<>();
                    Map<Cuboid, NamespacedKey> worldBlocks = new HashMap<>();
                    furnitureMap.forEach((keyString, serializedList) -> {
                        var key = NamespacedKey.fromString(keyString);
                        if (key == null) return;

                        var structures = new ArrayList<FurnitureStructureData>();
                        for (SerializedStructure serializedStructure : serializedList) {
                            var uuid = UUID.fromString(serializedStructure.uuid);
                            var min = blockVectorOf(serializedStructure.min);
                            var max = blockVectorOf(serializedStructure.max);
                            var cuboid = new Cuboid(min, max, worldName);
                            var data = new FurnitureStructureData(cuboid, uuid);
                            structures.add(data);
                            worldBlocks.put(cuboid, key);
                        }
                        worldStorage.put(key, structures);
                    });
                    storage.put(worldName, worldStorage);
                });
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return new FurnitureStructures(storage);
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
}