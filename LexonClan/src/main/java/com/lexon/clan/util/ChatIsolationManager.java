package com.lexon.clan.util;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanRole;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ChatIsolationManager {

    private final LexonClan plugin;
    private final Map<UUID, IsolationSession> sessions;

    public ChatIsolationManager(LexonClan plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
    }

    
    public boolean isIsolated(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    
    public void startClanCreation(Player player, boolean premium) {
        UUID uuid = player.getUniqueId();
        clearChat(player);
        IsolationSession session = new IsolationSession(IsolationType.CLAN_CREATION);
        session.setData("premium", premium);
        session.setStep(0);
        sessions.put(uuid, session);
        int timeout = plugin.getConfig().getInt("settings.chat-isolation-timeout", 60);
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (sessions.containsKey(uuid)) {
                cancelIsolation(uuid);
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(plugin.getMessage("creation-timeout"));
                }
            }
        }, timeout * 20L);
        session.setTimeoutTask(timeoutTask);
        player.sendMessage(plugin.getRawMessage("chat-isolated"));
        player.sendMessage(plugin.getRawMessage("enter-clan-name"));
        player.sendMessage(plugin.getRawMessage("type-cancel"));
    }

    
    public void startRoleCreation(Player player, Clan clan) {
        UUID uuid = player.getUniqueId();
        clearChat(player);
        IsolationSession session = new IsolationSession(IsolationType.ROLE_CREATION);
        session.setData("clan_id", clan.getId());
        session.setStep(0);
        sessions.put(uuid, session);
        int timeout = plugin.getConfig().getInt("settings.chat-isolation-timeout", 60);
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (sessions.containsKey(uuid)) {
                cancelIsolation(uuid);
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(plugin.getMessage("creation-timeout"));
                }
            }
        }, timeout * 20L);
        session.setTimeoutTask(timeoutTask);
        player.sendMessage(LexonClan.parseMessage("<gradient:#673AB7:#9C27B0>⚙ Rol Oluşturma</gradient>"));
        player.sendMessage(LexonClan.parseMessage("<yellow>✎ Rol ismi girin:</yellow>"));
        player.sendMessage(plugin.getRawMessage("type-cancel"));
    }

    
    public void handleInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        IsolationSession session = sessions.get(uuid);

        if (session == null) {
            return;
        }
        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            cancelIsolation(uuid);
            player.sendMessage(plugin.getMessage("creation-cancelled"));
            return;
        }

        switch (session.getType()) {
            case CLAN_CREATION -> handleClanCreationInput(player, session, message);
            case ROLE_CREATION -> handleRoleCreationInput(player, session, message);
        }
    }

    
    private void handleClanCreationInput(Player player, IsolationSession session, String message) {
        int step = session.getStep();

        if (step == 0) {
            if (!validateClanName(message)) {
                player.sendMessage(plugin.getMessage("invalid-clan-name"));
                player.sendMessage(plugin.getRawMessage("enter-clan-name"));
                return;
            }
            if (containsBlockedWord(message)) {
                player.sendMessage(plugin.getMessage("blocked-word"));
                player.sendMessage(plugin.getRawMessage("enter-clan-name"));
                return;
            }
            if (plugin.getClanManager().isNameTaken(message)) {
                player.sendMessage(LexonClan.parseMessage("<red>Bu isim zaten kullanılıyor!</red>"));
                player.sendMessage(plugin.getRawMessage("enter-clan-name"));
                return;
            }

            session.setData("name", message);
            session.setStep(1);
            player.sendMessage(plugin.getRawMessage("enter-clan-tag"));

        } else if (step == 1) {
            if (!validateClanTag(message)) {
                player.sendMessage(plugin.getMessage("invalid-clan-tag"));
                player.sendMessage(plugin.getRawMessage("enter-clan-tag"));
                return;
            }
            if (plugin.getClanManager().isTagTaken(message)) {
                player.sendMessage(LexonClan.parseMessage("<red>Bu etiket zaten kullanılıyor!</red>"));
                player.sendMessage(plugin.getRawMessage("enter-clan-tag"));
                return;
            }
            String name = (String) session.getData("name");
            boolean premium = (boolean) session.getData("premium");

            Clan clan = plugin.getClanManager().createClan(name, message, player.getUniqueId(), premium);

            cancelIsolation(player.getUniqueId());

            if (clan != null) {
                player.sendMessage(plugin.getMessage("clan-created", "clan", clan.getName()));
            } else {
                player.sendMessage(LexonClan.parseMessage("<red>Clan oluşturulurken bir hata oluştu!</red>"));
            }
        }
    }

    
    private void handleRoleCreationInput(Player player, IsolationSession session, String message) {
        int step = session.getStep();
        String clanId = (String) session.getData("clan_id");
        Clan clan = plugin.getClanManager().getClan(clanId);

        if (clan == null) {
            cancelIsolation(player.getUniqueId());
            player.sendMessage(plugin.getMessage("clan-not-found"));
            return;
        }

        if (step == 0) {
            if (message.length() < 2 || message.length() > 32) {
                player.sendMessage(LexonClan.parseMessage("<red>Rol ismi 2-32 karakter olmalı!</red>"));
                return;
            }
            int maxRoles = plugin.getConfig().getInt("settings.max-roles", 7);
            if (clan.getCustomRoleCount() >= maxRoles) {
                cancelIsolation(player.getUniqueId());
                player.sendMessage(plugin.getMessage("role-max-reached"));
                return;
            }
            String roleId = message.toLowerCase().replace(" ", "_");
            if (clan.getRole(roleId) != null) {
                player.sendMessage(plugin.getMessage("role-name-exists"));
                return;
            }
            ClanRole role = new ClanRole(roleId, "<gray>" + message + "</gray>", 30);
            role.addPermission("clan.storage.view");
            role.addPermission("clan.storage.deposit");

            clan.addRole(role);
            plugin.getClanManager().saveClan(clan);

            cancelIsolation(player.getUniqueId());
            player.sendMessage(plugin.getMessage("role-created", "role", message));
            plugin.getGuiManager().openRolePermissionsMenu(player, clan, role);
        }
    }

    
    public void cancelIsolation(UUID uuid) {
        IsolationSession session = sessions.remove(uuid);
        if (session != null && session.getTimeoutTask() != null) {
            session.getTimeoutTask().cancel();
        }
    }

    
    private void clearChat(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage("");
        }
    }

    
    private boolean validateClanName(String name) {
        int minLength = plugin.getConfig().getInt("settings.clan-name.min-length", 3);
        int maxLength = plugin.getConfig().getInt("settings.clan-name.max-length", 16);
        String pattern = plugin.getConfig().getString("settings.clan-name.allowed-characters",
                "^[a-zA-Z0-9öüşçğıİÖÜŞÇĞ ]+$");

        if (name.length() < minLength || name.length() > maxLength) {
            return false;
        }

        return Pattern.matches(pattern, name);
    }

    
    private boolean validateClanTag(String tag) {
        int minLength = plugin.getConfig().getInt("settings.clan-tag.min-length", 2);
        int maxLength = plugin.getConfig().getInt("settings.clan-tag.max-length", 4);
        String pattern = plugin.getConfig().getString("settings.clan-tag.allowed-characters", "^[a-zA-Z0-9]+$");

        if (tag.length() < minLength || tag.length() > maxLength) {
            return false;
        }

        return Pattern.matches(pattern, tag);
    }

    
    private boolean containsBlockedWord(String text) {
        String lowerText = text.toLowerCase();
        for (String blocked : plugin.getConfig().getStringList("settings.clan-name.blocked-words")) {
            if (lowerText.contains(blocked.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private enum IsolationType {
        CLAN_CREATION,
        ROLE_CREATION
    }

    private static class IsolationSession {
        private final IsolationType type;
        private final Map<String, Object> data;
        private int step;
        private BukkitTask timeoutTask;

        public IsolationSession(IsolationType type) {
            this.type = type;
            this.data = new ConcurrentHashMap<>();
            this.step = 0;
        }

        public IsolationType getType() {
            return type;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public Object getData(String key) {
            return data.get(key);
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public BukkitTask getTimeoutTask() {
            return timeoutTask;
        }

        public void setTimeoutTask(BukkitTask timeoutTask) {
            this.timeoutTask = timeoutTask;
        }
    }
}
