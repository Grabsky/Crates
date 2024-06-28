package cloud.grabsky.crates.crate;

import cloud.grabsky.crates.Crates;
import com.squareup.moshi.Json;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Crate {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Json(name = "display_name")
    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String displayName;

    @Json(name = "allowed_keys")
    @Getter(AccessLevel.PUBLIC)
    private final @NotNull List<String> allowedKeys;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull List<Reward> rewards;

    @Json(ignore = true)
    @Getter(AccessLevel.PUBLIC)
    private @NotNull ItemStack crateItem;

    @Json(ignore = true)
    @Getter(AccessLevel.PUBLIC)
    private @NotNull List<Integer> rewardPool;

    @Json(ignore = true)
    @Getter(AccessLevel.PUBLIC)
    private boolean isInitialized;

    public void initialize() {
        if (this.isInitialized == true)
            throw new IllegalStateException("CRATE_ALREADY_INITIALIZED");
        // ...
        this.crateItem = ItemStack.of(Material.CHEST);
        this.rewardPool = new ArrayList<>();
        // Applying PDC data to the crate item.
        crateItem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Crates.CRATE_NAME, PersistentDataType.STRING, name);
        });
        // Indexing rewards.
        for (int rewardIndex = 0; rewardIndex < rewards.size(); rewardIndex++) {
            final Reward reward = rewards.get(rewardIndex).fillIndex(rewardIndex);
            for (int i = 0; i < reward.getWeight(); i++)
                rewardPool.add(rewardIndex);
        }
        // Marking crate as initialized.
        this.isInitialized = true;
    }

    // Returns random Reward drawn from rewards path
    public @NotNull Reward getRandomReward() {
        final int maxBound = rewardPool.size();
        final int random = new SecureRandom().nextInt(maxBound);
        final int rewardIndex = rewardPool.get(random);
        return rewards.get(rewardIndex);
    }

    public boolean canUnlock(final @NotNull ItemStack item) {
        final @Nullable String key = item.getPersistentDataContainer().get(Crates.KEY_NAME, PersistentDataType.STRING);
        // ...
        return key != null && this.allowedKeys.contains(key);
    }

}
