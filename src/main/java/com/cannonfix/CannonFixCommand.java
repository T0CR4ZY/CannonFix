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
            sender.sendMessage(ChatColor.GOLD + "=== CannonFix ===");
            sender.sendMessage(ChatColor.YELLOW + "/cf reload " + ChatColor.WHITE + "- Reload");
            sender.sendMessage(ChatColor.YELLOW + "/cf status " + ChatColor.WHITE + "- Status");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("cannonfix.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "Reloaded!");
                return true;

            case "status":
                sender.sendMessage(ChatColor.GOLD + "=== CannonFix Status ===");
                sender.sendMessage(ChatColor.WHITE + "TNT Radius: " + (plugin.getConfig().getBoolean("tnt.consistent-radius") ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                sender.sendMessage(ChatColor.WHITE + "TNT Knockback: " + (plugin.getConfig().getBoolean("tnt.consistent-knockback") ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                for (World world : Bukkit.getWorlds()) {
                    Integer val = world.getGameRuleValue(GameRule.MAX_ENTITY_CRAMMING);
                    sender.sendMessage(ChatColor.WHITE + world.getName() + " cramming: " + val);
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (String s : Arrays.asList("reload", "status")) {
                if (s.startsWith(args[0].toLowerCase())) list.add(s);
            }
            return list;
        }
        return new ArrayList<>();
    }
}
