package me.grabsky.crates;

import org.bukkit.NamespacedKey;

public class CratesKeys {
    public static NamespacedKey CRATE_ID;

    public CratesKeys(Crates instance) {
        CRATE_ID = new NamespacedKey(instance, "crateId");
    }
}
