package io.github.anjoismysign.furnitureapi;

import io.github.anjoismysign.furnitureapi.command.FurnitureAPICommand;
import io.github.anjoismysign.furnitureapi.furniture.Furniture;
import io.github.anjoismysign.furnitureapi.furniture.FurnitureManager;
import io.github.anjoismysign.furnitureapi.furniture.FurnitureStructure;
import io.github.anjoismysign.furnitureapi.furniture.structuremanager.JsonStructureManager;
import io.github.anjoismysign.furnitureapi.furniture.structuremanager.StructureManager;
import io.github.anjoismysign.furnitureapi.listener.FurnitureDestroy;
import io.github.anjoismysign.furnitureapi.listener.FurnitureInteract;
import io.github.anjoismysign.furnitureapi.listener.FurniturePlace;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FurnitureApiPlugin extends JavaPlugin implements FurnitureApi {

    private FurnitureManager furnitureManager;
    private StructureManager structureManager;

    @Override
    public void onEnable() {
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new FurniturePlace(), this);
        pluginManager.registerEvents(new FurnitureDestroy(), this);
        pluginManager.registerEvents(new FurnitureInteract(), this);
        this.furnitureManager = new FurnitureManager(this);
        this.structureManager = new JsonStructureManager(this);
        var handler = new FurnitureAPICommand(this);
        var command = getCommand("furnitureapi");
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }
        var scheduler = Bukkit.getScheduler();
        scheduler.runTask(this, () -> {
            furnitureManager.reload();
        });
        scheduler.runTaskTimerAsynchronously(this, task ->{
            getStructureManager().save();
        }, 0, 600);
    }

    @Override
    public void onDisable() {
        furnitureManager.unload();
        getStructureManager().save();
    }

    public void reload(){
        getFurnitureManager().reload();
    }

    @NotNull
    public FurnitureManager getFurnitureManager() {
        return furnitureManager;
    }

    @NotNull
    public StructureManager getStructureManager(){
        return structureManager;
    }

    @Nullable
    public FurnitureStructure getFurnitureStructure(@NotNull Block block){
        return structureManager.getFurnitureStructure(block);
    }

    @Override
    public @NotNull List<Furniture> getFurniture() {
        return furnitureManager.getFurniture();
    }

    @Override
    public @Nullable Furniture getFurniture(@NotNull NamespacedKey key) {
        return furnitureManager.getFurniture(key);
    }

}
