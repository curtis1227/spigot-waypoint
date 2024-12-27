package me.licurtis.waypoint;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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

        switch (args[0]) {
            case "save":
                this.waypointManager.saveWaypoint(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), args[6]);
                break;
            case "delete":
                this.waypointManager.deleteWaypoint(args[1], args[2]);
                break;
            case "get":
                System.out.println(this.waypointManager.getWaypoints(args[1]));
                break;
            default:
                System.out.println("Nothing to see here.");
        }

        return true;
    }
}
