package io.github.anjoismysign.furnitureapi.command;

import io.github.anjoismysign.furnitureapi.FurnitureApiPlugin;
import io.github.anjoismysign.furnitureapi.StructureItem;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FurnitureAPICommand implements CommandExecutor, TabCompleter {

    private final FurnitureApiPlugin plugin;

    public FurnitureAPICommand(FurnitureApiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /furnitureapi <reload|item>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Handle RELOAD
        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("furnitureapi.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to reload FurnitureAPI.");
                return true;
            }
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "FurnitureAPI configurations and structures have been reloaded.");
            return true;
        }

        // Handle ITEM
        if (subCommand.equals("item")) {
            if (!sender.hasPermission("furnitureapi.item")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to get furniture items.");
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /furnitureapi item <namespacedKey>");
                return true;
            }

            NamespacedKey key = NamespacedKey.fromString(args[1]);
            if (key == null) {
                sender.sendMessage(ChatColor.RED + "Invalid NamespacedKey format. Expected 'namespace:key' or 'key'.");
                return true;
            }

            ItemStack item = StructureItem.INSTANCE.getItemFor(key);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Furniture with key '" + key + "' not found.");
                return true;
            }

            player.getInventory().addItem(item);
            sender.sendMessage(ChatColor.GREEN + "You received the item for: " + ChatColor.YELLOW + key);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /furnitureapi <reload|item>");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // Suggestions for the first argument: /furnitureapi <reload|item>
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if (sender.hasPermission("furnitureapi.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("furnitureapi.item")) {
                completions.add("item");
            }
            return completions;
        }

        // Suggestions for the second argument: /furnitureapi item <namespacedKey>
        if (args.length == 2 && args[0].equalsIgnoreCase("item")) {
            if (!sender.hasPermission("furnitureapi.item")) {
                return Collections.emptyList();
            }
            String input = args[1].toLowerCase();
            return plugin.getFurnitureManager().getFurniture().stream()
                    .map(f -> f.namespacedKey().toString())
                    .filter(key -> key.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}