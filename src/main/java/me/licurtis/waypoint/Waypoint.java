package me.licurtis.waypoint;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public final class Waypoint extends JavaPlugin {

    private WaypointManager waypointManager;

    @Override
    public void onEnable() {
        try {
            this.waypointManager = new WaypointManager(getServer().getWorlds().getFirst().getUID().toString());
        } catch (Exception e) {
            System.out.println("[Waypoint] Problem starting Waypoint plugin: " + e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.waypointManager == null) return false;

        // Only player commands for now
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        String action = "unknown";
        if (args.length > 0) {
            action = args[0];
        }
        switch (action) {
            case "get":
                this.getWaypoints(player);
                break;
            case "save":
                Optional<String> name = Optional.empty();
                if (args.length > 1) {
                    name = Optional.of(args[1]);
                }
                this.saveWaypoint(player, name);
                break;
            case "delete":
                if (args.length > 1) {
                    this.deleteWaypoint(player, args[1]);
                    break;
                }
            default:
                player.sendMessage("usage:", "/waypoint get", "/waypoint save [OPTIONAL name]", "/waypoint delete [waypoint id]");
        }
        return true;
    }

    private void getWaypoints(Player player) {
        List<WaypointRecord> waypoints = this.waypointManager.getWaypoints(player.getUniqueId().toString());

        this.sendMessage(player, "Waypoints:");
        for (WaypointRecord w : waypoints) {
            this.sendMessage(player, String.format("[%s] %s %s %s %s %s", w.id(), w.name(), w.x(), w.y(), w.z(), w.biome()));
        }
    }

    private void saveWaypoint(Player player, Optional<String> name) {
        this.waypointManager.saveWaypoint(
                player.getUniqueId().toString(),
                name.orElse(""),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ(),
                player.getLocation().getBlock().getBiome().toString()
        );
        this.sendMessage(player, "Your waypoint has been saved.");
    }

    private void deleteWaypoint(Player player, String waypointId) {
        this.waypointManager.deleteWaypoint(player.getUniqueId().toString(), waypointId);
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.GREEN + message);
    }
}
