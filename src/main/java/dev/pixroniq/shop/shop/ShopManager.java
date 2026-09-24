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
