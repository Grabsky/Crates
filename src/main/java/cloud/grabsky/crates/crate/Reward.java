package cloud.grabsky.crates.crate;

import com.squareup.moshi.Json;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reward {

    @Getter(AccessLevel.PUBLIC)
    private final int weight;

    @Json(name = "display_item")
    @Getter(AccessLevel.PUBLIC)
    private final @Nullable ItemStack displayItem;

    @Json(name = "items")
    @Getter(AccessLevel.PUBLIC)
    private final @Nullable List<ItemStack> items;

    @Json(name = "commands")
    @Getter(AccessLevel.PUBLIC)
    private final @Nullable List<String> consoleCommands;

    @Json(ignore = true)
    @Getter(AccessLevel.PUBLIC)
    private @UnknownNullability Integer index;

    @Internal // Reward is constructed directly by Moshi and we need to set this manually.
    public @NotNull Reward fillIndex(final int index) {
        // Throwing exception if index is already set.
        if (this.index != null)
            throw new IllegalStateException("REWARD_INDEX_ALREADY_FILLED");
        // Setting the index.
        this.index = index;
        // Returning this instance of Reward object to keep 'builder-like' flow.
        return this;
    }

}
