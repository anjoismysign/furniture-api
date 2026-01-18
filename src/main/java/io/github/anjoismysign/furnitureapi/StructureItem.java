package io.github.anjoismysign.furnitureapi;

import io.github.anjoismysign.furnitureapi.furniture.Furniture;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public enum StructureItem {
    INSTANCE;

    private final NamespacedKey namespacedKey;
    private final Material material = Material.STRUCTURE_BLOCK;
    private final ItemStack stack;

    StructureItem(){
        namespacedKey = FurnitureNamespacedKeys.ITEM.getNamespacedKey();
        stack = new ItemStack(material);
        var meta = Objects.requireNonNull(stack.getItemMeta(), material.name()+" has no ItemMeta");
        meta.setDisplayName(amperSand("&eFurniture"));
        stack.setItemMeta(meta);
    }

    private String amperSand(@NotNull String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @NotNull
    public ItemStack getItemFor(@NotNull Furniture furniture) {
        var stack = new ItemStack(this.stack);
        var meta = Objects.requireNonNull(stack.getItemMeta(), material.name()+" has no ItemMeta");
        var lore = new ArrayList<String>();
        lore.add(amperSand("&7For: &e")+furniture.namespacedKey());
        meta.setLore(lore);
        var container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, furniture.namespacedKey().toString());
        stack.setItemMeta(meta);
        return stack;
    }

    @Nullable
    public ItemStack getItemFor(@NotNull NamespacedKey namespacedKey){
        var plugin = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        @Nullable var furniture = plugin.getFurnitureManager().getFurniture(namespacedKey);
        if (furniture == null){
            return null;
        }
        return getItemFor(furniture);
    }

    @Nullable
    public Furniture getFurniture(@NotNull ItemStack stack) {
        var meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        var container = meta.getPersistentDataContainer();
        var keyString = container.get(namespacedKey, PersistentDataType.STRING);
        if (keyString == null) {
            return null;
        }
        var targetKey = NamespacedKey.fromString(keyString);
        if (targetKey == null) {
            return null;
        }
        var api = JavaPlugin.getPlugin(FurnitureApiPlugin.class);
        return api.getFurnitureManager().getFurniture(targetKey);
    }
}
