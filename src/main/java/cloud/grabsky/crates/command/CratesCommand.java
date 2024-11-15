package cloud.grabsky.crates.command;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import cloud.grabsky.crates.Crates;
import cloud.grabsky.crates.configuration.PluginLocale;
import cloud.grabsky.crates.crate.Crate;
import cloud.grabsky.crates.crate.Key;
import cloud.grabsky.crates.crate.Reward;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "crates", permission = "crates.command.crates", usage = "/crates (...)")
public final class CratesCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Crates plugin;

    // DateTimeFormatter instance used in file logging.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final RootCommandInput input = context.getInput();
        // Returning list of sub-commands when no argument was specified in the input.
        if (index == 0) return CompletionsProvider.of(
                Stream.of("get", "give", "reload", "roll")
                        .filter(literal -> sender.hasPermission(this.getPermission() + "." + literal) == true)
                        .toList()
        );
        // Getting the first literal (argument) of user input.
        final String literal = input.at(1, "").toLowerCase();
        // Returning empty completions provider when missing permission for that literal.
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // Returning sub-command-aware completions provider.
        return switch (literal) {
            case "get" -> (index == 1) ? CompletionsProvider.of(Crate.class) : CompletionsProvider.EMPTY;
            case "give" -> switch(index) {
                case 1 -> CompletionsProvider.of(Player.class, "@all");
                case 2 -> CompletionsProvider.of(Key.class);
                case 3 -> CompletionsProvider.of("1");
                default -> CompletionsProvider.EMPTY;
            };
            case "roll" -> switch(index) {
                    case 1 -> CompletionsProvider.of(Player.class, "@all");
                    case 2 -> CompletionsProvider.of(Crate.class);
                    case 3 -> CompletionsProvider.of("--silent");
                    default -> CompletionsProvider.EMPTY;
            };
            // No completions by default.
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            this.onDefault(context);
            return;
        }
        // Next argument should be a literal.
        final String literal = arguments.nextString().toLowerCase();
        switch (literal) {
            case "get" -> this.onCratesGet(context, arguments);
            case "give" -> this.onCratesGive(context, arguments);
            case "reload" -> this.onCratesReload(context);
            case "roll" -> this.onCratesRoll(context, arguments);
            default -> onDefault(context);
        }
    }


    /* DEFAULT / HELP */

    public void onDefault(final @NotNull RootCommandContext context) {
        Message.of(PluginLocale.COMMANDS_CRATES_HELP).send(context.getExecutor());
    }


    /* CRATES GIVE */

    private static final ExceptionHandler.Factory CRATES_GIVE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMANDS_CRATES_GIVE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    public void onCratesGive(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Checking permissions.
        if (sender.hasPermission(this.getPermission() + ".give") == true) {
            // Checking it next argument is '@all', which indicates all players should receive a key.
            if (arguments.peek().nextString().equalsIgnoreCase("@all") == true) {
                // Consuming next element from the queue.
                arguments.nextString();
                // Getting next argument as Key.
                final Key key = arguments.next(Key.class).asRequired(CRATES_GIVE_USAGE);
                // Getting next argument as Integer. Represents amount of keys.
                final int amount = arguments.next(Integer.class, IntegerArgument.ofRange(1, key.getItem().getMaxStackSize())).asOptional(1);
                // Getting some Key properties.
                final String keyDisplayName = key.getDisplayName();
                final ItemStack item = key.getItem().clone();
                // Updating amount of keys to match whatever was provided in the command.
                item.setAmount(amount);
                // Iterating over all online players and adding key to their inventory.
                Bukkit.getOnlinePlayers().forEach(it -> {
                    // Adding item to player's inventory if not full.
                    if (it.getInventory().firstEmpty() != -1)
                        it.getInventory().addItem(item);
                        // Otherwise, dropping key at the location of the player.
                    else it.getLocation().getWorld().dropItem(it.getLocation(), item);
                    // Sending message to the target.
                    Message.of(PluginLocale.COMMANDS_CRATES_GIVE_SUCCESS_TARGET).placeholder("amount", amount).placeholder("key", keyDisplayName).send(it);
                });
                // Sending message to the sender.
                Message.of(PluginLocale.COMMANDS_CRATES_GIVE_SUCCESS_SENDER_ALL).placeholder("amount", amount).placeholder("key", keyDisplayName).send(sender);
                return;
            }
            // Getting next argument as Player target.
            final Player target = arguments.next(Player.class).asRequired();
            // Getting next argument as Key.
            final Key key = arguments.next(Key.class).asRequired(CRATES_GIVE_USAGE);
            // Getting next argument as Integer. Represents amount of keys.
            final int amount = arguments.next(Integer.class, IntegerArgument.ofRange(1, key.getItem().getMaxStackSize())).asOptional(1);
            // Getting some Key properties.
            final String keyDisplayName = key.getDisplayName();
            final ItemStack item = key.getItem().clone();
            // Updating amount of keys to match whatever was provided in the command.
            item.setAmount(amount);
            // Adding item to player's inventory if not full.
            if (target.getInventory().firstEmpty() != -1)
                target.getInventory().addItem(item);
                // Otherwise, dropping key at the location of the player.
            else target.getLocation().getWorld().dropItem(target.getLocation(), item);
            // Sending message to the sender.
            if (sender != target)
                Message.of(PluginLocale.COMMANDS_CRATES_GIVE_SUCCESS_SENDER_SINGLE).placeholder("amount", amount).placeholder("key", keyDisplayName).placeholder("target", target).send(sender);
            // Sending message to the target.
            Message.of(PluginLocale.COMMANDS_CRATES_GIVE_SUCCESS_TARGET).placeholder("amount", amount).placeholder("key", keyDisplayName).send(target);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.Commands.MISSING_PERMISSIONS).send(sender);
    }


    /* CRATES GET */

    private static final ExceptionHandler.Factory CRATES_GET_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMANDS_CRATES_GET_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    public void onCratesGet(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // Checking permissions.
        if (sender.hasPermission(this.getPermission() + ".get") == true) {
            // Getting next argument as Crate.
            final Crate crate = arguments.next(Crate.class).asRequired(CRATES_GET_USAGE);
            // Getting some Crate properties.
            final String crateDisplayName = crate.getDisplayName();
            final ItemStack item = crate.getCrateItem();
            // Adding crate to sender inventory.
            sender.getInventory().addItem(item);
            // Sending message to the sender.
            Message.of(PluginLocale.COMMANDS_CRATES_GET_SUCCESS).placeholder("crate", crateDisplayName).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.Commands.MISSING_PERMISSIONS).send(sender);
    }


    /* CRATES RELOAD*/

    public void onCratesReload(final @NotNull RootCommandContext context) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Checking permissions.
        if (sender.hasPermission(this.getPermission() + ".reload") == true) {
            Message.of(plugin.onReload() == true ? PluginLocale.RELOAD_SUCCESS : PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.Commands.MISSING_PERMISSIONS).send(sender);
    }

    /* CRATES ROLL */

    private static final ExceptionHandler.Factory CRATES_ROLL_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMANDS_CRATES_ROLL_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    public void onCratesRoll(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Checking permissions.
        if (sender.hasPermission(this.getPermission() + ".roll") == true) {
            // Checking it next argument is '@all', which indicates all players should receive a key.
            if (arguments.peek().nextString().equalsIgnoreCase("@all") == true) {
                // Consuming next element from the queue.
                arguments.nextString();
                // Getting next argument as Key.
                final Crate crate = arguments.next(Crate.class).asRequired(CRATES_ROLL_USAGE);
                // Iterating over all online players and adding key to their inventory.
                Bukkit.getOnlinePlayers().forEach(target -> {
                    // Calling 'CratesCommand#roll' utility method which is responsible for rolling and adding the reward, as well as sending messages and writing logs.
                    roll(target, crate, arguments.next(String.class).asOptional("").equalsIgnoreCase("--silent") == true);
                });
                // Sending message to the sender.
                Message.of(PluginLocale.COMMANDS_CRATES_ROLL_SUCCESS_SENDER_ALL).placeholder("crate", crate.getDisplayName()).send(sender);
                // ...
                return;
            }
            // Getting next argument as Player target.
            final Player target = arguments.next(Player.class).asRequired();
            // Getting next argument as Key.
            final Crate crate = arguments.next(Crate.class).asRequired(CRATES_ROLL_USAGE);
            // Calling 'CratesCommand#roll' utility method which is responsible for rolling and adding the reward, as well as sending messages and writing logs.
            roll(target, crate, arguments.next(String.class).asOptional("").equalsIgnoreCase("--silent") == true);
            // Sending message to the sender. This checks if sender is not a target to prevent duplicated messages.
            if (sender.equals(target) == false)
                Message.of(PluginLocale.COMMANDS_CRATES_ROLL_SUCCESS_SENDER_SINGLE).send(sender);
            // ...
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.Commands.MISSING_PERMISSIONS).send(sender);
    }

    /* UTILITY METHODS */

    private void roll(final @NotNull Player target, final @NotNull Crate crate, final boolean isSilent) {
        final Reward reward = crate.getRandomReward();
        // Adding item to player's inventory. If specified.
        if (reward.getItems() != null)
            reward.getItems().forEach(it -> {
                // Adding item to player's inventory if not full.
                if (target.getInventory().firstEmpty() != -1)
                    target.getInventory().addItem(it);
                    // Otherwise, dropping reward at the location of the player.
                else target.getLocation().getWorld().dropItem(target.getLocation(), it, (entity) -> {
                    // Setting the owner so no other player or entity can pick up the reward.
                    entity.setOwner(target.getUniqueId());
                });
            });
        // Executing console commands. If any was specified.
        if (reward.getConsoleCommands() != null)
            reward.getConsoleCommands().forEach(it -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("<player>", target.getName()));
            });
        // Sending message to the target. If command is not marked as '--silent'.
        if (isSilent == false)
            Message.of(PluginLocale.COMMANDS_CRATES_ROLL_SUCCESS_TARGET).placeholder("crate", crate.getDisplayName()).send(target);

        // Create Path instance of the log file.
        final Path path = Path.of(plugin.getDataFolder().getPath(), "logs.log");
        // Constructing log message.
        final String message = "[" + LocalDateTime.now().format(DATE_FORMATTER) + "] [ROLL] " + target.getName() + " (" + target.getUniqueId() + ") was given a roll from crate " + crate.getName() + " and received reward with id " + reward.getIndex();
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
    }

}
