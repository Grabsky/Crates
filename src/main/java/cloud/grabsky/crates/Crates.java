package cloud.grabsky.crates;

import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.inventory.BedrockPanel;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationHolder;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import cloud.grabsky.crates.command.CratesCommand;
import cloud.grabsky.crates.command.argument.CrateArgument;
import cloud.grabsky.crates.command.argument.KeyArgument;
import cloud.grabsky.crates.command.template.CommandExceptionTemplate;
import cloud.grabsky.crates.configuration.PluginConfig;
import cloud.grabsky.crates.configuration.PluginLocale;
import cloud.grabsky.crates.crate.Crate;
import cloud.grabsky.crates.crate.Key;
import cloud.grabsky.crates.listener.CratesListener;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Crates extends BedrockPlugin {

    @Getter(AccessLevel.PUBLIC)
    private CratesManager cratesManager;

    @Getter(AccessLevel.PUBLIC)
    private RootCommandManager commandManager;

    @Getter(AccessLevel.PUBLIC)
    private ConfigurationMapper configurationMapper;

    // Used for identification of crates and keys.
    public static final NamespacedKey CRATE_NAME = new NamespacedKey("crates", "crate_name");
    public static final NamespacedKey KEY_NAME = new NamespacedKey("crates", "key_name");

    @Override
    public void onEnable() {
        super.onEnable();
        // Creating ConfigurationMapper instance.
        this.configurationMapper = PaperConfigurationMapper.create();
        // Initializing CratesManager
        this.cratesManager = new CratesManager(this);
        // Reloading configuration and shutting the server down in case it fails.
        if (this.onReload() == false)
            this.getServer().shutdown();
        // Registering event listeners.
        this.getServer().getPluginManager().registerEvents(new CratesListener(this), this);
        // Creating new instance of CrateArgument.
        final KeyArgument keyArgument = new KeyArgument(this);
        final CrateArgument crateArgument = new CrateArgument(this);
        // Creating new RootCommandManager instance= and registering commands.
        this.commandManager = new RootCommandManager(this)
                // Applying template(s)...
                .apply(CommandExceptionTemplate.INSTANCE)
                // Registering dependency(-ies)...
                .registerDependency(Crates.class, this)
                // Registering argument parser(s)...
                .setArgumentParser(Key.class, keyArgument)
                .setArgumentParser(Crate.class, crateArgument)
                .setCompletionsProvider(Key.class, keyArgument)
                .setCompletionsProvider(Crate.class, crateArgument)
                // Registering command(s)...
                .registerCommand(CratesCommand.class);
        // Register listeners required for crate preview inventories to work properly.
        BedrockPanel.registerDefaultListeners(this);
    }

    @Override
    public boolean onReload() {
        try {
            // Ensuring configuration file(s) exist.
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File localeCommands = ensureResourceExistence(this, new File(this.getDataFolder(), "locale_commands.json"));
            // Mapping configuration file(s).
            configurationMapper.map(
                    ConfigurationHolder.of(PluginConfig.class, config),
                    ConfigurationHolder.of(PluginLocale.class, locale),
                    ConfigurationHolder.of(PluginLocale.Commands.class, localeCommands)
            );
            // Reloading crates.
            this.cratesManager.reloadKeys();
            this.cratesManager.reloadCrates();
            return true;
        } catch (final ConfigurationMappingException | IllegalStateException | IOException e) {
            this.getLogger().severe("Reloading of the plugin failed due to following error(s):");
            this.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null)
                this.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            // Returning false, as plugin has failed to reload.
            return false;
        }
    }

}
