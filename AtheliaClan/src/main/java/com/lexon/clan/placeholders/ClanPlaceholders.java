package com.lexon.clan.placeholders;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import com.lexon.clan.model.ClanRole;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanPlaceholders extends PlaceholderExpansion {

    private final LexonClan plugin;

    public ClanPlaceholders(LexonClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lexonclan";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CordiDev";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        switch (params.toLowerCase()) {
            case "has_clan" -> {
                return clan != null ? "true" : "false";
            }
            case "total_clans" -> {
                return String.valueOf(plugin.getClanManager().getClanCount());
            }
        }
        if (clan == null) {
            return "";
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        ClanRole role = member != null ? clan.getRole(member.getRoleId()) : null;

        switch (params.toLowerCase()) {
            case "name" -> {
                return clan.getName();
            }
            case "tag" -> {
                return clan.getTag();
            }
            case "role" -> {
                return member != null ? member.getRoleId() : "";
            }
            case "role_display" -> {
                if (role != null) {
                    String display = role.getDisplayName();
                    return display.replaceAll("<[^>]*>", "");
                }
                return "";
            }
            case "members" -> {
                return String.valueOf(clan.getMemberCount());
            }
            case "members_online" -> {
                return String.valueOf(clan.getOnlineMemberCount());
            }
            case "members_max" -> {
                return String.valueOf(clan.getMemberLimit());
            }
            case "leader" -> {
                OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getLeaderUUID());
                return leader.getName() != null ? leader.getName() : "Unknown";
            }
            case "is_leader" -> {
                return clan.getLeaderUUID().equals(player.getUniqueId()) ? "true" : "false";
            }
            case "is_premium" -> {
                return clan.isPremium() ? "true" : "false";
            }
            case "is_private" -> {
                return clan.isPrivate() ? "true" : "false";
            }
            case "formatted" -> {
                return "[" + clan.getTag() + "]";
            }
            case "formatted_name" -> {
                return clan.getName() + " [" + clan.getTag() + "]";
            }
            case "tag_formatted" -> {
                if (clan.isPremium()) {
                    return "<gold>[" + clan.getTag() + "]</gold>";
                } else {
                    return "<gray>[" + clan.getTag() + "]</gray>";
                }
            }
            default -> {
                return null;
            }
        }
    }
}
