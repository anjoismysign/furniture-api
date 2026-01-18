package io.github.anjoismysign.furnitureapi.furniture;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface FurnitureStructure {
    /**
     * Destroys this FurnitureStructure from world
     */
    void destroy();

    /**
     * @return The world related to this FurnitureStructure
     */
    @NotNull
    World getWorld();

    /**
     * @return The data associated with this Furniture
     */
    @NotNull
    Furniture getFurniture();
}
