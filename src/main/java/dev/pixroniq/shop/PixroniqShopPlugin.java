package dev.pixroniq.shop;

import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.display.DisplayManager;
import dev.pixroniq.shop.economy.CoinCommand;
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

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        dataManager = new DataManager(this);
        shopManager = new ShopManager(this);
        displayManager = new DisplayManager();
        petManager = new PetManager(this);

        CoinCommand coinCommand = new CoinCommand(dataManager);
        getCommand("coins").setExecutor(coinCommand);
        getCommand("coins").setTabCompleter(coinCommand);

        ShopCommand shopCommand = new ShopCommand(shopManager, dataManager, displayManager, petManager);
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
