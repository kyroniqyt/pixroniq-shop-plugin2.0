package dev.pixroniq.shop.shop;

import dev.pixroniq.shop.PixroniqShopPlugin;
import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.data.PlayerData;
import dev.pixroniq.shop.display.DisplayManager;
import dev.pixroniq.shop.economy.EconomyManager;
import dev.pixroniq.shop.pets.PetManager;
import dev.pixroniq.shop.pets.PetType;
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

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final PixroniqShopPlugin plugin;
    private final ShopManager shop;
    private final DataManager data;
    private final DisplayManager display;
    private final PetManager pets;
    private final EconomyManager economy;

    public ShopCommand(PixroniqShopPlugin plugin, ShopManager shop, DataManager data, DisplayManager display,
                        PetManager pets, EconomyManager economy) {
        this.plugin = plugin;
        this.shop = shop;
        this.data = data;
        this.display = display;
        this.pets = pets;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "additem": {
                if (!sender.hasPermission("pixroniqshop.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /shop additem <category> <id> <price> <name...>");
                    sender.sendMessage(ChatColor.GRAY + "Categories: COLOR PREFIX PET KILLMESSAGE");
                    sender.sendMessage(ChatColor.GRAY + "After creating it, set its value with /shop setvalue <id> <value>");
                    return true;
                }
                ShopCategory category = ShopCategory.parse(args[1]);
                if (category == null) {
                    sender.sendMessage(ChatColor.RED + "Unknown category. Use: COLOR, PREFIX, PET, or KILLMESSAGE.");
                    return true;
                }
                String id = args[2];
                int price;
                try {
                    price = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Price must be a number.");
                    return true;
                }
                String name = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                shop.addItem(id, category, name, price, "");
                sender.sendMessage(ChatColor.GREEN + "Created shop item '" + id + "' (" + category + ", " + price + " coins).");
                sender.sendMessage(ChatColor.GRAY + "Now set what it actually does: /shop setvalue " + id + " <value>");
                return true;
            }
            case "setvalue": {
                if (!sender.hasPermission("pixroniqshop.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /shop setvalue <id> <value>");
                    sender.sendMessage(ChatColor.GRAY + "COLOR: a color name like AQUA, or &b");
                    sender.sendMessage(ChatColor.GRAY + "PREFIX: literal text, & codes allowed, e.g. &6[Legend] ");
                    sender.sendMessage(ChatColor.GRAY + "PET: DOG, CAT, PARROT, or CAPYBARA");
                    sender.sendMessage(ChatColor.GRAY + "KILLMESSAGE: a template using {killer} and {victim}");
                    return true;
                }
                ShopItem item = shop.getItem(args[1]);
                if (item == null) {
                    sender.sendMessage(ChatColor.RED + "No shop item with that id.");
                    return true;
                }
                String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                item.setValue(value);
                shop.save();
                sender.sendMessage(ChatColor.GREEN + "Set value for '" + item.getId() + "'.");
                return true;
            }
            case "removeitem": {
                if (!sender.hasPermission("pixroniqshop.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /shop removeitem <id>");
                    return true;
                }
                boolean ok = shop.removeItem(args[1]);
                sender.sendMessage(ok ? ChatColor.GREEN + "Removed." : ChatColor.RED + "No shop item with that id.");
                return true;
            }
            case "list": {
                if (shop.getItems().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "The shop is empty.");
                    return true;
                }
                if (args.length >= 2) {
                    ShopCategory category = ShopCategory.parse(args[1]);
                    if (category == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown category. Use: COLOR, PREFIX, PET, or KILLMESSAGE.");
                        return true;
                    }
                    sendCategoryList(sender, category);
                } else {
                    for (ShopCategory category : ShopCategory.values()) {
                        sendCategoryList(sender, category);
                    }
                }
                return true;
            }
            case "buy": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /shop buy <id>");
                    return true;
                }
                ShopItem item = shop.getItem(args[1]);
                if (item == null) {
                    sender.sendMessage(ChatColor.RED + "No shop item with that id.");
                    return true;
                }
                if (!economy.isReady()) {
                    sender.sendMessage(ChatColor.RED + "No economy plugin found. Ask an admin to install Vault "
                            + "plus an economy plugin (e.g. EssentialsX) for the shop to work.");
                    return true;
                }
                PlayerData pd = data.get(player.getUniqueId());
                if (pd.owns(item.getId())) {
                    sender.sendMessage(ChatColor.YELLOW + "You already own that \u2014 equipping it instead.");
                    equip(player, item);
                    return true;
                }
                if (!economy.has(player, item.getPrice())) {
                    sender.sendMessage(ChatColor.RED + "You need " + economy.format(item.getPrice())
                            + " for that \u2014 you have " + economy.format(economy.getBalance(player)) + ".");
                    return true;
                }
                economy.withdraw(player, item.getPrice());
                pd.getOwned().add(item.getId());
                data.save();
                equip(player, item);
                sender.sendMessage(ChatColor.GREEN + "Bought and equipped '" + item.getDisplayName() + ChatColor.GREEN
                        + "' for " + economy.format(item.getPrice()) + ".");
                return true;
            }
            case "use": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /shop use <id>");
                    return true;
                }
                ShopItem item = shop.getItem(args[1]);
                if (item == null) {
                    sender.sendMessage(ChatColor.RED + "No shop item with that id.");
                    return true;
                }
                PlayerData pd = data.get(player.getUniqueId());
                if (!pd.owns(item.getId())) {
                    sender.sendMessage(ChatColor.RED + "You don't own that yet \u2014 buy it with /shop buy " + item.getId());
                    return true;
                }
                equip(player, item);
                sender.sendMessage(ChatColor.GREEN + "Equipped '" + item.getDisplayName() + ChatColor.GREEN + "'.");
                return true;
            }
            case "owned": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                PlayerData pd = data.get(player.getUniqueId());
                if (pd.getOwned().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "You don't own anything yet.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "You own: " + ChatColor.YELLOW + String.join(", ", pd.getOwned()));
                return true;
            }
            default:
                sendHelp(sender);
                return true;
        }
    }

    /** Applies a purchased/selected item's effect and remembers it as the player's active choice for that category. */
    private void equip(Player player, ShopItem item) {
        PlayerData pd = data.get(player.getUniqueId());
        pd.getActive().put(item.getCategory(), item.getId());
        data.save();

        switch (item.getCategory()) {
            case COLOR -> display.applyColor(player, item.getValue());
            case PREFIX -> display.applyPrefix(player, item.getValue());
            case PET -> {
                PetType type = PetType.parse(item.getValue());
                if (type != null && player.getWorld().getName()
                        .equalsIgnoreCase(plugin.getConfig().getString("hub-world", "hub"))) {
                    pets.spawnPet(player, type);
                }
            }
            case KILLMESSAGE -> {
                // No live effect to apply - looked up at kill time by /killmsg trigger.
            }
        }
    }

    private void sendCategoryList(CommandSender sender, ShopCategory category) {
        List<ShopItem> categoryItems = shop.getItems(category);
        if (categoryItems.isEmpty()) return;
        sender.sendMessage(ChatColor.GOLD + "--- " + category.name() + " ---");
        for (ShopItem item : categoryItems) {
            sender.sendMessage(ChatColor.YELLOW + item.getId() + ChatColor.GRAY + " - "
                    + ChatColor.translateAlternateColorCodes('&', item.getDisplayName())
                    + ChatColor.GRAY + " (" + item.getPrice() + " coins)");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Shop ---");
        sender.sendMessage(ChatColor.YELLOW + "/shop list [category]");
        sender.sendMessage(ChatColor.YELLOW + "/shop buy <id>");
        sender.sendMessage(ChatColor.YELLOW + "/shop use <id>" + ChatColor.GRAY + "  (switch to something you already own)");
        sender.sendMessage(ChatColor.YELLOW + "/shop owned");
        if (sender.hasPermission("pixroniqshop.admin")) {
            sender.sendMessage(ChatColor.GOLD + "--- Admin ---");
            sender.sendMessage(ChatColor.YELLOW + "/shop additem <category> <id> <price> <name...>");
            sender.sendMessage(ChatColor.YELLOW + "/shop setvalue <id> <value>");
            sender.sendMessage(ChatColor.YELLOW + "/shop removeitem <id>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(Arrays.asList("list", "buy", "use", "owned"));
            if (sender.hasPermission("pixroniqshop.admin")) {
                out.addAll(Arrays.asList("additem", "setvalue", "removeitem"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("additem")) {
            out.addAll(Arrays.asList("COLOR", "PREFIX", "PET", "KILLMESSAGE"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            out.addAll(Arrays.asList("COLOR", "PREFIX", "PET", "KILLMESSAGE"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("use")
                || args[0].equalsIgnoreCase("setvalue") || args[0].equalsIgnoreCase("removeitem"))) {
            out.addAll(shop.getItems().keySet());
        }
        String cur = args[args.length - 1].toLowerCase();
        return out.stream().filter(s -> s.toLowerCase().startsWith(cur)).collect(Collectors.toList());
    }
}
