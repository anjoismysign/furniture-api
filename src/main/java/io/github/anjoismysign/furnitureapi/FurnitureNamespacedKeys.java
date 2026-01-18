package io.github.anjoismysign.furnitureapi;

import org.bukkit.NamespacedKey;

public enum FurnitureNamespacedKeys {
    ITEM("furniture"),
    STRUCTURE("structure");

    private final NamespacedKey namespacedKey;

    FurnitureNamespacedKeys(String key){
        namespacedKey = new NamespacedKey("furnitureapi",key);
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }
}
