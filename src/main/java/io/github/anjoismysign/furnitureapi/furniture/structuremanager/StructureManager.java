package io.github.anjoismysign.furnitureapi.furniture.structuremanager;

import io.github.anjoismysign.furnitureapi.furniture.FurnitureStructure;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StructureManager {
    void save();

    void add(@NotNull FurnitureStructure furnitureStructure);

    /**
     * @param block The block to query
     * @return The FurnitureStructure associated with this block, null if there's no FurnitureStructure associated.
     */
    @Nullable
    FurnitureStructure getFurnitureStructure(@NotNull Block block);

    void remove(@NotNull FurnitureStructure furnitureStructure);
}
