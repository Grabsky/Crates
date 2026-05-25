/*
 * Crates (https://github.com/Grabsky/Crates)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.crates;

import cloud.grabsky.bedrock.BedrockScheduler;
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
import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Crates extends JavaPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static Crates instance;

    @Getter(AccessLevel.PUBLIC)
    private BedrockScheduler bedrockScheduler;

    @Getter(AccessLevel.PUBLIC)
    private ConfigurationMapper configurationMapper;

    @Getter(AccessLevel.PUBLIC)
    private CratesManager cratesManager;

    @Getter(AccessLevel.PUBLIC)
    private RootCommandManager commandManager;

    // Used to identify crates and keys.
    public static final NamespacedKey CRATE_NAME = new NamespacedKey("crates", "crate_name");
    public static final NamespacedKey KEY_NAME = new NamespacedKey("crates", "key_name");

    @Override
    public void onEnable() {
        instance = this;
        // Creating BedrockScheduler instance.
        this.bedrockScheduler = new BedrockScheduler(this);
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
        // Registering PlaceholderAPI placeholders...
        Placeholders.INSTANCE.register();
    }

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


    public static final class Placeholders extends PlaceholderExpansion {
        public static final Placeholders INSTANCE = new Placeholders(); // SINGLETON

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public @NotNull String getAuthor() {
            return "Grabsky";
        }

        @Override
        public @NotNull String getIdentifier() {
            return "crates";
        }

        @Override
        public @NotNull String getVersion() {
            return Crates.getInstance().getPluginMeta().getVersion();
        }

        @Override
        public String onRequest(final @NotNull OfflinePlayer offlinePlayer, final @NotNull String params) {
            // Placeholder: %crates_opened_count_[CRATE]%
            if (params.startsWith("opened_count_") == true && offlinePlayer.isConnected() == true) {
                final String crateId = params.replace("opened_count_", "");
                final Long count = offlinePlayer.getPersistentDataContainer().getOrDefault(new NamespacedKey("crates", "opened/" + crateId), PersistentDataType.LONG, 0L);
                return String.valueOf(count);
            }
            return null;
        }
    }


    // Downloading libraries directly from Maven Central may be considered a violation of their Terms of Service.
    // Usage of PaperMC repository is highly discouraged, and this leaves Google's mirror as the most reliable option.
    @SuppressWarnings("UnstableApiUsage")
    public static final class PluginLoader implements io.papermc.paper.plugin.loader.PluginLoader {
        private static String MAVEN_CENTRAL_DEFAULT_MIRROR = "https://maven-central.storage-download.googleapis.com/maven2";

        static {
            try {
                // Replacing Maven Central repository with a pre-configured mirror.
                // See: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/#loaders
                MAVEN_CENTRAL_DEFAULT_MIRROR = (String) MavenLibraryResolver.class.getField("MAVEN_CENTRAL_DEFAULT_MIRROR").get(null);
            } catch (final NoSuchFieldError | NoSuchFieldException | IllegalAccessException e) {
                // NO OVERRIDE; IGNORE
            }
        }

        @Override
        public void classloader(final @NotNull PluginClasspathBuilder classpathBuilder) throws IllegalStateException {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Parsing the JSON file generated by 'plugin-yml' Gradle plugin. (https://github.com/eldoriarpg/plugin-yml)
            try (final InputStream in = getClass().getResourceAsStream("/paper-libraries.json")) {
                final PluginLibraries libraries = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
                // Adding repositories and dependencies to the maven library resolver.
                libraries.asRepositories().forEach(resolver::addRepository);
                libraries.asDependencies().forEach(resolver::addDependency);
                // Adding library resolver to the classpath builder.
                classpathBuilder.addLibrary(resolver);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private record PluginLibraries(Map<String, String> repositories, List<String> dependencies) {

            public Stream<RemoteRepository> asRepositories() {
                return repositories.entrySet().stream().map(entry -> {
                    if (entry.getValue().contains("maven.org") == true || entry.getValue().contains("maven.apache.org") == true) {
                        return new RemoteRepository.Builder(entry.getKey(), "default", MAVEN_CENTRAL_DEFAULT_MIRROR).build();
                    }
                    return new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build();
                });
            }

            public Stream<Dependency> asDependencies() {
                return dependencies.stream().map(value -> new Dependency(new DefaultArtifact(value), null));
            }
        }

    }

}
