package com.mrt.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mrt.model.Station;

public class JsonLoader {

    // Data folder and file name
    private static final String DATA_DIR_NAME = "data";
    private static final String FILE_NAME = "mrt.json";

    /**
     * Gets the base directory used to locate the data folder.
     * - If running from a packaged jar, use the folder next to the jar.
     * - If running from an IDE / compiled classes, use the current working
     *   directory (usually the project root), so the existing data/mrt.json
     *   file in the project can be found and reused.
     */
    private static String getBaseDir() {
        try {
            String path = JsonLoader.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File location = new File(path);

            if (location.isFile()) {
                // Running from a packaged jar — anchor next to the jar
                return location.getParent();
            } else {
                // Running from an IDE / compiled classes — use the current
                // working directory, which is usually the project root
                return System.getProperty("user.dir");
            }
        } catch (URISyntaxException | NullPointerException e) {
            // Fall back to the current working directory if resolution fails,
            // so the program can still run
            System.out.println("Warning: could not resolve jar location, falling back to current directory.");
            return ".";
        }
    }

    private static File getDataDir() {
        return new File(getBaseDir(), DATA_DIR_NAME);
    }

    private static File getDataFile() {
        return new File(getDataDir(), FILE_NAME);
    }

    public static List<Station> loadStations() {

        List<Station> stations = new ArrayList<>();
        File file = getDataFile();

        try {
            if (!file.exists()) {
                // First run, no save file yet — this is normal, not an error
                System.out.println("mrt.json not found at " + file.getAbsolutePath() + ", starting with an empty map.");
                return stations;
            }

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            JSONArray stationArray = new JSONArray(content);

            for (int i = 0; i < stationArray.length(); i++) {
                JSONObject obj = stationArray.getJSONObject(i);

                String name = obj.getString("name");

                List<String> connectedStations = new ArrayList<>();
                JSONArray connectedArray = obj.getJSONArray("connectedStations");
                for (int j = 0; j < connectedArray.length(); j++) {
                    connectedStations.add(connectedArray.getString(j));
                }

                stations.add(new Station(name, connectedStations));
            }

        } catch (Exception e) {
            System.out.println("Failed to load mrt.json: " + e.getMessage());
            e.printStackTrace();
        }

        return stations;
    }

    public static void saveStations(Map<String, Station> stationMap) {

        File dataDir = getDataDir();

        // Make sure the data directory exists before writing,
        // otherwise FileWriter will fail
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (!created) {
                System.out.println("Failed to create data directory at " + dataDir.getAbsolutePath());
                return;
            }
        }

        JSONArray stationArray = new JSONArray();

        for (Station s : stationMap.values()) {
            JSONObject obj = new JSONObject();
            obj.put("name", s.getName());
            obj.put("connectedStations", new JSONArray(s.getConnectedStations()));
            stationArray.put(obj);
        }

        File file = getDataFile();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(stationArray.toString(2));
        } catch (IOException e) {
            System.out.println("Failed to save mrt.json: " + e.getMessage());
        }
    }
}