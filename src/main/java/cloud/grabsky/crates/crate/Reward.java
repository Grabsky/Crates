package cloud.grabsky.crates.crate;

import com.squareup.moshi.Json;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import org.jetbrains.annotations.Nullable;

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

}
