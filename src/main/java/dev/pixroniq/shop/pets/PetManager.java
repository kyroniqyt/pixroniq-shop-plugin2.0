package dev.pixroniq.shop.pets;

import dev.pixroniq.shop.PixroniqShopPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetManager {

    private final PixroniqShopPlugin plugin;
    private final Map<UUID, Entity> activePets = new HashMap<>();
    private final Map<UUID, BukkitTask> followTasks = new HashMap<>();

    public PetManager(PixroniqShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnPet(Player owner, PetType type) {
        despawnPet(owner);

        Location loc = owner.getLocation();
        Entity entity = owner.getWorld().spawnEntity(loc, type.getEntityType());
        entity.setPersistent(false);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCustomName(owner.getName() + "'s " + type.getLabel());
        entity.setCustomNameVisible(false);

        if (entity instanceof Mob mob) {
            mob.setAI(false); // we drive movement ourselves so it can't wander off or fight anything
        }

        activePets.put(owner.getUniqueId(), entity);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!owner.isOnline() || entity.isDead()) {
                despawnPet(owner);
                return;
            }
            if (!owner.getWorld().equals(entity.getWorld())) {
                entity.teleport(owner.getLocation());
                return;
            }
            double distance = entity.getLocation().distance(owner.getLocation());
            if (distance > 12) {
                entity.teleport(owner.getLocation());
            } else if (distance > 2.5 && entity instanceof Mob mob) {
                mob.getPathfinder().moveTo(owner.getLocation(), 1.2);
            }
        }, 10L, 5L);

        followTasks.put(owner.getUniqueId(), task);
    }

    public void despawnPet(Player owner) {
        despawnPet(owner.getUniqueId());
    }

    public void despawnPet(UUID ownerId) {
        BukkitTask task = followTasks.remove(ownerId);
        if (task != null) task.cancel();

        Entity entity = activePets.remove(ownerId);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    public boolean hasPet(Player owner) {
        return activePets.containsKey(owner.getUniqueId());
    }

    /** Call on plugin disable to clean up any pets left in the world. */
    public void despawnAll() {
        for (UUID ownerId : new HashMap<>(activePets).keySet()) {
            despawnPet(ownerId);
        }
    }
}
