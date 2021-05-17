package net.skydistrict.crates.configuration;

import me.grabsky.indigo.adventure.MiniMessage;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skydistrict.crates.Crates;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Lang {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final File file;
    private FileConfiguration fileConfiguration;

    public static Component PLAYER_NOT_FOUND;
    public static Component MISSING_PERMISSIONS;
    public static Component PLAYER_ONLY;
    public static Component RELOAD_SUCCESS;
    public static Component RELOAD_FAIL;
    public static Component NO_SPACE;
    public static Component COMMAND_HELP;
    public static Component CRATE_NOT_FOUND;
    public static Component CRATE_PLACED;
    public static Component CRATE_MISSING_KEY;
    public static Component CRATE_OCCUPIED;
    
    public static String CRATE_KEY_RECEIVED;
    public static String CRATE_BLOCK_RECEIVED;
    public static String CRATE_OPENED;

    public Lang(Crates instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    public void reload() {
        // Saving default plugin translation file
        if (!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (fileConfiguration.getInt("version") != 2) {
            consoleLogger.error("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        // General
        PLAYER_NOT_FOUND = this.component("general.player-not-found");
        MISSING_PERMISSIONS = this.component("general.missing-permissions");
        PLAYER_ONLY = this.component("general.player-only");
        RELOAD_SUCCESS = this.component("general.reload-success");
        RELOAD_FAIL = this.component("general.reload-fail");
        NO_SPACE = this.component("general.no-space");
        // Crates
        COMMAND_HELP = this.component("crates.command-help");
        CRATE_NOT_FOUND = this.component("crates.crate-not-found");
        CRATE_PLACED = this.component("crates.crate-placed");
        CRATE_OPENED = this.string("crates.crate-opened");
        CRATE_MISSING_KEY = this.component("crates.crate-missing-key");
        CRATE_KEY_RECEIVED = this.string("crates.crate-key-received");
        CRATE_BLOCK_RECEIVED = this.string("crates.crate-block-received");
        CRATE_OCCUPIED = this.component("crates.crate-occupied");
    }

    private String string(String path) {
        final StringBuilder sb = new StringBuilder();
        if (fileConfiguration.isList(path)) {
            final List<String> list = fileConfiguration.getStringList(path);
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i + 1 != list.size()) {
                    sb.append("\n");
                }
            }
        } else {
            sb.append(fileConfiguration.getString(path));
        }
        return sb.toString();
    }

    private Component component(String path) {
        return LegacyComponentSerializer.legacySection().deserialize(this.string(path));
    }

    /** Sends parsed component */
    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }

    /** Parses and sends component */
    public static void send(@NotNull CommandSender sender, @NotNull String text) {
        final Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }
}
