package net.skydistrict.crates.crates;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.utils.Parser;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrateManager {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final Map<String, Crate> crates;
    private final File cratesDirectory;
    private final File defaultCrateFile;

    public static NamespacedKey CRATE_ID;

    public CrateManager(Crates instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.crates = new HashMap<>();
        this.cratesDirectory = new File(instance.getDataFolder() + File.separator + "crates");
        this.defaultCrateFile = new File(cratesDirectory + File.separator + "example.yml");
        CRATE_ID = new NamespacedKey(instance, "crateId");
    }

    public Crate getCrate(String id) {
        return crates.get(id);
    }

    public Set<String> getCrateIds() {
        return crates.keySet();
    }

    public void reloadRewards() {
        // Making sure the Map is empty
        crates.clear();
        // Trying to create directory if doesn't exist
        cratesDirectory.mkdirs();
        // Saving default file
        try {
            // Saving default file (replacing if exists)
            Files.copy(instance.getResource("example.yml"), defaultCrateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            consoleLogger.error("An error occurred while trying to save a default file.");
            e.printStackTrace();
            return;
        }
        // Loading crates
        int loaded = 0;
        for (File file : cratesDirectory.listFiles()) {
            final YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
            if (file.getName().endsWith(".yml") && !file.getName().equals("example.yml")) {
                final String crateId = file.getName().replace(".yml", "");
                final String crateName = fc.getString("name");
                final ItemStack crateKey = Parser.keyItem(fc, "crate-key", crateId);
                final ItemBuilder crateItem = new ItemBuilder(Material.CHEST).setName(crateName);
                crateItem.getPersistentDataContainer().set(CRATE_ID, PersistentDataType.STRING, crateId);
                crates.put(crateId, new Crate(crateName, crateKey, crateItem.build()));
                // Getting values
                for (String key : fc.getConfigurationSection("rewards").getKeys(false)) {
                    final String path = "rewards." + key;
                    final int weight = fc.getInt(path + ".weight");
                    final ItemStack item = (fc.isConfigurationSection(path + ".item")) ? Parser.rewardItem(fc, path + ".item") : null;
                    final List<String> consoleCommands = (fc.isList(path + ".commands")) ? fc.getStringList(path + ".commands") : null;
                    crates.get(crateId).addReward(new Reward(weight, item, consoleCommands));
                }
                // Generating rewards pool
                crates.get(crateId).generateRewardsPool();
            }
            loaded++;
        }
        consoleLogger.success("Loaded " + loaded + " crates.");
    }
}
