package net.skydistrict.crates.commands;

import me.grabsky.indigo.acf.BaseCommand;
import me.grabsky.indigo.acf.annotation.*;
import me.grabsky.indigo.acf.bukkit.contexts.OnlinePlayer;
import me.grabsky.indigo.adventure.MiniMessage;
import net.kyori.adventure.text.Component;
import net.skydistrict.crates.Crates;
import net.skydistrict.crates.configuration.Lang;
import net.skydistrict.crates.crates.Crate;
import net.skydistrict.crates.crates.CrateManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("crates|crate|skrzynki")
@CommandPermission("skydistrict.command.crates")
public class CratesCommand extends BaseCommand {
    private final Crates instance;
    private final CrateManager manager;

    public CratesCommand(Crates instance) {
        this.instance = instance;
        this.manager = instance.getCratesManager();
    }

    @Default
    @CatchUnknown
    public void onCratesDefault(CommandSender sender) {
        Lang.send(sender, Lang.COMMAND_HELP);
    }

    @Subcommand("reload")
    @CommandPermission("skydistrict.command.crates.reload")
    public void onCratesReload(CommandSender sender) {
        if (instance.reload()) {
            Lang.send(sender, Lang.RELOAD_SUCCESS);
        } else {
            Lang.send(sender, Lang.RELOAD_FAIL);
        }
    }

    @Subcommand("give")
    @CommandPermission("skydistrict.command.crates.give")
    @CommandCompletion("* @crates 1")
    @Syntax("<nick> <crate> [<amount>]")
    public void onCratesKeyGive(CommandSender sender, OnlinePlayer player, String crateId, @Default("1") int amount) {
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

    @Subcommand("giveall")
    @CommandPermission("skydistrict.command.crates.giveall")
    @CommandCompletion("@crates 1")
    @Syntax("<crate> [<amount>]")
    public void onCratesKeyGiveAll(CommandSender sender, String crateId, @Default("1") int amount) {
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

    @Subcommand("getcrate")
    @CommandPermission("skydistrict.command.crates.getcrate")
    @CommandCompletion("@crates")
    @Syntax("<crate>")
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
