package dev.pixroniq.shop.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Gives each player their own scoreboard team so their tab-list name can carry a color and a
 * prefix independently of anything else on the server. One team per player, named from a hash
 * of their UUID to stay unique and under the legacy 16-character team-name limit.
 *
 * Note: this changes the tab list and the nametag above the player's head. Whether it also
 * changes chat depends on how your chat plugin (if any) formats messages - most chat plugins
 * that read the player's scoreboard team prefix will pick this up automatically, but a chat
 * plugin with its own prefix system may need to be pointed at this team's prefix instead.
 */
public class DisplayManager {

    private Team getOrCreateTeam(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "pq" + Integer.toHexString(player.getUniqueId().hashCode());
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        return team;
    }

    /** Sets the name color. Pass null to clear it without touching the prefix. */
    public void applyColor(Player player, String colorValue) {
        Team team = getOrCreateTeam(player);
        if (colorValue == null || colorValue.isBlank()) {
            team.setColor(ChatColor.RESET);
            return;
        }
        ChatColor color = parseColor(colorValue);
        if (color != null) {
            team.setColor(color);
        }
    }

    /** Sets the prefix shown before the name. Pass null to clear it without touching the color. */
    public void applyPrefix(Player player, String prefixValue) {
        Team team = getOrCreateTeam(player);
        if (prefixValue == null || prefixValue.isBlank()) {
            team.setPrefix("");
            return;
        }
        String prefix = ChatColor.translateAlternateColorCodes('&', prefixValue);
        if (prefix.length() > 64) prefix = prefix.substring(0, 64);
        team.setPrefix(prefix);
    }

    private ChatColor parseColor(String value) {
        String v = value.trim();
        if (v.startsWith("&") && v.length() == 2) {
            ChatColor c = ChatColor.getByChar(v.charAt(1));
            if (c != null) return c;
        }
        if (v.length() == 1) {
            ChatColor c = ChatColor.getByChar(v.charAt(0));
            if (c != null) return c;
        }
        try {
            return ChatColor.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
