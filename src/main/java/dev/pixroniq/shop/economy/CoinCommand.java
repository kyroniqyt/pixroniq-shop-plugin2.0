package dev.pixroniq.shop.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoinCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economy;

    public CoinCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!economy.isReady()) {
            sender.sendMessage(ChatColor.RED + "No economy plugin found. Ask an admin to install Vault "
                    + "plus an economy plugin (e.g. EssentialsX) for coins to work.");
            return true;
        }

        // /coins                       -> your own balance
        // /coins <player>              -> someone else's balance
        // /coins give|take|set <player> <amount>  -> admin only
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /coins <player>");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GOLD + economy.format(economy.getBalance(player)));
            return true;
        }

        String first = args[0].toLowerCase();
        if (first.equals("give") || first.equals("take") || first.equals("set")) {
            if (!sender.hasPermission("pixroniqshop.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /coins " + first + " <player> <amount>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }
            switch (first) {
                case "give" -> economy.deposit(target, amount);
                case "take" -> economy.withdraw(target, amount);
                case "set" -> economy.set(target, amount);
            }
            sender.sendMessage(ChatColor.GREEN + (target.getName() != null ? target.getName() : args[1])
                    + "'s balance is now " + economy.format(economy.getBalance(target)));
            return true;
        }

        // /coins <player>
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        sender.sendMessage(ChatColor.YELLOW + (target.getName() != null ? target.getName() : args[0]) + " has "
                + ChatColor.GOLD + economy.format(economy.getBalance(target)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(Arrays.asList("give", "take", "set"));
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        String cur = args[args.length - 1].toLowerCase();
        return out.stream().filter(s -> s.toLowerCase().startsWith(cur)).collect(Collectors.toList());
    }
}
