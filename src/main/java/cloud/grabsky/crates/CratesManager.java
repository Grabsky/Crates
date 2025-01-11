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

import cloud.grabsky.configuration.adapter.AbstractEnumJsonAdapter;
import cloud.grabsky.configuration.paper.adapter.ComponentAdapter;
import cloud.grabsky.configuration.paper.adapter.EnchantmentAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.EnchantmentEntryAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.EntityTypeAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.ItemFlagAdapter;
import cloud.grabsky.configuration.paper.adapter.ItemStackAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.MaterialAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import cloud.grabsky.configuration.paper.adapter.PersistentDataEntryAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.PersistentDataTypeAdapterFactory;
import cloud.grabsky.configuration.paper.util.Resources;
import cloud.grabsky.crates.configuration.adapter.ListAdapterFactory;
import cloud.grabsky.crates.crate.Crate;
import cloud.grabsky.crates.crate.Key;
import cloud.grabsky.crates.crate.Reward;
import com.squareup.moshi.Moshi;
import net.kyori.adventure.text.Component;
import okio.BufferedSource;
import okio.Okio;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CratesManager {
    private final Crates plugin;
    private final Moshi moshi;

    private final File defaultKeyFile;
    private final File defaultCrateFile;
    private final File keysDirectory;
    private final File cratesDirectory;

    private final Map<String, Crate> crates;
    private final Map<String, Key> keys;

    public CratesManager(Crates plugin) {
        this.plugin = plugin;
        // Creating a new instance of Moshi.
        this.moshi = new Moshi.Builder()
                // Everything needed for ItemStack deserialization...
                .add(Component.class, ComponentAdapter.INSTANCE)
                .add(ItemFlag.class, ItemFlagAdapter.INSTANCE)
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(EnchantmentAdapterFactory.INSTANCE)
                .add(EnchantmentEntryAdapterFactory.INSTANCE)
                .add(EntityTypeAdapterFactory.INSTANCE)
                .add(ItemStackAdapterFactory.INSTANCE)
                .add(MaterialAdapterFactory.INSTANCE)
                .add(PersistentDataEntryAdapterFactory.INSTANCE)
                .add(PersistentDataTypeAdapterFactory.INSTANCE)
                .add(ListAdapterFactory.INSTANCE)
                .add(Reward.RewardFunction.class, new AbstractEnumJsonAdapter<>(Reward.RewardFunction.class, false) {})
                // Building...
                .build();
        this.keysDirectory = new File(plugin.getDataFolder() + File.separator + "keys");
        this.cratesDirectory = new File(plugin.getDataFolder() + File.separator + "crates");
        this.defaultCrateFile = new File(cratesDirectory + File.separator + "example_crate.json");
        this.defaultKeyFile = new File(keysDirectory + File.separator + "example_key.json");
        this.keys = new HashMap<>();
        this.crates = new HashMap<>();
    }

    public @Nullable Key getKey(final @NotNull String name) {
        return keys.get(name);
    }

    public @Nullable Crate getCrate(final @NotNull String name) {
        return crates.get(name);
    }

    public void reloadKeys() {
        // Clearing maps before populating them again.
        keys.clear();
        // Ensuring 'plugins/Crates/keys' directory exists and saving default file if applicable.
        if (keysDirectory.mkdirs() == true) {
            try {
                Resources.ensureResourceExistence(plugin, defaultKeyFile);
            } catch (IOException e) {
                plugin.getLogger().severe("An error occurred while trying to save default key file.");
                plugin.getLogger().severe("  " + e.getMessage());
                return;
            }
        }
        // Listing files inside the directory.
        final @Nullable File[] files = keysDirectory.listFiles();
        // Returning amd logging a message if directory is empty.
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No keys have been defined inside '" + keysDirectory + "' directory.");
            return;
        }
        // Iterating over .json files in the 'plugins/Crates/keys/' directory and loading keys.
        for (final File file : files) {
            // Skipping files we cannot process.
            if (file == null || file.getName().endsWith(".json") == false)
                continue;
            try {
                // Creating new BufferedSource instance from the file.
                final BufferedSource source = Okio.buffer(Okio.source(file));
                // Converting file contents to a Key instance.
                final @Nullable Key key = moshi.adapter(Key.class).lenient().nullSafe().fromJson(source);
                // Throwing exception in case key ended up being null.
                if (key == null)
                    throw new IOException("null");
                // Initializing the crate.
                key.initialize();
                // Putting Key instance to the map.
                keys.put(key.getName(), key);
                // Logging info to the console.

            } catch (final IOException | RuntimeException e) {
                plugin.getLogger().severe("Loading of key '" + file.getName() + "' failed due to following error(s):");
                plugin.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
                if (e.getCause() != null)
                    plugin.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            }
            plugin.getLogger().info("Loaded " + keys.size() + " keys.");
        }
    }

    public void reloadCrates() {
        // Clearing maps before populating them again.
        crates.clear();
        // Ensuring 'plugins/Crates/crates' directory exists and saving default file if applicable.
        if (cratesDirectory.mkdirs() == true) {
            try {
                Resources.ensureResourceExistence(plugin, defaultCrateFile);
            } catch (IOException e) {
                plugin.getLogger().severe("An error occurred while trying to save default crate file.");
                plugin.getLogger().severe("  " + e.getMessage());
                return;
            }
        }
        // Listing files inside the directory.
        final @Nullable File[] files = cratesDirectory.listFiles();
        // Returning amd logging a message if directory is empty.
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No crates have been defined inside '" + cratesDirectory + "' directory.");
            return;
        }
        // Iterating over .json files in the 'plugins/Crates/crates/' directory and loading crates.
        for (final File file : files) {
            // Skipping files we cannot process.
            if (file == null || file.getName().endsWith(".json") == false)
                continue;
            try {
                // Creating new BufferedSource instance from the file.
                final BufferedSource source = Okio.buffer(Okio.source(file));
                // Converting file contents to a Crate instance.
                final @Nullable Crate crate = moshi.adapter(Crate.class).lenient().nullSafe().fromJson(source);
                // Throwing exception in case crate ended up being null.
                if (crate == null)
                    throw new IOException("null");
                // Initializing the crate.
                crate.initialize();
                // Putting Crate instance to the map.
                crates.put(crate.getName(), crate);
                // Logging info to the console.
                plugin.getLogger().info("Loaded crate " + crate.getName() + " with " + crate.getRewards().size() + " rewards.");
            } catch (final IOException | RuntimeException e) {
                plugin.getLogger().severe("Loading of crate '" + file.getName() + "' failed due to following error(s):");
                plugin.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
                if (e.getCause() != null)
                    plugin.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            }
        }
    }

    public @NotNull Collection<String> getKeyNames() {
        return keys.keySet();
    }

    public @NotNull Collection<String> getCrateNames() {
        return crates.keySet();
    }

}
