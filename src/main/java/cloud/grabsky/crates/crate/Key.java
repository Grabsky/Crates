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
            throw new IllegalStateException("CRATE_ALREADY_INITIALIZED");
        // ...
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Crates.KEY_NAME, PersistentDataType.STRING, name);
        });
        // ...
        isInitialized = true;
    }
}
