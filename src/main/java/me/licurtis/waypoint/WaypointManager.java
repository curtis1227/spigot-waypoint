package me.licurtis.waypoint;

import com.google.common.io.Files;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

public final class WaypointManager {

    private final String worldId;
    private final HashMap<String, JsonObject> playerJsons;

    public WaypointManager(String worldId) throws Exception {
        this.worldId = worldId;
        this.playerJsons = loadPlayerJsons(worldId);
    }

    public List<WaypointRecord> getWaypoints(String playerId) {
        JsonObject playerJson = this.playerJsons.get(playerId);
        if (playerJson == null) {
            return new ArrayList<>();
        }

        List<WaypointRecord> waypointRecords = new ArrayList<>();
        JsonObject playerWaypoints = playerJson.getAsJsonObject("waypoints");
        for (Map.Entry<String, JsonElement> waypointEntry : playerWaypoints.entrySet()) {
            JsonObject waypoint = waypointEntry.getValue().getAsJsonObject();

            waypointRecords.add(
                    new WaypointRecord(
                            this.worldId,
                            playerId,
                            waypointEntry.getKey(),
                            waypoint.get("name").getAsString(),
                            waypoint.get("x").getAsInt(),
                            waypoint.get("y").getAsInt(),
                            waypoint.get("z").getAsInt(),
                            waypoint.get("biome").getAsString()
                    )
            );
        }

        return waypointRecords;
    }

    public void saveWaypoint(String playerId, String name, int x, int y, int z, String biome) {
        JsonObject newWaypoint = createWaypoint(name, x, y, z, biome);

        JsonObject playerJson = this.playerJsons.get(playerId);
        if (playerJson == null) {
            this.createPlayerJson(playerId);
            playerJson = this.playerJsons.get(playerId);
        }

        addWaypoint(playerJson, newWaypoint);

        this.save(playerId);
    }

    public void deleteWaypoint(String playerId, String id) {
        JsonObject playerJson = this.playerJsons.get(playerId);
        if (playerJson == null) {
            System.out.println("[Waypoint] Attempting to delete for non-existent player data " + playerId);
            return;
        }

        JsonObject playerWaypoints = playerJson.getAsJsonObject("waypoints");
        if (!playerWaypoints.has(id)) {
            System.out.println("[Waypoint] Attempting to delete non-existent waypoint " + id + " for player " + playerId);
            return;
        }

        playerWaypoints.remove(id);

        this.save(playerId);
    }

    private void createPlayerJson(String playerId) {
        JsonObject newPlayerJson = new JsonObject();
        newPlayerJson.add("waypoints", new JsonObject());
        newPlayerJson.addProperty("lastWaypointId", 0);

        this.playerJsons.put(playerId, newPlayerJson);
    }

    private void save(String playerId) {
        JsonObject playerJson = this.playerJsons.get(playerId);
        if (playerJson == null) {
            System.out.println("[Waypoint] Cannot save for null playerJson " + playerId);
            return;
        }

        try (FileWriter fileWriter = new FileWriter(this.getPlayerFile(playerId))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(playerJson, fileWriter);
        } catch (Exception e) {
            System.out.println("[Waypoint] Could not write to file for player " + playerId);
        }
    }

    private File getPlayerFile(String playerId) throws Exception {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Waypoint");
        if (plugin == null) {
            throw new Exception("[Waypoint] Could not get Waypoint plugin.");
        }

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File worldFolder = new File(plugin.getDataFolder(), this.worldId);
        if (!worldFolder.exists()) {
            worldFolder.mkdir();
        }

        File playerFile = new File(worldFolder, playerId + ".json");
        if (!playerFile.exists()) {
            playerFile.createNewFile();
        }

        return playerFile;
    }

    private static HashMap<String, JsonObject> loadPlayerJsons(String worldId) throws Exception {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Waypoint");
        if (plugin == null) {
            throw new Exception("[Waypoint] Could not get Waypoint plugin.");
        }


        File worldFolder = new File(plugin.getDataFolder(), worldId);
        if (!worldFolder.exists()) {
            return new HashMap<>();
        }

        HashMap<String, JsonObject> playerJsons = new HashMap<>();

        for (File playerFile : Objects.requireNonNull(worldFolder.listFiles())) {
            if (playerFile.isFile()) {
                String playerId = Files.getNameWithoutExtension(playerFile.getName());
                JsonObject playerJson = JsonParser.parseReader(new FileReader(playerFile)).getAsJsonObject();
                playerJsons.put(playerId, playerJson);
            }
        }

        return playerJsons;
    }

    private static JsonObject createWaypoint(String name, int x, int y, int z, String biome) {
        JsonObject newWaypoint = new JsonObject();
        newWaypoint.addProperty("name", name);
        newWaypoint.addProperty("x", x);
        newWaypoint.addProperty("y", y);
        newWaypoint.addProperty("z", z);
        newWaypoint.addProperty("biome", biome);
        return newWaypoint;
    }

    private static void addWaypoint(JsonObject playerJson, JsonObject newWaypoint) {
        JsonObject playerWaypoints = playerJson.getAsJsonObject("waypoints");
        int lastId = playerJson.get("lastWaypointId").getAsInt();

        playerWaypoints.add(String.valueOf(lastId + 1), newWaypoint);
        playerJson.addProperty("lastWaypointId", lastId + 1);
    }
}
