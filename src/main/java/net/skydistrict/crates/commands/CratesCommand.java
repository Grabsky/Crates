package net.skydistrict.crates.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.configuration.Lang;
import net.skydistrict.crates.crates.Crate;
import net.skydistrict.crates.crates.CrateManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

// Commands could be written in a 'smarter' way, although current code is less confusing.
// I'm not going to touch them for now.
public class CratesCommand implements CommandExecutor, TabCompleter {
    private final Crates instance;
    private final CrateManager manager;

    // Completions
    private static final String[] BASE = {"getcrate", "give", "giveall", "reload"};
    private static final String[] NUMBERS = {"1", "16", "32", "64"};

    public CratesCommand(Crates instance) {
        this.instance = instance;
        this.manager = instance.getCratesManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            Lang.send(sender, Lang.COMMAND_HELP);
            return true;
        } else {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("skydistrict.crates.reload")) {
                    if (instance.reload()) {
                        Lang.send(sender, Lang.RELOAD_SUCCESS);
                        return true;
                    }
                    Lang.send(sender, Lang.RELOAD_FAIL);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;
            } else if (args[0].equalsIgnoreCase("give")) {
                if (sender.hasPermission("skydistrict.crates.give")) {
                    if (args.length > 2) {
                        final Player target = Bukkit.getPlayer(args[1]);
                        if (target != null && target.isOnline()) {
                            final Crate crate = manager.getCrate(args[2]);
                            if (crate != null) {
                                final ItemStack key = crate.getCrateKey().clone();
                                if (args.length > 3) {
                                    key.setAmount(this.parseIntOrDefault(args[3], 1));
                                }
                                target.getInventory().addItem(key);
                                Lang.send(target, Lang.CRATE_KEY_RECEIVED, crate.getName());
                                return true;
                            }
                            Lang.send(sender, Lang.CRATE_NOT_FOUND);
                            return true;
                        }
                        Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                        return true;
                    }
                    Lang.send(sender, Lang.COMMAND_HELP);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;

            } else if (args[0].equalsIgnoreCase("giveall")) {
                if (sender.hasPermission("skydistrict.crates.give.all")) {
                    if (args.length > 1) {
                        final Crate crate = manager.getCrate(args[1]);
                        if (crate != null) {
                            final Component component = MiniMessage.get().parse(MessageFormat.format(Lang.CRATE_KEY_RECEIVED.getString(), crate.getName()));
                            for (Player target : Bukkit.getOnlinePlayers()) {
                                final ItemStack key = crate.getCrateKey().clone();
                                if (args.length > 2) {
                                    key.setAmount(this.parseIntOrDefault(args[2], 1));
                                }
                                target.getInventory().addItem(key);
                                target.sendMessage(component);
                                return true;
                            }
                        }
                        Lang.send(sender, Lang.CRATE_NOT_FOUND);
                        return true;
                    }
                    Lang.send(sender, Lang.COMMAND_HELP);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;

            } else if (args[0].equalsIgnoreCase("getcrate")) {
                if (sender instanceof Player) {
                    if (sender.hasPermission("skydistrict.crates.getcrate")) {
                        if (args.length > 1) {
                            final Crate crate = manager.getCrate(args[1]);
                            if (crate != null) {
                                final Player player = (Player) sender;
                                player.getInventory().addItem(crate.getCrateItem());
                                Lang.send(sender, Lang.CRATE_BLOCK_RECEIVED, crate.getName());
                                return true;
                            }
                            Lang.send(sender, Lang.CRATE_NOT_FOUND);
                            return true;
                        }
                        Lang.send(sender, Lang.COMMAND_HELP);
                        return true;
                    }
                    Lang.send(sender, Lang.MISSING_PERMISSIONS);
                    return true;
                }
                Lang.send(sender, Lang.PLAYER_ONLY);
                return true;
            } else {
                Lang.send(sender, Lang.COMMAND_HELP);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        ArrayList<String> tabCompletions = new ArrayList<>();
        if (args.length == 1) {
            this.addMatches(args[args.length - 1], BASE, tabCompletions);
        } else if (args[0].equalsIgnoreCase("getcrate")) {
            if (args.length == 2) this.addMatches(args[args.length - 1], manager.getCrateIds(), tabCompletions);
        } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) return null;
            if (args.length == 3) this.addMatches(args[args.length - 1], manager.getCrateIds(), tabCompletions);
            if (args.length == 4) this.addMatches(args[args.length - 1], NUMBERS, tabCompletions);
        } else if (args[0].equalsIgnoreCase("giveall")) {
            if (args.length == 2) this.addMatches(args[args.length - 1], manager.getCrateIds(), tabCompletions);
            if (args.length == 3) this.addMatches(args[args.length - 1], NUMBERS, tabCompletions);
        }
        return tabCompletions;
    }

    private int parseIntOrDefault(String string, int def) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void addMatches(String arg, String[] completions, ArrayList<String> list) {
        for (String s : completions) {
            if (s.startsWith(arg)) {
                list.add(s);
            }
        }
    }

    private void addMatches(String arg, Iterable<String> completions, ArrayList<String> list) {
        for (String s : completions) {
            if (s.startsWith(arg)) {
                list.add(s);
            }
        }
    }
}
