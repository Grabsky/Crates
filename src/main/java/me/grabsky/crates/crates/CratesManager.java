package me.grabsky.crates.crates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.grabsky.crates.Crates;
import me.grabsky.crates.crates.json.JsonCrate;
import me.grabsky.crates.crates.json.JsonReward;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CratesManager {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final Gson gson;
    private final Map<String, Crate> crates;
    private final File cratesDirectory;
    private final File defaultCrateFile;

    private List<String> crateIds;

    public CratesManager(Crates instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping()
                .create();
        this.crates = new HashMap<>();
        this.cratesDirectory = new File(instance.getDataFolder() + File.separator + "crates");
        this.defaultCrateFile = new File(cratesDirectory + File.separator + "example.json");
    }

    public Crate getCrate(String id) {
        return crates.get(id);
    }

    public List<String> getCrateIds() {
        return crateIds;
    }

    public void reloadRewards() {
        // Making sure the Map is empty
        crates.clear();
        // Trying to create directory if doesn't exist
        cratesDirectory.mkdirs();
        // Saving default file
        try {
            // Saving default file (replacing if exists)
            Files.copy(instance.getResource("example.json"), defaultCrateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            consoleLogger.error("An error occurred while trying to save a default file.");
            e.printStackTrace();
            return;
        }
        // Loading crates
        for (final File file : cratesDirectory.listFiles()) {
            // Skipping null files (?) and example.json
            if (file == null || !file.getName().endsWith(".json") || file.getName().equals("example.json")) continue;
            int loadedRewards = 0;
            try {
                final BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
                final JsonCrate jsonCrate = gson.fromJson(bufferedReader, JsonCrate.class);
                if (jsonCrate != null) {
                    final String id = file.getName().replace(".json", "");
                    final ItemStack keyItem = jsonCrate.getCrateKeyItem().toItemStack();
                    if (keyItem != null) {
                        final Crate crate = new Crate(id, jsonCrate.getName(), jsonCrate.getPreviewName(), keyItem);
                        for (final JsonReward jsonReward : jsonCrate.getJsonRewards()) {
                            crate.addReward(jsonReward.toReward());
                            loadedRewards++;
                        }
                        crates.put(id, crate);
                        consoleLogger.success("Loaded crate " + id + " with " + loadedRewards + " rewards.");
                        continue;
                    }
                    consoleLogger.error("An error occurred while trying to load '" + id + "' crate file. Key item is null.");
                }
            } catch (IOException e) {
                consoleLogger.error("An error occurred while trying to load '" + file.getName() + "' crate file.");
                e.printStackTrace();
            }
        }
        crateIds = crates.keySet().stream().sorted().toList();
    }
}
