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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Restores a player's equipped color/prefix on join (those work everywhere), and handles
 * pets, which are hub-only: given when entering the configured hub world, removed the
 * moment the player leaves it.
 */
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

    private String hubWorld() {
        return plugin.getConfig().getString("hub-world", "hub");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData pd = data.get(player.getUniqueId());

        String colorId = pd.getActive(ShopCategory.COLOR);
        if (colorId != null) {
            ShopItem item = shop.getItem(colorId);
            if (item != null) display.applyColor(player, item.getValue());
        }

        String prefixId = pd.getActive(ShopCategory.PREFIX);
        if (prefixId != null) {
            ShopItem item = shop.getItem(prefixId);
            if (item != null) display.applyPrefix(player, item.getValue());
        }

        if (player.getWorld().getName().equalsIgnoreCase(hubWorld())) {
            spawnEquippedPet(player, pd);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String hub = hubWorld();

        boolean nowInHub = player.getWorld().getName().equalsIgnoreCase(hub);
        boolean wasInHub = event.getFrom().getName().equalsIgnoreCase(hub);

        if (wasInHub && !nowInHub) {
            pets.despawnPet(player);
        } else if (nowInHub && !wasInHub) {
            spawnEquippedPet(player, data.get(player.getUniqueId()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pets.despawnPet(event.getPlayer());
    }

    private void spawnEquippedPet(Player player, PlayerData pd) {
        String petId = pd.getActive(ShopCategory.PET);
        if (petId == null) return;
        ShopItem item = shop.getItem(petId);
        if (item == null) return;
        PetType type = PetType.parse(item.getValue());
        if (type == null) return;
        // Delay slightly so the player's world/location is fully settled after join/teleport.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.getWorld().getName().equalsIgnoreCase(hubWorld())) {
                pets.spawnPet(player, type);
            }
        }, 10L);
    }
}
