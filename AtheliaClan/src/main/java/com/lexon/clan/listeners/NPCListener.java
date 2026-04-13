package com.lexon.clan.listeners;

import com.lexon.clan.LexonClan;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCListener implements Listener {

    private final LexonClan plugin;

    public NPCListener(LexonClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (plugin.getNpcManager() == null) {
            return;
        }

        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if (plugin.getNpcManager().isOurNPC(entity)) {
            event.setCancelled(true);
            if (plugin.getClanManager().hasClan(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("clan-already-has"));
                return;
            }
            plugin.getGuiManager().openPublicClansMenu(player, 0);
        }
    }
}
