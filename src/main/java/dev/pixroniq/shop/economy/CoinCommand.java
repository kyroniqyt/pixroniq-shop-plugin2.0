package dev.pixroniq.shop.economy;

import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private final DataManager data;

    public CoinCommand(DataManager data) {
        this.data = data;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /coins                       -> your own balance
        // /coins <player>              -> someone else's balance
        // /coins give|take|set <player> <amount>  -> admin only
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /coins <player>");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GOLD + data.get(player.getUniqueId()).getBalance()
                    + ChatColor.YELLOW + " coins.");
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
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }
            PlayerData pd = data.get(target.getUniqueId());
            switch (first) {
                case "give" -> pd.setBalance(pd.getBalance() + amount);
                case "take" -> pd.setBalance(pd.getBalance() - amount);
                case "set" -> pd.setBalance(amount);
            }
            data.save();
            sender.sendMessage(ChatColor.GREEN + target.getName() + "'s balance is now " + pd.getBalance() + " coins.");
            return true;
        }

        // /coins <player>
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + target.getName() + " has " + ChatColor.GOLD
                + data.get(target.getUniqueId()).getBalance() + ChatColor.YELLOW + " coins.");
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
