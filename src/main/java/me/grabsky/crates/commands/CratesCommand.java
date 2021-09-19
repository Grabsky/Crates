package me.grabsky.crates.commands;

import me.grabsky.crates.Crates;
import me.grabsky.crates.configuration.CratesLang;
import me.grabsky.crates.crates.Crate;
import me.grabsky.crates.crates.CrateManager;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Components;
import me.grabsky.indigo.utils.Numbers;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CratesCommand extends BaseCommand {
    private final Crates instance;
    private final CrateManager manager;

    public CratesCommand(Crates instance) {
        super("crates", Arrays.asList("crate", "skrzynki"), "crates.command.crates", ExecutorType.ALL);
        this.instance = instance;
        this.manager = instance.getCratesManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return List.of("get", "give", "giveall", "reload");
        switch (context.get(0)) {
            case "get" -> {
                if (index == 1) return manager.getCrateIds();
            }
            case "give" -> {
                if (index == 1) return null;
                if (index == 2) return manager.getCrateIds();
                if (index == 3) return List.of("1");
            }
            case "giveall" -> {
                if (index == 1) return manager.getCrateIds();
                if (index == 2) return List.of("1");
            }
            default -> {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else switch (args[0]) {
            case "reload" -> this.onCratesReload(sender);
            case "get" -> {
                if (args.length == 2) {
                    this.onCratesGet(sender, args[1]);
                    return;
                }
                CratesLang.send(sender, Global.CORRECT_USAGE + "/crates get <crate>");
            }
            case "give" -> {
                if (args.length == 4) {
                    this.onCratesKeyGive(sender, args[1], args[2], args[3]);
                    return;
                }
                CratesLang.send(sender, Global.CORRECT_USAGE + "/crates give <player> <crate> <amount>");
            }
            case "giveall" -> {
                if (args.length == 3) {
                    this.onCratesKeyGiveAll(sender, args[1], args[2]);
                    return;
                }
                CratesLang.send(sender, Global.CORRECT_USAGE + "/crates giveall <crate> <amount>");
            }
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        CratesLang.send(sender, CratesLang.COMMAND_HELP);
    }

    @SubCommand
    public void onCratesGet(CommandSender sender, String crateId) {
        if (sender.hasPermission("crates.command.get")) {
            final Crate crate = manager.getCrate(crateId);
            if (crate != null) {
                final Player player = (Player) sender;
                player.getInventory().addItem(crate.getCrateItem());
                CratesLang.send(sender, CratesLang.CRATE_BLOCK_RECEIVED.replace("{crate}", crate.getName()));
                return;
            }
            CratesLang.send(sender, CratesLang.CRATE_NOT_FOUND);
            return;
        }
        CratesLang.send(sender, Global.MISSING_PERMISSIONS);
    }

    @SubCommand
    public void onCratesKeyGive(CommandSender sender, String playerName, String crateId, String amountString) {
        if (sender.hasPermission("crates.command.crates.give")) {
            final Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                final Crate crate = manager.getCrate(crateId);
                if (crate != null) {
                    final ItemStack key = crate.getCrateKey().clone();
                    key.setAmount(Numbers.parseInt(amountString, 1));
                    player.getInventory().addItem(key);
                    CratesLang.send(player, CratesLang.CRATE_KEY_RECEIVED.replace("{crate}", crate.getName()));
                    return;
                }
                CratesLang.send(sender, CratesLang.CRATE_NOT_FOUND);
                return;
            }
            CratesLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        CratesLang.send(sender, Global.MISSING_PERMISSIONS);
    }

    @SubCommand
    public void onCratesKeyGiveAll(CommandSender sender, String crateId, String amountString) {
        if (sender.hasPermission("crates.command.giveall")) {
            final Crate crate = manager.getCrate(crateId);
            if (crate != null) {
                final Component component = Components.parseSection(CratesLang.CRATE_KEY_RECEIVED.replace("{crate}", crate.getName()));
                for (Player target : Bukkit.getOnlinePlayers()) {
                    final ItemStack key = crate.getCrateKey().clone();
                    key.setAmount(Numbers.parseInt(amountString, 1));
                    target.getInventory().addItem(key);
                    target.sendMessage(component);
                }
                return;
            }
            CratesLang.send(sender, CratesLang.CRATE_NOT_FOUND);
            return;
        }
        CratesLang.send(sender, Global.MISSING_PERMISSIONS);
    }

    @SubCommand
    public void onCratesReload(CommandSender sender) {
        if (sender.hasPermission("crates.command.crates.reload")) {
            if (instance.reload()) {
                CratesLang.send(sender, Global.RELOAD_SUCCESS);
                return;
            }
            CratesLang.send(sender, Global.RELOAD_FAIL);
            return;
        }
        CratesLang.send(sender, Global.MISSING_PERMISSIONS);
    }
}
