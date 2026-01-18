package io.github.anjoismysign.furnitureapi;

import io.github.anjoismysign.furnitureapi.furniture.Furniture;
import io.github.anjoismysign.furnitureapi.furniture.FurnitureStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FurnitureApi {
    /**
     * @param block The block that might belong to a FurnitureStructure
     * @return The FurnitureStructure if it belongs to, null if not a FurnitureStructure
     */
    @Nullable
    FurnitureStructure getFurnitureStructure(@NotNull Block block);

    /**
     * Gets all Furniture
     * @return All Furniture
     */
    @NotNull
    List<Furniture> getFurniture();

    /**
     * Gets some Furniture by its NamespacedKey
     * @param key The NamespacedKey associated with this Furniture
     * @return The Furniture if found, null otherwise
     */
    @Nullable
    Furniture getFurniture(@NotNull NamespacedKey key);
}
