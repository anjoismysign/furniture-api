package io.github.anjoismysign.furnitureapi.listener;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class FurnitureDestroy implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event){
        var block = event.getBlock();
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        @Nullable var structure = plugin.getStructureManager().getFurnitureStructure(block);
        if (structure == null){
            return;
        }
        structure.destroy();
    }

}
