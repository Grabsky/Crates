package net.skydistrict.crates;

import net.skydistrict.crates.commands.CratesCommand;
import net.skydistrict.crates.configuration.Config;
import net.skydistrict.crates.configuration.Lang;
import net.skydistrict.crates.crates.CrateManager;
import net.skydistrict.crates.listeners.CrateListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class Crates extends JavaPlugin {
    // Instances
    private static Crates instance;
    private Config config;
    private Lang lang;
    private CrateListener crateListener;
    private CrateManager crateManager;

    // Getters
    public static Crates getInstance() {
        return instance;
    }
    public CrateManager getCratesManager() {
        return crateManager;
    }
    public static NamespacedKey CRATE_ID;

    @Override
    public void onEnable() {
        instance = this;
        CRATE_ID = new NamespacedKey(this, "crateId");
        // Creating instances
        this.config = new Config(this);
        this.lang = new Lang(this);
        this.crateManager = new CrateManager(this);
        // Reloading configuration files and rewards
        this.reload();
        // Registering event
        this.crateListener = new CrateListener(this);
        this.getServer().getPluginManager().registerEvents(crateListener, this);
        // Registering command
        CratesCommand cratesCommand = new CratesCommand(this);
        this.getCommand("crates").setExecutor(cratesCommand);
        this.getCommand("crates").setTabCompleter(cratesCommand);
    }

    @Override
    public void onDisable() {
        // Removing display item
        crateListener.removeDisplayItem();
    }

    public boolean reload() {
        config.reload();
        lang.reload();
        crateManager.reloadRewards();
        return true;
    }
}
