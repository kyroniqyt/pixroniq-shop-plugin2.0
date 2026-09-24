package dev.pixroniq.shop.listeners;

import dev.pixroniq.shop.PixroniqShopPlugin;
import dev.pixroniq.shop.data.DataManager;
import dev.pixroniq.shop.data.PlayerData;
import dev.pixroniq.shop.display.DisplayManager;
import dev.pixroniq.shop.pets.PetManager;
import dev.pixroniq.shop.pets.PetType;
import dev.pixroniq.shop.shop.ShopCategory;
import dev.pixroniq.shop.shop.ShopItem;
import dev.pixroniq.shop.shop.ShopManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final PixroniqShopPlugin plugin;
    private final DataManager data;
    private final ShopManager shop;
    private final DisplayManager display;
    private final PetManager pets;

    public PlayerConnectionListener(PixroniqShopPlugin plugin, DataManager data, ShopManager shop,
                                     DisplayManager display, PetManager pets) {
        this.plugin = plugin;
        this.data = data;
        this.shop = shop;
        this.display = display;
        this.pets = pets;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData pd = data.get(event.getPlayer().getUniqueId());

        String colorId = pd.getActive(ShopCategory.COLOR);
        if (colorId != null) {
            ShopItem item = shop.getItem(colorId);
            if (item != null) display.applyColor(event.getPlayer(), item.getValue());
        }

        String prefixId = pd.getActive(ShopCategory.PREFIX);
        if (prefixId != null) {
            ShopItem item = shop.getItem(prefixId);
            if (item != null) display.applyPrefix(event.getPlayer(), item.getValue());
        }

        String petId = pd.getActive(ShopCategory.PET);
        if (petId != null) {
            ShopItem item = shop.getItem(petId);
            if (item != null) {
                PetType type = PetType.parse(item.getValue());
                if (type != null) {
                    // Delay slightly so the player's world/location is fully settled after join.
                    plugin.getServer().getScheduler().runTaskLater(plugin,
                            () -> pets.spawnPet(event.getPlayer(), type), 20L);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pets.despawnPet(event.getPlayer());
    }
}
