package com.lexon.clan.commands;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KlanCommand implements CommandExecutor, TabCompleter {

    private final LexonClan plugin;

    public KlanCommand(LexonClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("lexonclan.use")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("ayarlar")) {
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "kabul", "accept" -> handleAccept(player, args);
            case "reddet", "reject" -> handleReject(player, args);
            case "liste", "list" -> handleList(player);
            case "admin" -> handleAdmin(player, args);
            default -> {
                player.sendMessage(LexonClan.parseMessage(""));
                player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
                player.sendMessage(LexonClan.parseMessage("   <gold>⚔</gold> <white>Klan Komutları</white>"));
                player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
                player.sendMessage(LexonClan.parseMessage(""));
                player.sendMessage(LexonClan.parseMessage(
                        "<gold>/klan ayarlar</gold> <dark_gray>-</dark_gray> <gray>Klan menüsünü aç</gray>"));
                player.sendMessage(LexonClan.parseMessage(
                        "<gold>/klan kabul <klan></gold> <dark_gray>-</dark_gray> <gray>Daveti kabul et</gray>"));
                player.sendMessage(LexonClan.parseMessage(
                        "<gold>/klan reddet <klan></gold> <dark_gray>-</dark_gray> <gray>Daveti reddet</gray>"));
                player.sendMessage(LexonClan.parseMessage(
                        "<gold>/klan liste</gold> <dark_gray>-</dark_gray> <gray>Açık klanları gör</gray>"));
                player.sendMessage(LexonClan.parseMessage(""));
                player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
                return true;
            }
        }

        return true;
    }

    private void handleAccept(Player player, String[] args) {
        if (plugin.getClanManager().hasClan(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("clan-already-has"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(LexonClan.parseMessage("<red>Kullanım:</red> <white>/klan kabul <klan></white>"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByName(args[1]);
        if (clan == null) {
            player.sendMessage(plugin.getMessage("clan-not-found"));
            return;
        }

        if (!clan.hasInvite(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("invite-expired"));
            return;
        }

        if (plugin.getClanManager().addMemberToClan(clan.getId(), player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("member-joined", "player", player.getName()));

            for (ClanMember member : clan.getMembers()) {
                Player memberPlayer = member.getPlayer();
                if (memberPlayer != null && !memberPlayer.equals(player)) {
                    memberPlayer.sendMessage(plugin.getMessage("invite-accepted", "player", player.getName()));
                }
            }
        } else {
            player.sendMessage(plugin.getMessage("clan-full"));
        }
    }

    private void handleReject(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(LexonClan.parseMessage("<red>Kullanım:</red> <white>/klan reddet <klan></white>"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByName(args[1]);
        if (clan == null) {
            player.sendMessage(plugin.getMessage("clan-not-found"));
            return;
        }

        if (!clan.hasInvite(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("invite-expired"));
            return;
        }

        clan.cancelInvite(player.getUniqueId());
        plugin.getClanManager().saveClan(clan);

        player.sendMessage(LexonClan.parseMessage("<gray>Davet reddedildi.</gray>"));

        Player leader = Bukkit.getPlayer(clan.getLeaderUUID());
        if (leader != null) {
            leader.sendMessage(plugin.getMessage("invite-rejected", "player", player.getName()));
        }
    }

    private void handleList(Player player) {
        plugin.getGuiManager().openPublicClansMenu(player, 0);
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("lexonclan.admin")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(LexonClan.parseMessage(""));
            player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
            player.sendMessage(LexonClan.parseMessage("   <gold>⚔</gold> <white>Admin Komutları</white>"));
            player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
            player.sendMessage(LexonClan.parseMessage(""));
            player.sendMessage(LexonClan.parseMessage(
                    "<gold>/klan admin reload</gold> <dark_gray>-</dark_gray> <gray>Config yeniden yükle</gray>"));
            player.sendMessage(LexonClan.parseMessage(
                    "<gold>/klan admin delete <klan></gold> <dark_gray>-</dark_gray> <gray>Klanı sil</gray>"));
            player.sendMessage(LexonClan.parseMessage(
                    "<gold>/klan admin setlimit <klan> <limit></gold> <dark_gray>-</dark_gray> <gray>Limit ayarla</gray>"));
            player.sendMessage(LexonClan.parseMessage(""));
            player.sendMessage(LexonClan.parseMessage("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
            return;
        }

        String adminCommand = args[1].toLowerCase();

        switch (adminCommand) {
            case "reload" -> {
                plugin.reloadConfig();
                player.sendMessage(LexonClan.parseMessage("<green>Config yeniden yüklendi.</green>"));
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage(
                            LexonClan.parseMessage("<red>Kullanım:</red> <white>/klan admin delete <klan></white>"));
                    return;
                }
                Clan clan = plugin.getClanManager().getClanByName(args[2]);
                if (clan == null) {
                    player.sendMessage(plugin.getMessage("clan-not-found"));
                    return;
                }
                plugin.getClanManager().deleteClan(clan.getId());
                player.sendMessage(
                        LexonClan.parseMessage("<green>Klan silindi:</green> <white>" + args[2] + "</white>"));
            }
            case "setlimit" -> {
                if (args.length < 4) {
                    player.sendMessage(LexonClan
                            .parseMessage("<red>Kullanım:</red> <white>/klan admin setlimit <klan> <limit></white>"));
                    return;
                }
                Clan clan = plugin.getClanManager().getClanByName(args[2]);
                if (clan == null) {
                    player.sendMessage(plugin.getMessage("clan-not-found"));
                    return;
                }
                try {
                    int limit = Integer.parseInt(args[3]);
                    clan.setMemberLimit(limit);
                    plugin.getClanManager().saveClan(clan);
                    player.sendMessage(
                            LexonClan.parseMessage("<green>Limit ayarlandı:</green> <white>" + limit + "</white>"));
                } catch (NumberFormatException e) {
                    player.sendMessage(LexonClan.parseMessage("<red>Geçersiz sayı!</red>"));
                }
            }
            default -> player.sendMessage(LexonClan.parseMessage("<red>Bilinmeyen admin komutu.</red>"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("ayarlar", "kabul", "reddet", "liste"));
            if (sender.hasPermission("lexonclan.admin")) {
                completions.add("admin");
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "kabul", "accept", "reddet", "reject" -> {
                    if (sender instanceof Player player) {
                        for (Clan clan : plugin.getClanManager().getAllClans()) {
                            if (clan.hasInvite(player.getUniqueId())) {
                                completions.add(clan.getName());
                            }
                        }
                    }
                }
                case "admin" -> {
                    if (sender.hasPermission("lexonclan.admin")) {
                        completions.addAll(Arrays.asList("reload", "delete", "setlimit"));
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("lexonclan.admin")) {
                switch (args[1].toLowerCase()) {
                    case "delete", "setlimit" -> {
                        for (Clan clan : plugin.getClanManager().getAllClans()) {
                            completions.add(clan.getName());
                        }
                    }
                }
            }
        }

        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
