package dev.pixroniq.shop.data;

import dev.pixroniq.shop.shop.ShopCategory;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int balance;
    private final Set<String> owned = new HashSet<>();
    private final Map<ShopCategory, String> active = new EnumMap<>(ShopCategory.class);

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = Math.max(0, balance);
    }

    public Set<String> getOwned() {
        return owned;
    }

    public boolean owns(String itemId) {
        return owned.contains(itemId.toLowerCase());
    }

    public Map<ShopCategory, String> getActive() {
        return active;
    }

    public String getActive(ShopCategory category) {
        return active.get(category);
    }
}
