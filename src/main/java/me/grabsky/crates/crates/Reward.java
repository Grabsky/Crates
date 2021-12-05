package me.grabsky.crates.crates;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Reward {
    private final int weight;
    private final String previewItemRarity;
    private final ItemStack previewItem;
    private final int previewItemSlot;
    private final boolean usePreviewItemAsReward;
    private final String[] consoleCommands;

    public Reward(final int weight, @Nullable final String previewItemRarity, final int previewItemSlot, @NotNull final ItemStack previewItem, final boolean usePreviewItemAsReward, @Nullable final String[] consoleCommands) {
        this.weight = weight;
        this.previewItemRarity = previewItemRarity;
        this.previewItemSlot = previewItemSlot;
        this.previewItem = previewItem;
        this.usePreviewItemAsReward = usePreviewItemAsReward;
        this.consoleCommands = consoleCommands;
    }

    public double getWeight() {
        return weight;
    }

    public String getPreviewItemRarity() {
        return previewItemRarity;
    }

    public ItemStack getPreviewItem() {
        return previewItem;
    }

    public int getPreviewItemSlot() {
        return previewItemSlot;
    }

    public boolean hasItem() {
        return usePreviewItemAsReward;
    }

    public ItemStack getItem() {
        return (usePreviewItemAsReward) ? previewItem : null;
    }

    public boolean hasConsoleCommands() {
        return (consoleCommands != null && consoleCommands.length > 0);
    }

    public String[] getConsoleCommands() {
        return consoleCommands;
    }
}
