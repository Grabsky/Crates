package net.skydistrict.crates.crates;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Reward {
    private final int weight;
    private final ItemStack item;
    private final List<String> consoleCommands;

    public Reward(int weight, @Nullable ItemStack item, @Nullable List<String> consoleCommands) {
        this.item = item;
        this.weight = weight;
        this.consoleCommands = consoleCommands;
    }

    public double getWeight() {
        return weight;
    }

    public ItemStack getItem() {
        return item;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public boolean hasItem() {
        return item != null;
    }

    public boolean hasConsoleCommands() {
        return (consoleCommands != null && !consoleCommands.isEmpty());
    }
}
