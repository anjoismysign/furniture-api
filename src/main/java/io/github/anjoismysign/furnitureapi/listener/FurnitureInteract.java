package io.github.anjoismysign.furnitureapi.listener;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class FurnitureInteract implements Listener {

    @EventHandler
    public void interact(PlayerInteractEvent event){
        if (event.getHand() != EquipmentSlot.HAND){
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        var block = event.getClickedBlock();
        if (block == null){
            return;
        }
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        @Nullable var structure = plugin.getStructureManager().getFurnitureStructure(block);
        if (structure == null){
            return;
        }
        var furniture = structure.getFurniture();
        furniture.executeCommand(event.getPlayer());
    }

}
