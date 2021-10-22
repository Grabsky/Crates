package me.grabsky.crates.listeners;

import me.grabsky.crates.Crates;
import me.grabsky.crates.CratesKeys;
import me.grabsky.crates.configuration.CratesConfig;
import me.grabsky.crates.configuration.CratesLang;
import me.grabsky.crates.crates.Crate;
import me.grabsky.crates.crates.CratesManager;
import me.grabsky.crates.crates.Reward;
import me.grabsky.indigo.configuration.Global;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TileEntityChest;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CratesListener implements Listener {
    private final Crates instance;
    private final CratesManager manager;
    private final Map<Location, Boolean> crates;
    private final Set<Item> displayItems;

    public CratesListener(Crates instance) {
        this.instance = instance;
        this.manager = instance.getCratesManager();
        this.crates = new HashMap<>();
        this.displayItems = new HashSet<>();
    }

    private void openAnimation(Chest chest, Location location, ItemStack item) {
        // A bunch of variables I can't really get rid of
        final org.bukkit.World world = location.getWorld();
        final Location displayLocation = chest.getLocation().clone().add(0.5, 1, 0.5);
        final net.minecraft.world.level.World nmsWorld = ((CraftWorld) chest.getWorld()).getHandle();
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
        world.playSound(location, CratesConfig.OPEN_SOUND_TYPE, CratesConfig.OPEN_SOUND_VOLUME, CratesConfig.OPEN_SOUND_PITCH);
        world.spawnParticle(CratesConfig.PARTICLES_TYPE, displayLocation, CratesConfig.PARTICLES_AMOUNT, CratesConfig.PARTICLES_OFFSET_X, CratesConfig.PARTICLES_OFFSET_Y, CratesConfig.PARTICLES_OFFSET_Z, CratesConfig.PARTICLES_SPEED);
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
        }, CratesConfig.ANIMATION_TIME);
    }

    public void removeDisplayItem() {
        if (displayItems != null && !displayItems.isEmpty()) {
            for (Item item : displayItems) {
                item.remove();
            }
        }
    }

    @EventHandler
    public void onCrateOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        final Chest chest = (Chest) event.getClickedBlock().getState();
        // Returning if clicked chest doesn't have PersistentData of 'crateId'
        if (!chest.getPersistentDataContainer().has(CratesKeys.CRATE_ID, PersistentDataType.STRING)) return;
        // Cancelling the event as player clicked on a crate
        event.setCancelled(true);
        final Player player = event.getPlayer();
        // If player is holding an item
        if (event.getItem() != null) {
            final ItemStack item = event.getItem();
            final ItemMeta meta = item.getItemMeta();
            // If player's held item has PersistentData of 'crateId'
            if (meta.getPersistentDataContainer().has(CratesKeys.CRATE_ID, PersistentDataType.STRING)) {
                final String id = chest.getPersistentDataContainer().get(CratesKeys.CRATE_ID, PersistentDataType.STRING);
                // If player's held item PersistentData of 'crateId' matches the crate one
                if (meta.getPersistentDataContainer().get(CratesKeys.CRATE_ID, PersistentDataType.STRING).equals(id)) {
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
                                // Sending player a message
                                CratesLang.send(player, CratesLang.CRATE_OPENED.replace("{crate}", crate.getName()));
                                // Removing 1 key from player's inventory
                                item.setAmount(item.getAmount() - 1);
                                // Drawing a reward
                                final Reward reward = crate.getRandomReward();
                                // Giving player an ItemStack reward
                                if (reward.hasItem()) {
                                    player.getInventory().addItem(reward.getItem());
                                }
                                // Executing commands
                                if (reward.hasConsoleCommands()) {
                                    for (String c : reward.getConsoleCommands()) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", player.getName()));
                                    }
                                }
                                this.openAnimation(chest, location, reward.getItem());
                                return;
                            }
                            player.playSound(player.getLocation(), CratesConfig.MISSING_KEY_SOUND_TYPE, CratesConfig.MISSING_KEY_SOUND_VOLUME, CratesConfig.MISSING_KEY_SOUND_PITCH);
                            CratesLang.send(player, CratesLang.CRATE_OCCUPIED);
                            return;
                        }
                        player.playSound(player.getLocation(), CratesConfig.MISSING_KEY_SOUND_TYPE, CratesConfig.MISSING_KEY_SOUND_VOLUME, CratesConfig.MISSING_KEY_SOUND_PITCH);
                        CratesLang.send(player, CratesLang.CRATE_NOT_FOUND);
                        return;
                    }
                    player.playSound(player.getLocation(), CratesConfig.MISSING_KEY_SOUND_TYPE, CratesConfig.MISSING_KEY_SOUND_VOLUME, CratesConfig.MISSING_KEY_SOUND_PITCH);
                    CratesLang.send(player, Global.NO_INVENTORY_SPACE);
                    return;
                }
            }
        }
        player.playSound(player.getLocation(), CratesConfig.MISSING_KEY_SOUND_TYPE, CratesConfig.MISSING_KEY_SOUND_VOLUME, CratesConfig.MISSING_KEY_SOUND_PITCH);
        CratesLang.send(player, CratesLang.CRATE_MISSING_KEY);
    }

    @EventHandler
    public static void onCratePlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getItemInHand().getType() != Material.CHEST) return;
        final ItemMeta meta = event.getItemInHand().getItemMeta();
        // Returning if clicked chest doesn't have PersistentData of 'crateId'
        if (!meta.getPersistentDataContainer().has(CratesKeys.CRATE_ID, PersistentDataType.STRING)) return;
        // Getting crate from PersistentData of 'crateId' of player's held item
        final String crateId = meta.getPersistentDataContainer().get(CratesKeys.CRATE_ID, PersistentDataType.STRING);
        if (crateId != null) {
            // Applying PersistentData to newly placed chest
            final Chest chest = (Chest) event.getBlockPlaced().getState();
            chest.getPersistentDataContainer().set(CratesKeys.CRATE_ID, PersistentDataType.STRING, crateId);
            chest.update();
            CratesLang.send(event.getPlayer(), CratesLang.CRATE_PLACED);
        }
    }

    // Prevents crate keys from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (ItemStack item : event.getInventory().getMatrix()) {
            // Yes Bukkit, apparently it can be null...
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(CratesKeys.CRATE_ID, PersistentDataType.STRING)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onCratePreview(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) return;
        if (!event.hasBlock() || event.getAction() != Action.LEFT_CLICK_BLOCK || event.getPlayer().isSneaking()) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST) return;
        // Checking if clicked chest is crate
        final Chest chest = (Chest) event.getClickedBlock().getState();
        final PersistentDataContainer container = chest.getPersistentDataContainer();
        if (container.has(CratesKeys.CRATE_ID, PersistentDataType.STRING)) {
            // Cancelling the event
            event.setCancelled(true);
            // Displaying crate preview to player
            final Crate crate = manager.getCrate(container.get(CratesKeys.CRATE_ID, PersistentDataType.STRING));
            if (crate != null) {
                crate.getPreviewInventory().open(event.getPlayer());
            }
        }
    }
}
