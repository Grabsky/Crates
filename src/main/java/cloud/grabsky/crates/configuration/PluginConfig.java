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

}
