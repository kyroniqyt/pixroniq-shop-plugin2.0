package dev.pixroniq.shop.kill;

import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.data.PlayerData;
import dev.pixroniq.shop.shop.ShopCategory;
import dev.pixroniq.shop.shop.ShopItem;
import dev.pixroniq.shop.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Wire this into your duels plugin's "on kill" reward command, e.g.:
 *   killmsg trigger %killer% %victim%
 * (check your duels plugin's config for its exact placeholder names for killer/victim).
 */
public class KillCommand implements CommandExecutor {

    private static final String DEFAULT_MESSAGE = "{killer} defeated {victim}.";

    private final DataManager data;
    private final ShopManager shop;

    public KillCommand(DataManager data, ShopManager shop) {
        this.data = data;
        this.shop = shop;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("trigger")) {
            sender.sendMessage(ChatColor.RED + "Usage: /killmsg trigger <killerName> <victimName>");
            return true;
        }

        String killerName = args[1];
        String victimName = args[2];

        String template = DEFAULT_MESSAGE;
        var killer = Bukkit.getPlayerExact(killerName);
        if (killer != null) {
            PlayerData pd = data.get(killer.getUniqueId());
            String activeId = pd.getActive(ShopCategory.KILLMESSAGE);
            if (activeId != null) {
                ShopItem item = shop.getItem(activeId);
                if (item != null) {
                    template = item.getValue();
                }
            }
        }

        String message = ChatColor.translateAlternateColorCodes('&', template)
                .replace("{killer}", killerName)
                .replace("{victim}", victimName);

        Bukkit.broadcastMessage(message);
        return true;
    }
}
