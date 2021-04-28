package net.skydistrict.crates.crates;

import org.bukkit.inventory.ItemStack;

public class Reward {
    private final ItemStack item;
    private final int weight;

    public Reward(ItemStack item, int weight) {
        this.item = item;
        this.weight = weight;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getWeight() {
        return weight;
    }
}
