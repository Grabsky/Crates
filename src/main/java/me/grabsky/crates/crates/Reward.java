package me.grabsky.crates.crates;

import org.bukkit.inventory.ItemStack;

public class Reward {
    private final int weight;
    private final ItemStack previewItem;
    private final int previewSlot;
    private final boolean givePreviewItemAsReward;
    private final String[] consoleCommands;

    public Reward(int weight, int previewSlot, ItemStack previewItem, boolean givePreviewItemAsReward, String[] consoleCommands) {
        this.weight = weight;
        this.previewSlot = previewSlot;
        this.previewItem = previewItem;
        this.givePreviewItemAsReward = givePreviewItemAsReward;
        this.consoleCommands = consoleCommands;
    }

    public double getWeight() {
        return weight;
    }

    public ItemStack getPreviewItem() {
        return previewItem;
    }

    public int getPreviewSlot() {
        return previewSlot;
    }

    public ItemStack getItem() {
        return (givePreviewItemAsReward) ? previewItem : null;
    }

    public String[] getConsoleCommands() {
        return consoleCommands;
    }

    public boolean hasItem() {
        return givePreviewItemAsReward;
    }

    public boolean hasConsoleCommands() {
        return (consoleCommands != null && consoleCommands.length > 0);
    }
}
