package io.github.anjoismysign.furnitureapi.furniture;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FurnitureManager {

    private final JavaPlugin plugin;
    private final List<Furniture> furniture = new ArrayList<>();

    public FurnitureManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads furniture from "${pluginDataFolder}/furniture/"
     * Every .nbt file becomes a Furniture object; .yml files provide optional metadata.
     */
    public void reload() {
        unload();
        var folder = new File(plugin.getDataFolder(), "furniture");
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }
        var files = folder.listFiles((dir, name) -> name.endsWith(".nbt"));
        if (files == null) return;
        var structureManager = Bukkit.getStructureManager();
        for (var nbtFile : files) {
            var key = nbtFile.getName().replace(".nbt", "");
            var namespacedKey = NamespacedKey.minecraft(key);
            try {
                var structure = structureManager.loadStructure(nbtFile);
                var ymlFile = new File(folder, key + ".yml");
                String command = null;
                var executeAsServer = false;
                if (ymlFile.exists()) {
                    var config = YamlConfiguration.loadConfiguration(ymlFile);
                    command = config.getString("Command", null);
                    executeAsServer = config.getBoolean("Execute-Command-As-Server", false);
                }
                structureManager.registerStructure(namespacedKey, structure);
                var item = new Furniture(namespacedKey, command, executeAsServer);
                furniture.add(item);
                plugin.getLogger().info("Loaded furniture: " + namespacedKey);
            } catch (IOException | RuntimeException exception) {
                plugin.getLogger().severe("Failed to load furniture structure for " + key + ": " + exception.getMessage());
            }
        }
    }

    /**
     * Iterates the furniture list to unregister structures from Bukkit and clears the list.
     */
    public void unload() {
        var structureManager = Bukkit.getStructureManager();
        for (var item : furniture) {
            structureManager.unregisterStructure(item.namespacedKey());
        }
        furniture.clear();
    }

    @NotNull
    public List<Furniture> getFurniture() {
        return Collections.unmodifiableList(furniture);
    }

    @Nullable
    public Furniture getFurniture(@NotNull NamespacedKey key) {
        return furniture.stream()
                .filter(furniture -> furniture.namespacedKey().equals(key))
                .findFirst().orElse(null);
    }
}