package net.skydistrict.crates.crates;

import net.skydistrict.crates.Crates;
import net.skydistrict.crates.builders.ItemBuilder;
import net.skydistrict.crates.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrateManager {
    private final Crates instance;
    private final Map<String, Crate> crates;
    private final File cratesDirectory;
    private final File defaultCrateFile;

    public CrateManager(Crates instance) {
        this.instance = instance;
        this.crates = new HashMap<>();
        this.cratesDirectory = new File(instance.getDataFolder() + File.separator + "crates");
        this.defaultCrateFile = new File(cratesDirectory + File.separator + "default.yml");
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
        if (!defaultCrateFile.exists()) {
            try {
                FileUtils.copyToFile(instance.getResource("default.yml"), defaultCrateFile);
            } catch (IOException e) {
                instance.getLogger().warning("An error occurred while trying to save a default file.");
                e.printStackTrace();
                return;
            }
        }
        for (File file : cratesDirectory.listFiles()) {
            final YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
            if (file.getName().endsWith(".yml")) {
                final String crateId = file.getName().replace(".yml", "");
                final String crateName = fc.getString("name");
                final ItemStack crateKey = ItemUtils.parseKeyCrate(fc, "crate-key", crateId);
                final ItemBuilder crateItem = new ItemBuilder(Material.CHEST).setName(crateName);
                crateItem.getPersistentDataContainer().set(Crates.CRATE_ID, PersistentDataType.STRING, crateId);
                crates.put(crateId, new Crate(crateName, crateKey, crateItem.build()));
                // Getting values
                for (String key : fc.getConfigurationSection("rewards").getKeys(false)) {
                    final String path = "rewards." + key;
                    final int weight = fc.getInt(path + ".weight");
                    final ItemStack item = (fc.isConfigurationSection(path + ".item")) ? ItemUtils.parseRewardItem(fc, path + ".item") : null;
                    final List<String> consoleCommands = (fc.isList(path + ".commands")) ? fc.getStringList(path + ".commands") : null;
                    crates.get(crateId).addReward(new Reward(weight, item, consoleCommands));
                }
                // Generating rewards pool
                crates.get(crateId).generateRewardsPool();
            }
        }
    }
}
