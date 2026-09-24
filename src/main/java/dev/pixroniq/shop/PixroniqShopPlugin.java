package dev.pixroniq.shop;

import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.display.DisplayManager;
import dev.pixroniq.shop.economy.CoinCommand;
import dev.pixroniq.shop.economy.EconomyManager;
import dev.pixroniq.shop.kill.KillCommand;
import dev.pixroniq.shop.listeners.PlayerConnectionListener;
import dev.pixroniq.shop.pets.PetManager;
import dev.pixroniq.shop.shop.ShopCommand;
import dev.pixroniq.shop.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PixroniqShopPlugin extends JavaPlugin {

    private DataManager dataManager;
    private ShopManager shopManager;
    private DisplayManager displayManager;
    private PetManager petManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        economyManager = new EconomyManager();
        if (economyManager.setup()) {
            getLogger().info("Hooked into Vault for coins.");
        } else {
            getLogger().warning("Vault (or an economy plugin behind it) was not found. "
                    + "Coins and the shop won't work until both are installed. "
                    + "Get Vault at https://www.spigotmc.org/resources/vault.34315/ "
                    + "and an economy plugin such as EssentialsX.");
        }

        dataManager = new DataManager(this);
        shopManager = new ShopManager(this);
        shopManager.loadDefaultsIfEmpty();
        displayManager = new DisplayManager();
        petManager = new PetManager(this);

        CoinCommand coinCommand = new CoinCommand(economyManager);
        getCommand("coins").setExecutor(coinCommand);
        getCommand("coins").setTabCompleter(coinCommand);

        ShopCommand shopCommand = new ShopCommand(this, shopManager, dataManager, displayManager, petManager, economyManager);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        getCommand("killmsg").setExecutor(new KillCommand(dataManager, shopManager));

        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(this, dataManager, shopManager, displayManager, petManager), this);

        getLogger().info("PixroniqShop enabled: " + shopManager.getItems().size() + " shop items loaded.");
    }

    @Override
    public void onDisable() {
        if (petManager != null) petManager.despawnAll();
        if (dataManager != null) dataManager.save();
        if (shopManager != null) shopManager.save();
    }
}
