package me.grabsky.crates;

import me.grabsky.crates.commands.CratesCommand;
import me.grabsky.crates.configuration.Config;
import me.grabsky.crates.configuration.Lang;
import me.grabsky.crates.crates.CrateManager;
import me.grabsky.crates.listeners.CrateListener;
import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class Crates extends JavaPlugin {
    // Instances
    private static Crates instance;
    private ConsoleLogger consoleLogger;
    private Config config;
    private Lang lang;
    private CrateListener crateListener;
    private CrateManager crateManager;
    // Getters
    public static Crates getInstance() {
        return instance;
    }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public CrateManager getCratesManager() {
        return crateManager;
    }


    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        // Creating instances
        this.config = new Config(this);
        this.lang = new Lang(this);
        this.crateManager = new CrateManager(this);
        // Reloading configuration files and rewards
        this.reload();
        // Registering event
        this.crateListener = new CrateListener(this);
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
