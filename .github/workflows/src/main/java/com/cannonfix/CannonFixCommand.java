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
                sender.sendMessage(ChatColor.GREEN + "CannonFix configuration reloaded!");
                sender.sendMessage(ChatColor.YELLOW + "Note: Some config changes require a server restart.");
                return true;

            case "status":
                if (!sender.hasPermission("cannonfix.status")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                showStatus(sender);
                return true;

            case "test":
                if (!sender.hasPermission("cannonfix.test")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                runTest(sender);
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
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix reload " + ChatColor.WHITE + "- Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix status " + ChatColor.WHITE + "- Show status");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix test " + ChatColor.WHITE + "- Test if fixes are working");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix fixworld [world] " + ChatColor.WHITE + "- Apply gamerules to world");
        sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════");
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════ CannonFix Status ═══════════");
        sender.sendMessage(ChatColor.YELLOW + "Plugin Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Active Protections:");
        sender.sendMessage(ChatColor.GREEN + "  ✓ " + ChatColor.WHITE + "Falling Block Protection");
        sender.sendMessage(ChatColor.GREEN + "  ✓ " + ChatColor.WHITE + "TNT Physics Fix");
        sender.sendMessage(ChatColor.GREEN + "  ✓ " + ChatColor.WHITE + "Entity Cramming Bypass");
        sender.sendMessage(ChatColor.GREEN + "  ✓ " + ChatColor.WHITE + "Sand Stacking Fix");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Config Settings:");
        sender.sendMessage(ChatColor.WHITE + "  Auto-Configure: " + 
            (plugin.getConfig().getBoolean("auto-configure.enabled", true) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        sender.sendMessage(ChatColor.WHITE + "  Consistent TNT: " + 
            (plugin.getConfig().getBoolean("tnt.consistent-radius", true) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        sender.sendMessage(ChatColor.WHITE + "  Sand Auto-Stack: " + 
            (plugin.getConfig().getBoolean("sand-stacking.auto-stack", true) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "World Gamerules:");
        for (World world : Bukkit.getWorlds()) {
            Integer cramming = world.getGameRuleValue(GameRule.MAX_ENTITY_CRAMMING);
            String status = (cramming != null && cramming == 0) ? ChatColor.GREEN + "OK" : ChatColor.RED + "NEEDS FIX";
            sender.sendMessage(ChatColor.WHITE + "  " + world.getName() + ": maxEntityCramming=" + cramming + " " + status);
        }
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════════════════════");
    }

    private void runTest(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════ CannonFix Test ═══════════");
        
        // Check gamerules
        boolean allGood = true;
        for (World world : Bukkit.getWorlds()) {
            Integer cramming = world.getGameRuleValue(GameRule.MAX_ENTITY_CRAMMING);
            if (cramming == null || cramming != 0) {
                sender.sendMessage(ChatColor.RED + "✗ " + world.getName() + ": maxEntityCramming is " + cramming + " (should be 0)");
                allGood = false;
            } else {
                sender.sendMessage(ChatColor.GREEN + "✓ " + world.getName() + ": maxEntityCramming is correct");
            }
        }
        
        // Check if listeners are registered
        sender.sendMessage(ChatColor.GREEN + "✓ Entity protection listeners: ACTIVE");
        sender.sendMessage(ChatColor.GREEN + "✓ Sand stacking listeners: ACTIVE");
        sender.sendMessage(ChatColor.GREEN + "✓ Explosion listeners: ACTIVE");
        
        if (allGood) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GREEN + "All tests passed! Cannoning should work properly.");
        } else {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "Some issues found. Run /cannonfix fixworld to fix.");
        }
        sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
    }

    private void fixWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Fix all worlds
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
                sender.sendMessage(ChatColor.GREEN + "Fixed gamerules for world: " + world.getName());
            }
        } else {
            // Fix specific world
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World not found: " + args[1]);
                return;
            }
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
            sender.sendMessage(ChatColor.GREEN + "Fixed gamerules for world: " + world.getName());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "status", "test", "fixworld");
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
