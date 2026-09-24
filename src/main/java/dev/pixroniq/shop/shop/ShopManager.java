package dev.pixroniq.shop.shop;

import dev.pixroniq.shop.PixroniqShopPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private final PixroniqShopPlugin plugin;
    private final File file;
    private final Map<String, ShopItem> items = new LinkedHashMap<>();

    public ShopManager(PixroniqShopPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
        load();
    }

    public ShopItem addItem(String id, ShopCategory category, String displayName, int price, String value) {
        ShopItem item = new ShopItem(id.toLowerCase(), category, displayName, price, value);
        items.put(id.toLowerCase(), item);
        save();
        return item;
    }

    public boolean removeItem(String id) {
        boolean removed = items.remove(id.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public ShopItem getItem(String id) {
        return items.get(id.toLowerCase());
    }

    public Map<String, ShopItem> getItems() {
        return items;
    }

    public List<ShopItem> getItems(ShopCategory category) {
        List<ShopItem> out = new ArrayList<>();
        for (ShopItem item : items.values()) {
            if (item.getCategory() == category) out.add(item);
        }
        return out;
    }

    /**
     * Populates the shop with the full catalog from your original list, only if it's
     * currently empty (so this never overwrites items you've since edited or added).
     * Adjust prices/values afterward with /shop setvalue or /shop additem as needed.
     */
    public void loadDefaultsIfEmpty() {
        if (!items.isEmpty()) return;

        // Colored names - best-guess colors for names that don't map to a standard color word.
        // Fix any of these with /shop setvalue <id> <color> if they're not what you meant.
        addItem("cyan", ShopCategory.COLOR, "&bCyan", 1000, "AQUA");
        addItem("pink", ShopCategory.COLOR, "&dPink", 1000, "LIGHT_PURPLE");
        addItem("yn", ShopCategory.COLOR, "&6YN", 1000, "GOLD");
        addItem("black", ShopCategory.COLOR, "&0Black", 1000, "BLACK");
        addItem("slice", ShopCategory.COLOR, "&fSlice", 1000, "WHITE");
        addItem("yellow", ShopCategory.COLOR, "&eYellow", 1000, "YELLOW");
        addItem("kyronblue", ShopCategory.COLOR, "&9Kyron Blue", 1000, "BLUE");

        // Prefixes
        addItem("legend", ShopCategory.PREFIX, "&6Legend", 2000, "&6[Legend] ");
        addItem("bedwarsmaster", ShopCategory.PREFIX, "&aBedWars Master", 2000, "&a[BedWars Master] ");
        addItem("skywarsgod", ShopCategory.PREFIX, "&bSkyWars God", 2000, "&b[SkyWars God] ");
        addItem("duelsmonster", ShopCategory.PREFIX, "&cDuels Monster", 2000, "&c[Duels Monster] ");
        addItem("killingmachine", ShopCategory.PREFIX, "&4Killing Machine", 2000, "&4[Killing Machine] ");
        addItem("immortal", ShopCategory.PREFIX, "&5Immortal", 2000, "&5[Immortal] ");
        addItem("touchgrass", ShopCategory.PREFIX, "&2Touch Grass", 2000, "&2[Touch Grass] ");
        addItem("ekitten", ShopCategory.PREFIX, "&dE-Kitten", 2000, "&d[E-Kitten] ");

        // Hub pets - Capybara is a Turtle standing in until a resource pack reskins one; see PetType.java.
        addItem("dog", ShopCategory.PET, "&fDog", 4000, "DOG");
        addItem("cat", ShopCategory.PET, "&fCat", 4000, "CAT");
        addItem("capybara", ShopCategory.PET, "&fCapybara", 4000, "CAPYBARA");
        addItem("parrot", ShopCategory.PET, "&fParrot", 4000, "PARROT");

        // Kill messages
        addItem("deleted", ShopCategory.KILLMESSAGE, "Deleted", 1250, "{killer} deleted {victim}.");
        addItem("clipped", ShopCategory.KILLMESSAGE, "Clipped", 1250, "{killer} clipped {victim}.");
        addItem("comboed", ShopCategory.KILLMESSAGE, "Comboed", 1250, "{victim} got comboed by {killer}.");
        addItem("ragequit", ShopCategory.KILLMESSAGE, "Rage Quit", 1250, "{killer} made {victim} rage quit.");
        addItem("obliterated", ShopCategory.KILLMESSAGE, "Obliterated", 1250, "{killer} obliterated {victim}.");
        addItem("lobby", ShopCategory.KILLMESSAGE, "Sent to Lobby", 1250, "{killer} sent {victim} back to the lobby.");
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (ShopItem item : items.values()) {
            String path = "items." + item.getId();
            cfg.set(path + ".category", item.getCategory().name());
            cfg.set(path + ".name", item.getDisplayName());
            cfg.set(path + ".price", item.getPrice());
            cfg.set(path + ".value", item.getValue());
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save shop.yml: " + ex.getMessage());
        }
    }

    public void load() {
        items.clear();
        if (!file.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("items");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String base = "items." + id;
            ShopCategory category = ShopCategory.parse(cfg.getString(base + ".category", ""));
            if (category == null) continue;
            String name = cfg.getString(base + ".name", id);
            int price = cfg.getInt(base + ".price", 0);
            String value = cfg.getString(base + ".value", "");
            items.put(id, new ShopItem(id, category, name, price, value));
        }
    }
}
