package me.grabsky.crates.crates.json;

import com.google.gson.annotations.Expose;
import me.grabsky.crates.crates.Reward;
import me.grabsky.indigo.json.JsonItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JsonReward {
    @Expose private int weight;
    @Expose private String previewItemRarity;
    @Expose private int previewItemSlot;
    @Expose private JsonItem previewItem;
    @Expose private boolean usePreviewItemAsReward;
    @Expose private String[] commands;

    @Nullable
    public Reward toReward() {
        final ItemStack internalPreviewItem = this.previewItem.toItemStack();
        if (internalPreviewItem != null) {
            return new Reward(weight, previewItemRarity, previewItemSlot, internalPreviewItem, usePreviewItemAsReward, commands);
        }
        return null;
    }
}
