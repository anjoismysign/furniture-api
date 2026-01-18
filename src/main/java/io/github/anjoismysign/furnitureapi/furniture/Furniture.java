package io.github.anjoismysign.furnitureapi.furniture;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import io.github.anjoismysign.furnitureapi.FurnitureNamespacedKeys;
import io.github.anjoismysign.util.Cuboid;
import io.github.anjoismysign.util.Structrador;
import io.github.anjoismysign.util.Vectorator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public record Furniture(@NotNull NamespacedKey namespacedKey,
                        @Nullable String command,
                        boolean executeAsServer) {

    private static final StructureManager STRUCTURE_MANAGER = Bukkit.getStructureManager();


    /**
     * Makes a player place some Furniture at a specific location.
     * @param location Where the Furniture is placed at
     * @param direction The direction of the furniture
     * @return true if successful, false otherwise
     */
    public boolean place(@NotNull Location location,
                         @NotNull BlockFace direction){
        @Nullable var bukkitStructure = STRUCTURE_MANAGER.getStructure(namespacedKey);
        if (bukkitStructure == null){
            return false;
        }
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        var structrador = new Structrador(bukkitStructure, plugin);
        StructureRotation rotation;
        switch (direction) {
            case WEST -> rotation = StructureRotation.CLOCKWISE_90;
            case SOUTH -> rotation = StructureRotation.NONE;
            case EAST -> rotation = StructureRotation.COUNTERCLOCKWISE_90;
            default -> rotation = StructureRotation.CLOCKWISE_180;
        }
        var uuid = UUID.randomUUID();
        var world = location.getWorld();
        var pos1 = location.toVector().toBlockVector();
        var pos2 = pos1.clone().add(Vectorator.of(bukkitStructure.getSize().clone().subtract(new Vector(1,1,1))).rotate(rotation));
        var cuboid = Cuboid.of(pos1,pos2,world);
        var worldName = world.getName();
        var furnitureStructure = new SimpleFurnitureStructure(namespacedKey, worldName, new FurnitureStructureData(cuboid, uuid));
        plugin.getStructureManager().add(furnitureStructure);
        Bukkit.getScheduler().runTask(plugin, ()->{
            structrador.simultaneousPlace(
                    location,
                    true,
                    rotation,
                    Mirror.NONE,
                    0,
                    1.0f,
                    new Random(),
                    block -> {
                    },
                    entity -> {
                        var type = entity.getType();
                        if (!type.name().endsWith("DISPLAY")){
                            entity.remove();
                            return;
                        }
                        var container = entity.getPersistentDataContainer();
                        container.set(FurnitureNamespacedKeys.STRUCTURE.getNamespacedKey(), PersistentDataType.STRING, uuid.toString());
                    }
            );
        });
        return true;
    }

    public void executeCommand(@NotNull Player player) {
        if (command == null) {
            return;
        }
        CommandSender sender = executeAsServer ? Bukkit.getConsoleSender() : player;
        Bukkit.dispatchCommand(sender, command);
    }

}
