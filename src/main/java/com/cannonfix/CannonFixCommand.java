package com.cannonfix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CannonFixCommand implements CommandExecutor, TabCompleter {

    private final CannonFix plugin;

    public CannonFixCommand(CannonFix plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("cannonfix.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "CannonFix reloaded!");
                return true;

            case "status":
                if (!sender.hasPermission("cannonfix.status")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                showStatus(sender);
                return true;

            case "fixworld":
                if (!sender.hasPermission("cannonfix.fixworld")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                fixWorld(sender, args);
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════ CannonFix ═══════════");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix status " + ChatColor.WHITE + "- Show status");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix fixworld [world] " + ChatColor.WHITE + "- Apply gamerules");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix reload " + ChatColor.WHITE + "- Reload config");
        sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════");
        sender.sendMessage(ChatColor.GRAY + "This plugin auto-configures your server for cannoning.");
        sender.sendMessage(ChatColor.GRAY + "Restart server after first install for full effect!");
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════ CannonFix Status ═══════════");
        sender.sendMessage(ChatColor.YELLOW + "Plugin Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "World Gamerules:");
        for (World world : Bukkit.getWorlds()) {
            Integer cramming = world.getGameRuleValue(GameRule.MAX_ENTITY_CRAMMING);
            String status = (cramming != null && cramming == 0) ? ChatColor.GREEN + "✓ OK" : ChatColor.RED + "✗ NEEDS FIX";
            sender.sendMessage(ChatColor.WHITE + "  " + world.getName() + ": maxEntityCramming=" + cramming + " " + status);
        }
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Config auto-applied on startup:");
        sender.sendMessage(ChatColor.WHITE + "  • tnt-entity-height-nerf: 0");
        sender.sendMessage(ChatColor.WHITE + "  • falling-block-height-nerf: 0");
        sender.sendMessage(ChatColor.WHITE + "  • max-tnt-per-tick: 5000");
        sender.sendMessage(ChatColor.WHITE + "  • redstone-implementation: VANILLA");
        sender.sendMessage(ChatColor.WHITE + "  • max-entity-collisions: 0");
        sender.sendMessage(ChatColor.WHITE + "  • allow-permanent-block-break-exploits: true");
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════════════════════");
    }

    private void fixWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
                sender.sendMessage(ChatColor.GREEN + "Set maxEntityCramming=0 in: " + world.getName());
            }
        } else {
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World not found: " + args[1]);
                return;
            }
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
            sender.sendMessage(ChatColor.GREEN + "Set maxEntityCramming=0 in: " + world.getName());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "status", "fixworld");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("fixworld")) {
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(world.getName());
                }
            }
        }
        
        return completions;
    }
}
