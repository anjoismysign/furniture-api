package io.github.anjoismysign.furnitureapi.furniture;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import io.github.anjoismysign.furnitureapi.FurnitureNamespacedKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record SimpleFurnitureStructure(@NotNull NamespacedKey furnitureNamespacedKey,
                                       @NotNull String worldName,
                                       @NotNull FurnitureStructureData data)
        implements FurnitureStructure {

    @NotNull
    public Furniture getFurniture() {
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        var furnitureManager = plugin.getFurnitureManager();
        return Objects.requireNonNull(furnitureManager.getFurniture(furnitureNamespacedKey), furnitureNamespacedKey + " no longer exists?");
    }

    @NotNull
    public World getWorld() {
        return Objects.requireNonNull(Bukkit.getWorld(worldName), worldName + " is not loaded");
    }

    public void destroy() {
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        var cuboid = data.cuboid();
        cuboid.forEachBlock(block -> {
            Material blockType = block.getType();
            if (blockType.isAir() || blockType == Material.LIGHT){
                return;
            }
            block.setType(Material.AIR, true);
        });
        var entities = cuboid.getEntities();
        var uuid = data.uuid();
        var compare = uuid.toString();
        entities.forEach(entity -> {
            var container = entity.getPersistentDataContainer();
            @Nullable var toString = container.get(FurnitureNamespacedKeys.STRUCTURE.getNamespacedKey(), PersistentDataType.STRING);
            if (toString == null){
                return;
            }
            if (!toString.equals(compare)){
                return;
            }
            entity.remove();
        });
        plugin.getStructureManager().remove(this);
    }
}
