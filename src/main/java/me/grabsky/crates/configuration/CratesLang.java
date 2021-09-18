package me.grabsky.crates.configuration;

import me.grabsky.crates.Crates;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.lang.AbstractLang;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CratesLang extends AbstractLang {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final File file;

    public static Component COMMAND_HELP;
    public static Component USAGE_CRATES_GETCRATE;
    public static Component USAGE_CRATES_GIVE;
    public static Component USAGE_CRATES_GIVEALL;
    public static Component CRATE_NOT_FOUND;
    public static Component CRATE_PLACED;
    public static Component CRATE_MISSING_KEY;
    public static Component CRATE_OCCUPIED;
    
    public static String CRATE_KEY_RECEIVED;
    public static String CRATE_BLOCK_RECEIVED;
    public static String CRATE_OPENED;

    public CratesLang(Crates instance) {
        super(instance);
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    @Override
    public void reload() {
        // Saving default plugin translation file
        if (!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (fileConfiguration.getInt("version") != 5) {
            consoleLogger.error(Global.OUTDATED_LANG);
        }
        // Crates
        USAGE_CRATES_GETCRATE = this.component("crates.usage.getcrate");
        USAGE_CRATES_GIVE = this.component("crates.usage.give");
        USAGE_CRATES_GIVEALL = this.component("crates.usage.giveall");
        COMMAND_HELP = this.component("crates.command-help");
        CRATE_NOT_FOUND = this.component("crates.crate-not-found");
        CRATE_PLACED = this.component("crates.crate-placed");
        CRATE_OPENED = this.string("crates.crate-opened");
        CRATE_MISSING_KEY = this.component("crates.crate-missing-key");
        CRATE_KEY_RECEIVED = this.string("crates.crate-key-received");
        CRATE_BLOCK_RECEIVED = this.string("crates.crate-block-received");
        CRATE_OCCUPIED = this.component("crates.crate-occupied");
    }
}
