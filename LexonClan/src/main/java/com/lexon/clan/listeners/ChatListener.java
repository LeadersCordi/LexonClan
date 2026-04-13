package com.lexon.clan.listeners;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.util.ChatIsolationManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

    private final LexonClan plugin;

    public ChatListener(LexonClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatIsolationManager isolationManager = plugin.getChatIsolationManager();
        if (!isolationManager.isIsolated(player.getUniqueId())) {
            for (Player viewer : event.viewers().stream()
                    .filter(v -> v instanceof Player)
                    .map(v -> (Player) v)
                    .toList()) {
                if (isolationManager.isIsolated(viewer.getUniqueId())) {
                    event.viewers().remove(viewer);
                }
            }
            if (plugin.getConfig().getBoolean("chat.show-clan-tag", true)) {
                Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    String format;
                    if (clan.isPremium()) {
                        format = plugin.getConfig().getString("chat.premium-format", "<gold>[{tag}]</gold>");
                    } else {
                        format = plugin.getConfig().getString("chat.normal-format", "<gray>[{tag}]</gray>");
                    }
                    String tagText = format.replace("{tag}", clan.getTag());
                    Component clanTag = LexonClan.parseMessage(tagText + " ");

                    event.renderer((source, sourceDisplayName, message, viewer) -> {
                        return clanTag.append(sourceDisplayName)
                                .append(Component.text(": "))
                                .append(message);
                    });
                }
            }
            return;
        }
        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            isolationManager.handleInput(player, message);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getChatIsolationManager().cancelIsolation(event.getPlayer().getUniqueId());
    }
}
