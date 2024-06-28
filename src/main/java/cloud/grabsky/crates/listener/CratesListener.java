package cloud.grabsky.crates.listener;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.crates.Crates;
import cloud.grabsky.crates.configuration.PluginConfig;
import cloud.grabsky.crates.configuration.PluginLocale;
import cloud.grabsky.crates.crate.Crate;
import cloud.grabsky.crates.crate.Reward;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
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

    private void openAnimation(final @NotNull Chest chest, final @NotNull Location location, final @NotNull ItemStack reward) {
        // A bunch of variables I can't really get rid of...
        final Location adjustedLocation = chest.getLocation().clone().add(0.5, 1, 0.5);
        final Level nmsWorld = ((CraftWorld) chest.getWorld()).getHandle();
        final BlockPos position = new BlockPos(location.blockX(), location.blockY(), location.blockZ());
        final ChestBlockEntity tileChest = (ChestBlockEntity) nmsWorld.getBlockEntity(position);
        // Playing chest open animation.
        nmsWorld.blockEvent(position, tileChest.getBlockState().getBlock(), 1, 1);
        // Spawning reward item entity.
        final Item item = chest.getWorld().dropItem(adjustedLocation, reward, (it) -> {
            // Making the item non-persistent.
            it.setPersistent(false);
            // Making the item impossible to pick up.
            it.setPickupDelay(Integer.MAX_VALUE);
            // Zeroing velocity to prevent the item from moving.
            it.setVelocity(new Vector(0, -0.5, 0));
        });
        // Playing configured sound.
        if (PluginConfig.OPEN_EFFECTS_SOUND != null)
            location.getWorld().playSound(PluginConfig.OPEN_EFFECTS_SOUND);
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
        // Closing chest and removing item after X ticks (80 by default)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Playing chest close animation.
            nmsWorld.blockEvent(position, tileChest.getBlockState().getBlock(), 1, 0);
            // Removing item from the world.
            item.remove();
            // Spawning item "break" particles.
            location.getWorld().spawnParticle(Particle.ITEM, adjustedLocation, 5, 0.15, 0.15, 0.15, 0.01, reward);
            // Marking crate as no longer occupied.
            isCrateOccupied.put(location, false);
        }, PluginConfig.ANIMATION_TIME);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrateOpen(final @NotNull PlayerInteractEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.CHEST)
            return;
        // Getting BlockState instance of the clicked block.
        final Chest chest = (Chest) event.getClickedBlock().getState();
        // Ignoring non-crate chests.
        if (chest.getPersistentDataContainer().has(Crates.CRATE_NAME, PersistentDataType.STRING) == false)
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
        final @Nullable String crateId = chest.getPersistentDataContainer().get(Crates.CRATE_NAME, PersistentDataType.STRING);
        // Getting the Crate instance from the id. Can be null.
        final @Nullable Crate crate = (crateId != null) ? plugin.getCratesManager().getCrate(crateId) : null;
        // Checking if player item is a valid key for this crate.
        if (crate != null && item != null && crate.canUnlock(item) == true) {
            final Location location = chest.getLocation();
            // If crate is not occupied
            if (isCrateOccupied.getOrDefault(location, false) == false) {
                // Sending player a message
                Message.of(PluginLocale.CRATE_OPENED).placeholder("crate", crate.getDisplayName()).send(player);
                // Removing 1 key from player's inventory
                item.setAmount(item.getAmount() - 1);
                // Drawing a reward
                final Reward reward = crate.getRandomReward();
                // Adding item to player's inventory. If specified.
                if (reward.getItems() != null)
                    reward.getItems().forEach(it -> {
                        // Adding item to player's inventory if not full.
                        if (player.getInventory().firstEmpty() != -1)
                            player.getInventory().addItem(it);
                        // Otherwise, dropping reward at the location of the player.
                        else player.getLocation().getWorld().dropItem(player.getLocation(), it, (entity) -> {
                            // Setting the owner so no other player or entity can pick up the reward.
                            entity.setOwner(player.getUniqueId());
                        });
                    });
                // Executing console commands. If any was specified.
                if (reward.getConsoleCommands() != null)
                    reward.getConsoleCommands().forEach(it -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("<player>", player.getName()));
                    });
                // Create Path instance of the log file.
                final Path path = Path.of(plugin.getDataFolder().getPath(), "logs.log");
                // Constructing log message.
                final String message = "[" + LocalDateTime.now().format(DATE_FORMATTER) + "] " + player.getName() + " (" + player.getUniqueId() + ") opened crate " + crate.getName() + " at " + location.blockX() + ", " + location.blockY() + ", " + location.blockZ() + " in " + location.getWorld().key().asString();
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
                    this.openAnimation(chest, location, reward.getDisplayItem());
                else if (reward.getItems() != null && reward.getItems().isEmpty() == false)
                    this.openAnimation(chest, location, reward.getItems().getFirst());
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

    @EventHandler(ignoreCancelled = true)
    public static void onCratePlace(BlockPlaceEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getItemInHand().getType() != Material.CHEST)
            return;
        // Reading crate id from the item. Can be null.
        final @Nullable String crateId = event.getItemInHand().getPersistentDataContainer().get(Crates.CRATE_NAME, PersistentDataType.STRING);
        // Copying the crate id from item to the block. If applicable.
        if (crateId != null) {
            // Applying PersistentData to newly placed chest
            final Chest chest = (Chest) event.getBlockPlaced().getState();
            chest.getPersistentDataContainer().set(Crates.CRATE_NAME, PersistentDataType.STRING, crateId);
            chest.update();
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
