package me.grabsky.crates;

import me.grabsky.crates.commands.CratesCommand;
import me.grabsky.crates.configuration.CratesConfig;
import me.grabsky.crates.configuration.CratesLang;
import me.grabsky.crates.crates.CratesManager;
import me.grabsky.crates.listeners.CratesListener;
import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class Crates extends JavaPlugin {
    // Instances
    private static Crates instance;
    private ConsoleLogger consoleLogger;
    private CratesConfig config;
    private CratesLang lang;
    private CratesListener crateListener;
    private CratesManager crateManager;
    // Getters
    public static Crates getInstance() {
        return instance;
    }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public CratesManager getCratesManager() {
        return crateManager;
    }



    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        // Creating instances
        this.config = new CratesConfig(this);
        this.lang = new CratesLang(this);
        // Initializing NamespacedKeys
        new CratesKeys(this);
        // Initializing CratesManager
        this.crateManager = new CratesManager(this);
        // Reloading configuration files and rewards
        this.reload();
        // Registering event
        this.crateListener = new CratesListener(this);
        this.getServer().getPluginManager().registerEvents(crateListener, this);
        // Registering command(s)
        final CommandManager commands = new CommandManager(this);
        commands.register(new CratesCommand(this));
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
