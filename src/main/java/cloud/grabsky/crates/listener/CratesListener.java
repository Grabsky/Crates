/*
 * Crates (https://github.com/Grabsky/Crates)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.crates.listener;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.inventory.BedrockPanel;
import cloud.grabsky.crates.Crates;
import cloud.grabsky.crates.configuration.PluginConfig;
import cloud.grabsky.crates.configuration.PluginLocale;
import cloud.grabsky.crates.crate.Crate;
import cloud.grabsky.crates.crate.Reward;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Lidded;
import org.bukkit.block.TileState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class CratesListener implements Listener {

    private @NotNull Crates plugin;

    // Map containing crate states, of whether specific crate is currently occupied or not,
    private final Map<Location, Boolean> isCrateOccupied = new HashMap<>();

    // DateTimeFormatter instance used in file logging.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    private void openAnimation(final @NotNull TileState state, final @NotNull Location location, final @NotNull ItemStack reward) {
        if (state instanceof Lidded lidded) {
            final Location adjustedLocation = state.getLocation().clone().add(0.5, 1, 0.5);
            // Playing open animation.
            lidded.open();
            // Spawning reward item entity.
            final Item item = state.getWorld().dropItem(adjustedLocation, reward, (it) -> {
                // Making the item non-persistent.
                it.setPersistent(false);
                // Making the item impossible to pick up.
                it.setPickupDelay(Integer.MAX_VALUE);
                // Zeroing velocity to prevent the item from moving.
                it.setVelocity(new Vector(0, -0.5, 0));
                // Setting custom name.
                if (reward.hasData(DataComponentTypes.CUSTOM_NAME) == true) {
                    it.customName(reward.getData(DataComponentTypes.CUSTOM_NAME));
                    it.setCustomNameVisible(true);
                }
            });
            // Playing configured sound.
            if (PluginConfig.OPEN_EFFECTS_SOUND != null)
                location.getWorld().playSound(PluginConfig.OPEN_EFFECTS_SOUND, location.x(), location.y(), location.z());
            // Spawning configured particles.
            if (PluginConfig.OPEN_EFFECTS_PARTICLES != null)
                location.getWorld().spawnParticle(
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getParticle(),
                        adjustedLocation,
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getAmount(),
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getOffsetX(),
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getOffsetZ(),
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getOffsetZ(),
                        PluginConfig.OPEN_EFFECTS_PARTICLES.getSpeed()
                );
            // Marking crate as currently occupied.
            isCrateOccupied.put(location, true);
            // Playing close animation and removing item after X ticks (80 by default)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Playing close animation.
                lidded.close();
                // Removing item from the world.
                item.remove();
                // Spawning item "break" particles.
                location.getWorld().spawnParticle(Particle.ITEM, adjustedLocation, 5, 0.15, 0.15, 0.15, 0.01, reward);
                // Marking crate as no longer occupied.
                isCrateOccupied.put(location, false);
            }, PluginConfig.ANIMATION_TIME);
        }
    }

    @EventHandler
    public void onCratePreview(final @NotNull PlayerInteractEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        // Getting BlockState instance of the clicked block.
        if (event.getClickedBlock().getState() instanceof TileState state) {
            // Ignoring non-crate blocks.
            if (state.getPersistentDataContainer().has(Crates.CRATE_NAME, PersistentDataType.STRING) == false)
                return;
            // Getting the player involved in this event.
            final Player player = event.getPlayer();
            // Skipping if player wants to place a block.
            if (player.isSneaking() == true)
                return;
            // Cancelling the event because at this point we know player has clicked on a crate.
            event.setCancelled(true);
            // Reading crate id from the block. Can be null.
            final @Nullable String crateId = state.getPersistentDataContainer().get(Crates.CRATE_NAME, PersistentDataType.STRING);
            // Getting the Crate instance from the id. Can be null.
            final @Nullable Crate crate = (crateId != null) ? plugin.getCratesManager().getCrate(crateId) : null;
            // ...
            if (crate != null && crate.getPreviewInventoryRows() != null) {
                // Clamping the value to ensure it's in range.
                final int rows = Math.clamp(crate.getPreviewInventoryRows(), 1, 6);
                // Preparing the crate preview inventory.
                final BedrockPanel panel = new BedrockPanel.Builder()
                        .setTitle(MiniMessage.miniMessage().deserialize(crate.getPreviewInventoryTitle()))
                        .setRows(rows)
                        .build();
                // Setting rewards in the inventory.
                crate.getRewards().forEach(reward -> {
                    if (reward.getPreviewInventorySlot() != null) {
                        // Getting the reward preview. Display item is chosen if set.
                        final @Nullable ItemStack item = (reward.getDisplayItem() != null)
                                ? reward.getDisplayItem()
                                : (reward.getItems() != null)
                                        ? reward.getItems().getFirst()
                                        : null;
                        // Setting the item in GUI.
                        panel.setItem(Math.clamp(reward.getPreviewInventorySlot(), 0, (rows * 9) - 1), item, null);
                    }
                });
                // Setting the return button in GUI.
                if (crate.getPreviewInventoryReturnButton() != null && crate.getPreviewInventoryReturnButtonSlot() != null)
                    panel.setItem(Math.clamp(crate.getPreviewInventoryReturnButtonSlot(), 0, (rows * 9) - 1), crate.getPreviewInventoryReturnButton(), (e) -> panel.close());
                // Opening inventory to the player.
                panel.open(player, null);
                // Playing effects.
                if (PluginConfig.PREVIEW_EFFECTS_SOUND != null)
                    player.playSound(PluginConfig.PREVIEW_EFFECTS_SOUND);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrateOpen(final @NotNull PlayerInteractEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        // Getting BlockState instance of the clicked block.
        if (event.getClickedBlock().getState() instanceof TileState state) {
            // Ignoring non-crate blocks.
            if (state.getPersistentDataContainer().has(Crates.CRATE_NAME, PersistentDataType.STRING) == false)
                return;
            // Getting the player involved in this event.
            final Player player = event.getPlayer();
            // Getting the item player is holding in their hand.
            final ItemStack item = event.getItem();
            // Skipping if player wants to place a block.
            if (player.isSneaking() == true && item != null && item.getType().isBlock() == true)
                return;
            // Cancelling the event because at this point we know player has clicked on a crate.
            event.setCancelled(true);
            // Reading crate id from the block. Can be null.
            final @Nullable String crateId = state.getPersistentDataContainer().get(Crates.CRATE_NAME, PersistentDataType.STRING);
            // Getting the Crate instance from the id. Can be null.
            final @Nullable Crate crate = (crateId != null) ? plugin.getCratesManager().getCrate(crateId) : null;
            // Checking if player item is a valid key for this crate.
            if (crate != null && item != null && crate.canUnlock(item) == true) {
                final Location location = state.getLocation();
                // If crate is not occupied
                if (isCrateOccupied.getOrDefault(location, false) == false) {
                    // Sending player a message
                    Message.of(PluginLocale.CRATE_OPENED).placeholder("crate", crate.getDisplayName()).send(player);
                    // Removing 1 key from player's inventory
                    item.setAmount(item.getAmount() - 1);
                    // Drawing a reward
                    final Reward reward = crate.rollRandomReward(player);
                    // Create Path instance of the log file.
                    final Path path = Path.of(plugin.getDataFolder().getPath(), "logs.log");
                    // Constructing log message.
                    final String message = "[" + LocalDateTime.now().format(DATE_FORMATTER) + "] [OPEN] " + player.getName() + " (" + player.getUniqueId() + ") opened crate " + crate.getName() + " at " + location.blockX() + ", " + location.blockY() + ", " + location.blockZ() + " in " + location.getWorld().key().asString() + " and received reward with id " + reward.getIndex();
                    // Logging to the file with console fallback in case something goes wrong.
                    try {
                        Files.write(path, Collections.singletonList(message), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (final IOException e) {
                        plugin.getLogger().warning("Could not log message to '" + path.getFileName() + "' file due to following error(s):");
                        plugin.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        if (e.getCause() != null)
                            plugin.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                        // Logging message to the console.
                        plugin.getLogger().warning(message);
                    }
                    // Playing crate animation. Can be skipped if no display item was specified and reward does not contain any items.
                    if (reward.getDisplayItem() != null)
                        this.openAnimation(state, location, reward.getDisplayItem());
                    else if (reward.getItems() != null && reward.getItems().isEmpty() == false)
                        this.openAnimation(state, location, reward.getItems().getFirst());
                    // Exiting listener logic, we're done!
                    return;
                }
                // Playing sound and sending error message to the player.
                if (PluginConfig.FAILURE_EFFECTS_SOUND != null)
                    player.playSound(PluginConfig.FAILURE_EFFECTS_SOUND);
                Message.of(PluginLocale.CRATE_OCCUPIED).send(player);
                return;
            }
            // Playing sound and sending error message to the player.
            if (PluginConfig.FAILURE_EFFECTS_SOUND != null)
                player.playSound(PluginConfig.FAILURE_EFFECTS_SOUND);
            Message.of(PluginLocale.CRATE_MISSING_KEY).send(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onCratePlace(BlockPlaceEvent event) {
        // Reading crate id from the item. Can be null.
        final @Nullable String crateId = event.getItemInHand().getPersistentDataContainer().get(Crates.CRATE_NAME, PersistentDataType.STRING);
        // Copying the crate id from item to the block. If applicable.
        if (crateId != null && event.getBlockPlaced().getState() instanceof TileState state) {
            state.getPersistentDataContainer().set(Crates.CRATE_NAME, PersistentDataType.STRING, crateId);
            state.update();
            // Sending message to the player.
            Message.of(PluginLocale.CRATE_PLACED).placeholder("crate", crateId).send(event.getPlayer());
        }
    }

    // Prevents crate keys from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getRecipe() == null)
            return;
        // Cancelling craft result when using crate key as an ingredient.
        for (final @Nullable ItemStack item : event.getInventory().getMatrix())
            if (item != null)
                if (item.getPersistentDataContainer().has(Crates.KEY_NAME, PersistentDataType.STRING) == true || item.getPersistentDataContainer().has(Crates.CRATE_NAME, PersistentDataType.STRING) == true)
                    event.getInventory().setResult(null);
    }

}
