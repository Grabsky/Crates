package me.grabsky.crates.crates;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.utils.Components;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crate {
    private final String name;
    private final ItemStack crateKey;
    private final ItemStack crateItem;
    private final List<Reward> rewards;
    private final List<Integer> rewardPool;
    private final Inventory previewInventory;

    public static final ItemStack NAVIGATION_RETURN = new ItemBuilder(Material.BARRIER)
            .setName("§cPowrót")
            .setCustomModelData(1)
            .build();

    public Crate(String id, String name, String previewName, ItemStack crateKey) {
        this.name = name;
        // Preparing crate key
        this.crateKey = crateKey;
        crateKey.editMeta((meta) -> meta.getPersistentDataContainer().set(CrateManager.CRATE_ID, PersistentDataType.STRING, id));
        // Preparing crate item
        this.crateItem = new ItemStack(Material.CHEST);
        crateItem.editMeta((meta) -> {
            meta.displayName(Components.parseAmpersand(name));
            meta.getPersistentDataContainer().set(CrateManager.CRATE_ID, PersistentDataType.STRING, id);
        });
        this.rewards = new ArrayList<>();
        this.rewardPool = new ArrayList<>();
        // Preparing crate preview inventory
        this.previewInventory = Bukkit.createInventory(null, 54, Components.parseAmpersand(previewName));
        previewInventory.setMaxStackSize(-1);
        previewInventory.setItem(49, NAVIGATION_RETURN);
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
        // Add reward to preview
        previewInventory.setItem(reward.getPreviewSlot(), reward.getPreviewItem());
    }

    // Generates reward path
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

    public Inventory getPreviewInventory() {
        return this.previewInventory;
    }
}
