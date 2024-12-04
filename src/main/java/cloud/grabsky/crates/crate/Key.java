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
package cloud.grabsky.crates.crate;

import cloud.grabsky.crates.Crates;
import com.squareup.moshi.Json;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Key {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Json(name = "display_name")
    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String displayName;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull ItemStack item;

    @Json(ignore = true)
    private boolean isInitialized;

    public void initialize() {
        if (this.isInitialized == true)
            throw new IllegalStateException("KEY_ALREADY_INITIALIZED");
        // Applying PDC data to the key item.
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Crates.KEY_NAME, PersistentDataType.STRING, name);
        });
        // Marking key as initialized.
        isInitialized = true;
    }
}
