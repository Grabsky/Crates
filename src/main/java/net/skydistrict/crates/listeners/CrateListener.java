package net.skydistrict.crates.listeners;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.TileEntityChest;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.configuration.Config;
import net.skydistrict.crates.configuration.Lang;
import net.skydistrict.crates.crates.Crate;
import net.skydistrict.crates.crates.CrateManager;
import net.skydistrict.crates.crates.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CrateListener implements Listener {
    private final Crates instance;
    private final CrateManager manager;
    private final Map<Location, Boolean> crates;
    private final Set<Item> displayItems;

    public CrateListener(Crates instance) {
        this.instance = instance;
        this.manager = instance.getCratesManager();
        this.crates = new HashMap<>();
        this.displayItems = new HashSet<>();
    }

    @EventHandler
    public void onCrateOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        final Chest chest = (Chest) event.getClickedBlock().getState();
        // If clicked chest has PersistentData of 'crateId'
        if (chest.getPersistentDataContainer().has(Crates.CRATE_ID, PersistentDataType.STRING)) {
            event.setCancelled(true);
            final Player player = event.getPlayer();
            // If player is holding an item
            if (event.getItem() != null) {
                final ItemStack item = event.getItem();
                final ItemMeta meta = item.getItemMeta();
                // If player's held item has PersistentData of 'crateId'
                if (meta.getPersistentDataContainer().has(Crates.CRATE_ID, PersistentDataType.STRING)) {
                    final String id = chest.getPersistentDataContainer().get(Crates.CRATE_ID, PersistentDataType.STRING);
                    // If player's held item PersistentData of 'crateId' matches the crate one
                    if (meta.getPersistentDataContainer().get(Crates.CRATE_ID, PersistentDataType.STRING).equals(id)) {
                        // If player has 1 empty slot in his inventory
                        if (player.getInventory().firstEmpty() != -1) {
                            final Crate crate = manager.getCrate(id);
                            // If crate is not null (to prevent errors when trying to open old, non-existent anymore crates)
                            if (crate != null) {
                                final Location location = chest.getLocation();
                                // If crate is not occupied
                                if (!crates.containsKey(location) || !crates.get(location)) {
                                    // Mark crate as currently occupied
                                    crates.put(location, true);
                                    // Sending a message
                                    Lang.send(player, Lang.CRATE_OPENED, crate.getName());
                                    // Removing 1 key from player's inventory
                                    player.getInventory().removeItem(crate.getCrateKey());
                                    // Drawing a reward
                                    final Reward reward = crate.getRandomReward();
                                    // Giving player an ItemStack reward
                                    if (reward.hasItem()) {
                                        player.getInventory().addItem(reward.getItem());
                                    }
                                    // Executing commands
                                    if (reward.hasConsoleCommands()) {
                                        for (String c : reward.getConsoleCommands()) {
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replaceAll("%player%", player.getName()));
                                        }
                                    }
                                    this.openAnimation(chest, location, reward.getItem());
                                    return;
                                }
                                player.playSound(player.getLocation(), Config.MISSING_KEY_SOUND_TYPE, Config.MISSING_KEY_SOUND_VOLUME, Config.MISSING_KEY_SOUND_PITCH);
                                Lang.send(player, Lang.CRATE_OCCUPIED);
                                return;
                            }
                            player.playSound(player.getLocation(), Config.MISSING_KEY_SOUND_TYPE, Config.MISSING_KEY_SOUND_VOLUME, Config.MISSING_KEY_SOUND_PITCH);
                            Lang.send(player, Lang.CRATE_NOT_FOUND);
                            return;
                        }
                        player.playSound(player.getLocation(), Config.MISSING_KEY_SOUND_TYPE, Config.MISSING_KEY_SOUND_VOLUME, Config.MISSING_KEY_SOUND_PITCH);
                        Lang.send(player, Lang.NO_SPACE);
                        return;
                    }
                }
            }
            player.playSound(player.getLocation(), Config.MISSING_KEY_SOUND_TYPE, Config.MISSING_KEY_SOUND_VOLUME, Config.MISSING_KEY_SOUND_PITCH);
            Lang.send(player, Lang.CRATE_MISSING_KEY);
        }
    }

    @EventHandler
    public static void onCratePlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getItemInHand().getType() != Material.CHEST) return;
        final ItemMeta meta = event.getItemInHand().getItemMeta();
        if (meta.getPersistentDataContainer().has(Crates.CRATE_ID, PersistentDataType.STRING)) {
            final String id = meta.getPersistentDataContainer().get(Crates.CRATE_ID, PersistentDataType.STRING);
            if (id != null) {
                final Chest chest = (Chest) event.getBlockPlaced().getState();
                chest.getPersistentDataContainer().set(Crates.CRATE_ID, PersistentDataType.STRING, id);
                chest.update();
                Lang.send(event.getPlayer(), Lang.CRATE_PLACED);
            }
        }
    }

    private void openAnimation(Chest chest, Location location, ItemStack item) {
        // A bunch of variables I can't really get rid of
        final org.bukkit.World world = location.getWorld();
        final Location displayLocation = chest.getLocation().clone().add(0.5, 1, 0.5);
        final net.minecraft.server.v1_16_R3.World nmsWorld = ((CraftWorld) chest.getWorld()).getHandle();
        final BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        final TileEntityChest tileChest = (TileEntityChest) nmsWorld.getTileEntity(position);
        // Opening the chest
        nmsWorld.playBlockAction(position, tileChest.getBlock().getBlock(), 1, 1);
        // Spawning a reward item
        final Item displayItem = chest.getWorld().dropItem(displayLocation, item);
        displayItem.setPickupDelay(Integer.MAX_VALUE);
        displayItem.setVelocity(new Vector(0, -0.5, 0));
        // Adding item to a list (so we can remove it on plugin disable)
        displayItems.add(displayItem);
        // Playing sound and displaying particles
        world.playSound(location, Config.OPEN_SOUND_TYPE, Config.OPEN_SOUND_VOLUME, Config.OPEN_SOUND_PITCH);
        world.spawnParticle(Config.PARTICLES_TYPE, displayLocation, Config.PARTICLES_AMOUNT, Config.PARTICLES_OFFSET_X, Config.PARTICLES_OFFSET_Y, Config.PARTICLES_OFFSET_Z, Config.PARTICLES_SPEED);
        // Closing chest and removing item after X ticks (80 by default)
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            nmsWorld.playBlockAction(position, tileChest.getBlock().getBlock(), 1, 0);
            // Removing display item from list and then from the world
            displayItems.remove(displayItem);
            displayItem.remove();
            // Spawning particle
            world.spawnParticle(Particle.ITEM_CRACK, displayLocation, 5, 0.15, 0.15, 0.15, 0.01, item);
            // Making crate available again
            crates.put(location, false);
        }, Config.OPEN_TIME);
    }

    public void removeDisplayItem() {
        for (Item item : displayItems) {
            item.remove();
        }
    }
}
