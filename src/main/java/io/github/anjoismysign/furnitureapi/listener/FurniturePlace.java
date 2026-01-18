package io.github.anjoismysign.furnitureapi.listener;

import io.github.anjoismysign.furnitureapi.StructureItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.Nullable;

public class FurniturePlace implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void place(BlockPlaceEvent event){
        @Nullable var furniture = StructureItem.INSTANCE.getFurniture(event.getItemInHand());
        if (furniture == null){
            return;
        }
        boolean successful = furniture.place(
                event.getBlockPlaced().getLocation(),
                event.getPlayer().getFacing());
        if (!successful){
            return;
        }
        event.setCancelled(true);
    }

}
