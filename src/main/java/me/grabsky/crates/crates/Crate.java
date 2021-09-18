package me.grabsky.crates.crates;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crate {
    private final String name;
    private final ItemStack crateKey;
    private final ItemStack crateItem;
    private final List<Reward> rewards;
    private final List<Integer> rewardPool;

    public Crate(String name, ItemStack crateKey, ItemStack crateItem) {
        this.name = name;
        this.crateKey = crateKey;
        this.crateItem = crateItem;
        this.rewards = new ArrayList<>();
        this.rewardPool = new ArrayList<>();
    }

    // Returns (display) name of the crate
    public String getName() {
        return name;
    }

    // Returns crate key (ItemStack)
    public ItemStack getCrateKey() {
        return crateKey;
    }

    public ItemStack getCrateItem() {
        return crateItem;
    }

    // Adds reward to list
    public void addReward(Reward reward) {
        rewards.add(reward);
    }

    // Generates rewards path
    public void generateRewardsPool() {
        for (int i = 0; i < rewards.size(); i++) {
            for (int z = 0; z < rewards.get(i).getWeight(); z++) {
                rewardPool.add(i);
            }
        }
    }

    // Returns random Reward drawn from rewards path
    public Reward getRandomReward() {
        return rewards.get(rewardPool.get(new Random().nextInt(rewardPool.size())));
    }
}
