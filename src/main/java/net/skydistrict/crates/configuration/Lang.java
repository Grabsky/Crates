package net.skydistrict.crates.configuration;

import me.grabsky.indigo.adventure.MiniMessage;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.configuration.components.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

public class Lang {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final File file;
    private static final int currentVersion = 1;

    public static Message
            PLAYER_NOT_FOUND,
            MISSING_PERMISSIONS,
            PLAYER_ONLY,
            RELOAD_SUCCESS,
            RELOAD_FAIL,
            NO_SPACE,
            COMMAND_HELP,
            CRATE_NOT_FOUND,
            CRATE_PLACED,
            CRATE_OPENED,
            CRATE_MISSING_KEY,
            CRATE_KEY_RECEIVED,
            CRATE_BLOCK_RECEIVED,
            CRATE_OCCUPIED;

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
        final FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != currentVersion) {
            consoleLogger.error("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        // General
        PLAYER_NOT_FOUND = this.message(fc, "general.player-not-found", true);
        MISSING_PERMISSIONS = this.message(fc, "general.missing-permissions", true);
        PLAYER_ONLY = this.message(fc, "general.player-only", true);
        RELOAD_SUCCESS = this.message(fc, "general.reload-success", true);
        RELOAD_FAIL = this.message(fc, "general.reload-fail", true);
        NO_SPACE = this.message(fc, "general.no-space", true);
        // Crates
        COMMAND_HELP = this.message(fc, "crates.command-help", true);
        CRATE_NOT_FOUND = this.message(fc, "crates.crate-not-found", true);
        CRATE_PLACED = this.message(fc, "crates.crate-placed", true);
        CRATE_OPENED = this.message(fc, "crates.crate-opened", false);
        CRATE_MISSING_KEY = this.message(fc, "crates.crate-missing-key", true);
        CRATE_KEY_RECEIVED = this.message(fc, "crates.crate-key-received", false);
        CRATE_BLOCK_RECEIVED = this.message(fc, "crates.crate-block-received", false);
        CRATE_OCCUPIED = this.message(fc, "crates.crate-occupied", true);
    }

    /** Returns Message value from given path */
    public Message message(FileConfiguration fc, String path, boolean compile) {
        final StringBuilder builder = new StringBuilder();
        if (fc.isList(path)) {
            final List<String> list = fc.getStringList(path);
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i));
                if (i + 1 != list.size()) {
                    builder.append("\n");
                }
            }
        } else {
            builder.append(fc.getString(path));
        }
        if (compile) return new Message(MiniMessage.get().parse(builder.toString()));
        return new Message(builder.toString());
    }

    /** Sends message with placeholders (compiled just before sending) */
    public static void send(CommandSender sender, @NotNull Message message, Object... replacements) {
        final String string = message.getString();
        if (string != null && !string.equals("")) {
            sender.sendMessage(MiniMessage.get().parse(MessageFormat.format(string, replacements)));
        }
    }

    /** Sends compiled (static) message */
    public static void send(CommandSender sender, Message message) {
        final Component component = message.getComponent();
        if (component != null && component != Component.empty()) {
            sender.sendMessage(component);
        }
    }
}
