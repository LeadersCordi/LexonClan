package com.lexon.clan.listeners;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final LexonClan plugin;

    public PlayerListener(LexonClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Clan clan = plugin.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        if (clan != null) {
            ClanMember member = clan.getMember(event.getPlayer().getUniqueId());
            if (member != null) {
                member.updateLastOnline();
                plugin.getClanManager().saveClan(clan);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Clan clan = plugin.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        if (clan != null) {
            ClanMember member = clan.getMember(event.getPlayer().getUniqueId());
            if (member != null) {
                member.updateLastOnline();
                plugin.getClanManager().saveClan(clan);
            }
        }
    }
}
