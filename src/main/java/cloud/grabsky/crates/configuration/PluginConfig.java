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
package cloud.grabsky.crates.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.crates.util.Particles;
import net.kyori.adventure.sound.Sound;

import org.jetbrains.annotations.Nullable;

public final class PluginConfig implements JsonConfiguration {

    @JsonPath("animation_time")
    public static int ANIMATION_TIME;

    @JsonNullable
    @JsonPath("open_effects.sound")
    public static @Nullable Sound OPEN_EFFECTS_SOUND;

    @JsonNullable
    @JsonPath("open_effects.particles")
    public static @Nullable Particles OPEN_EFFECTS_PARTICLES;

    @JsonNullable
    @JsonPath("failure_effects.sound")
    public static @Nullable Sound FAILURE_EFFECTS_SOUND;

    @JsonNullable
    @JsonPath("preview_effects.sound")
    public static @Nullable Sound PREVIEW_EFFECTS_SOUND;

}
