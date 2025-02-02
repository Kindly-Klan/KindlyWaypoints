package kindly.klan.kkwaypoints;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static final Gson gson = new Gson();
    private static final File WAYPOINTS_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "kindlywaypoints");
    private static final File WAYPOINTS_FILE = new File(WAYPOINTS_DIR, "waypoints.json");

    static {
        if (!WAYPOINTS_DIR.exists()) {
            WAYPOINTS_DIR.mkdirs();
        }
        if (!WAYPOINTS_FILE.exists()) {
            try {
                WAYPOINTS_FILE.createNewFile();
                saveToJson(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        saveToJson();
    }

    public static void removeWaypoint(String name) {
        waypoints.removeIf(waypoint -> waypoint.getName().equals(name));
        saveToJson();
    }

    public static void removeWaypoint(int x, int y, int z) {
        waypoints.removeIf(waypoint -> waypoint.getX() == x && waypoint.getY() == y && waypoint.getZ() == z);
        saveToJson();
    }

    public static void saveToJson() {
        try (FileWriter writer = new FileWriter(WAYPOINTS_FILE)) {
            gson.toJson(waypoints, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromJson() {
        if (WAYPOINTS_FILE.exists()) {
            try (FileReader reader = new FileReader(WAYPOINTS_FILE)) {
                Type waypointListType = new TypeToken<ArrayList<Waypoint>>() {}.getType();
                List<Waypoint> loadedWaypoints = gson.fromJson(reader, waypointListType);
                if (loadedWaypoints != null) {
                    waypoints.clear();
                    waypoints.addAll(loadedWaypoints);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }
}