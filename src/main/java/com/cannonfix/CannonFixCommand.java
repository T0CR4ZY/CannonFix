package com.cannonfix;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CannonFixCommand implements CommandExecutor {

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
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "CannonFix configuration reloaded!");
                return true;

            case "status":
                if (!sender.hasPermission("cannonfix.status")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                showStatus(sender);
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════ CannonFix ═══════");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix reload " + ChatColor.WHITE + "- Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/cannonfix status " + ChatColor.WHITE + "- Show status");
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════");
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════ CannonFix Status ═══════");
        sender.sendMessage(ChatColor.YELLOW + "Falling Block Protection: " + ChatColor.GREEN + "ACTIVE");
        sender.sendMessage(ChatColor.YELLOW + "TNT Physics Fix: " + ChatColor.GREEN + "ACTIVE");
        sender.sendMessage(ChatColor.YELLOW + "Cramming Bypass: " + ChatColor.GREEN + "ACTIVE");
        sender.sendMessage(ChatColor.YELLOW + "Consistent TNT Radius: " + 
            (plugin.getConfig().getBoolean("tnt.consistent-radius", true) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        sender.sendMessage(ChatColor.YELLOW + "Consistent Knockback: " + 
            (plugin.getConfig().getBoolean("tnt.consistent-knockback", true) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════════════");
    }
}
