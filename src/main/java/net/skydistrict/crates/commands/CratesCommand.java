package net.skydistrict.crates.commands;

import me.grabsky.indigo.adventure.MiniMessage;
import me.grabsky.indigo.framework.BaseCommand;
import me.grabsky.indigo.framework.ExecutorType;
import me.grabsky.indigo.framework.annotations.DefaultCommand;
import me.grabsky.indigo.framework.annotations.SubCommand;
import me.grabsky.indigo.utils.Numbers;
import net.kyori.adventure.text.Component;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.configuration.Lang;
import net.skydistrict.crates.crates.Crate;
import net.skydistrict.crates.crates.CrateManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

// TO-DO: Context-aware tab completions
public class CratesCommand extends BaseCommand {
    private final Crates instance;
    private final CrateManager manager;
    private final List<String> subCommands;

    public CratesCommand(Crates instance) {
        super("crates", Arrays.asList("crate", "skrzynki"), "skydistrict.command.crates", ExecutorType.ALL);
        this.instance = instance;
        this.manager = instance.getCratesManager();
        this.subCommands = Arrays.asList("getcrate", "give", "giveall", "reload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onCrates(sender);
        } else {
            switch (args[0]) {
                case "reload" -> this.onCratesReload(sender);
                case "getcrate" -> {
                    if (args.length == 2) {
                        this.onCratesGet(sender, args[1]);
                        return;
                    }
                    Lang.send(sender, Lang.USAGE_CRATES_GETCRATE);
                }
                case "give" -> {
                    if (args.length == 4) {
                        final Player player = Bukkit.getPlayer(args[1]);
                        if (player != null && player.isOnline()) {
                            final Integer amount = Numbers.parseInt(args[3]);
                            if (amount != null) {
                                this.onCratesKeyGive(sender, player, args[2], amount);
                                return;
                            }
                            Lang.send(sender, Lang.INVALID_NUMBER);
                            return;
                        }
                        Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                        return;
                    }
                    Lang.send(sender, Lang.USAGE_CRATES_GIVE);
                }
                case "giveall" -> {
                    if (args.length == 3) {
                        final Integer amount = Numbers.parseInt(args[3]);
                        if (amount != null) {
                            this.onCratesKeyGiveAll(sender, args[2], amount);
                            return;
                        }
                        Lang.send(sender, Lang.INVALID_NUMBER);
                        return;
                    }
                    Lang.send(sender, Lang.USAGE_CRATES_GIVEALL);
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        return null;
    }

    @DefaultCommand
    public void onCrates(CommandSender sender) {
        Lang.send(sender, Lang.COMMAND_HELP);
    }

    @SubCommand
    public void onCratesReload(CommandSender sender) {
        if (instance.reload()) {
            Lang.send(sender, Lang.RELOAD_SUCCESS);
        } else {
            Lang.send(sender, Lang.RELOAD_FAIL);
        }
    }

    @SubCommand
    public void onCratesKeyGive(CommandSender sender, Player player, String crateId, int amount) {
        final Player p = player.getPlayer();
        final Crate crate = manager.getCrate(crateId);
        if (crate != null) {
            final ItemStack key = crate.getCrateKey().clone();
            key.setAmount(amount);
            p.getInventory().addItem(key);
            Lang.send(p, Lang.CRATE_KEY_RECEIVED.replace("%crate%", crate.getName()));
        } else {
            Lang.send(sender, Lang.CRATE_NOT_FOUND);
        }
    }

    @SubCommand
    public void onCratesKeyGiveAll(CommandSender sender, String crateId, int amount) {
        final Crate crate = manager.getCrate(crateId);
        if (crate != null) {
            final Component component = MiniMessage.get().parse(Lang.CRATE_KEY_RECEIVED.replace("%crate%", crate.getName()));
            for (Player target : Bukkit.getOnlinePlayers()) {
                final ItemStack key = crate.getCrateKey().clone();
                key.setAmount(amount);
                target.getInventory().addItem(key);
                target.sendMessage(component);
            }
        } else {
            Lang.send(sender, Lang.CRATE_NOT_FOUND);
        }
    }

    @SubCommand
    public void onCratesGet(CommandSender sender, String crateId) {
        final Crate crate = manager.getCrate(crateId);
        if (crate != null) {
            final Player player = (Player) sender;
            player.getInventory().addItem(crate.getCrateItem());
            Lang.send(sender, Lang.CRATE_BLOCK_RECEIVED.replace("%crate%", crate.getName()));
        } else {
            Lang.send(sender, Lang.CRATE_NOT_FOUND);
        }
    }
}
