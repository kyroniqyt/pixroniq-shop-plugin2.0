package dev.pixroniq.shop.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Hooks Vault's Economy service (provided by whatever economy plugin - EssentialsX, CMI,
 * etc. - is installed alongside Vault) so coins in this shop are the same money players see
 * everywhere else on the server.
 */
public class EconomyManager {

    private Economy economy;

    /** Call once on enable, after Vault (if present) has had a chance to register its service. */
    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isReady() {
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        return economy == null ? 0 : economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (economy != null) economy.depositPlayer(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /** Sets the player's balance to an exact amount by depositing/withdrawing the difference. */
    public void set(OfflinePlayer player, double amount) {
        if (economy == null) return;
        double current = economy.getBalance(player);
        if (amount > current) {
            economy.depositPlayer(player, amount - current);
        } else if (amount < current) {
            economy.withdrawPlayer(player, current - amount);
        }
    }

    public String format(double amount) {
        return economy == null ? String.valueOf(amount) : economy.format(amount);
    }
}
