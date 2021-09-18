package me.grabsky.crates.configuration;

import me.grabsky.crates.Crates;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CratesConfig {
    private final Crates instance;
    private final ConsoleLogger consoleLogger;
    private final File file;
    public static long ANIMATION_TIME;

    public static Sound OPEN_SOUND_TYPE;
    public static float OPEN_SOUND_VOLUME;
    public static float OPEN_SOUND_PITCH;

    public static Sound MISSING_KEY_SOUND_TYPE;
    public static float MISSING_KEY_SOUND_VOLUME;
    public static float MISSING_KEY_SOUND_PITCH;

    public static Particle PARTICLES_TYPE;
    public static int PARTICLES_AMOUNT;
    public static double PARTICLES_SPEED;
    public static double PARTICLES_OFFSET_X;
    public static double PARTICLES_OFFSET_Y;
    public static double PARTICLES_OFFSET_Z;

    public CratesConfig(Crates instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "config.yml");
    }

    public void reload() {
        // Saving default config
        if (!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        final FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != 2) {
            consoleLogger.error(Global.OUTDATED_CONFIG);
        }
        // General
        ANIMATION_TIME = fc.getLong("settings.animation-time");
        // Open sound
        OPEN_SOUND_TYPE = Sound.valueOf(fc.getString("settings.open-sound.type"));
        OPEN_SOUND_VOLUME = (float) fc.getDouble("settings.open-sound.volume");
        OPEN_SOUND_PITCH = (float) fc.getDouble("settings.open-sound.pitch");
        // Missing key sound
        MISSING_KEY_SOUND_TYPE = Sound.valueOf(fc.getString("settings.missing-key-sound.type"));
        MISSING_KEY_SOUND_VOLUME = (float) fc.getDouble("settings.missing-key-sound.volume");
        MISSING_KEY_SOUND_PITCH = (float) fc.getDouble("settings.missing-key-sound.pitch");
        // Particles
        PARTICLES_TYPE = Particle.valueOf(fc.getString("settings.particles.type"));
        PARTICLES_AMOUNT = fc.getInt("settings.particles.amount");
        PARTICLES_SPEED = fc.getDouble("settings.particles.speed");
        PARTICLES_OFFSET_X = fc.getDouble("settings.particles.offset-x");
        PARTICLES_OFFSET_Y = fc.getDouble("settings.particles.offset-y");
        PARTICLES_OFFSET_Z = fc.getDouble("settings.particles.offset-z");
    }
}