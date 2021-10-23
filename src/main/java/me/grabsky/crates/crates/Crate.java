package me.grabsky.crates.crates;

import me.grabsky.crates.CratesKeys;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.framework.inventories.GlobalInventory;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crate {
    private final String name;
    private final ItemStack crateKey;
    private final ItemStack crateItem;
    private final List<Reward> rewards;
    private final List<Integer> rewardPool;
    private final GlobalInventory previewInventory;

    private static final ItemStack NAVIGATION_RETURN = new ItemBuilder(Material.BARRIER)
            .setName("§cPowrót")
            .setCustomModelData(1)
            .build();

    public Crate(@NotNull final String id, @NotNull final String name, @NotNull final String previewInventoryTitle, @NotNull final ItemStack crateKey) {
        this.name = name;
        // Preparing crate key
        this.crateKey = crateKey;
        crateKey.editMeta((meta) -> meta.getPersistentDataContainer().set(CratesKeys.CRATE_ID, PersistentDataType.STRING, id));
        // Preparing crate item
        this.crateItem = new ItemStack(Material.CHEST);
        crateItem.editMeta((meta) -> {
            meta.displayName(Components.parseAmpersand(name));
            meta.getPersistentDataContainer().set(CratesKeys.CRATE_ID, PersistentDataType.STRING, id);
        });
        this.rewards = new ArrayList<>();
        this.rewardPool = new ArrayList<>();
        // Preparing crate preview inventory
        this.previewInventory = new GlobalInventory(Components.parseSection(previewInventoryTitle), 54, "block.note_block.hat", 1, 1.5f);
        previewInventory.setItem(49, NAVIGATION_RETURN, (event) -> event.getWhoClicked().closeInventory());
    }

    // Returns (display) name of the crate
    public String getName() {
        return name;
    }

    // Returns crate key (ItemStack)
    public ItemStack getCrateKey() {
        return crateKey;
    }

    // Returns placeable crate (chest)
    public ItemStack getCrateItem() {
        return crateItem;
    }

    // Adds reward to list
    public void addReward(@NotNull final Reward reward) {
        rewards.add(reward);
        // Adding reward to reward pool
        final int index = rewards.size() - 1;
        for (int i = 0; i < reward.getWeight(); i++) {
            rewardPool.add(index);
        }
        // Adding reward to preview inventory
        final ItemStack previewItem = reward.getPreviewItem().clone();
        if (reward.getPreviewItemRarity() != null && !reward.getPreviewItemRarity().equals("")) {
            previewItem.editMeta((meta) -> {
                final List<Component> lore = (meta.hasLore()) ? meta.lore() : new ArrayList<>();
                lore.add(Components.parseSection(reward.getPreviewItemRarity()).decoration(TextDecoration.ITALIC, false)); // Safe to ignore
                meta.lore(lore);
            });
        }
        previewInventory.setItem(reward.getPreviewItemSlot(), previewItem);
    }

    // Returns random Reward drawn from rewards path
    public Reward getRandomReward() {
        return rewards.get(rewardPool.get(new Random().nextInt(rewardPool.size())));
    }

    // Returns preview inventory
    public GlobalInventory getPreviewInventory() {
        return previewInventory;
    }
}
