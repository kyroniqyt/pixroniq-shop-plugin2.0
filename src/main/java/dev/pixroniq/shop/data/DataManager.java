package dev.pixroniq.shop.data;

import dev.pixroniq.shop.PixroniqShopPlugin;
import dev.pixroniq.shop.shop.ShopCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final PixroniqShopPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> data = new HashMap<>();

    public DataManager(PixroniqShopPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        load();
    }

    public PlayerData get(UUID uuid) {
        return data.computeIfAbsent(uuid, PlayerData::new);
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (PlayerData pd : data.values()) {
            String path = "players." + pd.getUuid();
            cfg.set(path + ".owned", pd.getOwned().stream().toList());
            for (Map.Entry<ShopCategory, String> entry : pd.getActive().entrySet()) {
                cfg.set(path + ".active." + entry.getKey().name(), entry.getValue());
            }
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save playerdata.yml: " + ex.getMessage());
        }
    }

    public void load() {
        data.clear();
        if (!file.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection playersSection = cfg.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String uuidStr : playersSection.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            PlayerData pd = new PlayerData(uuid);
            String base = "players." + uuidStr;
            pd.getOwned().addAll(cfg.getStringList(base + ".owned"));

            ConfigurationSection activeSection = cfg.getConfigurationSection(base + ".active");
            if (activeSection != null) {
                for (String key : activeSection.getKeys(false)) {
                    ShopCategory category = ShopCategory.parse(key);
                    if (category != null) {
                        pd.getActive().put(category, cfg.getString(base + ".active." + key));
                    }
                }
            }
            data.put(uuid, pd);
        }
    }
}
