package com.lexon.clan.data;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import com.lexon.clan.model.ClanRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClanManager {

    private final LexonClan plugin;
    private final Gson gson;
    private final Map<String, Clan> clans;
    private final Map<UUID, String> playerClanMap;
    private final Map<String, String> tagClanMap;
    private final Map<String, String> nameClanMap;
    private File clansFile;
    private FileConfiguration clansConfig;

    public ClanManager(LexonClan plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.clans = new ConcurrentHashMap<>();
        this.playerClanMap = new ConcurrentHashMap<>();
        this.tagClanMap = new ConcurrentHashMap<>();
        this.nameClanMap = new ConcurrentHashMap<>();
    }

    
    public void loadAll() {
        if (plugin.getDatabaseManager().isMySQL()) {
            loadFromMySQL();
        } else {
            loadFromYML();
        }
        plugin.getLogger().info("✓ " + clans.size() + " clan yüklendi");
    }

    
    public void saveAll() {
        if (plugin.getDatabaseManager().isMySQL()) {
            saveToMySQL();
        } else {
            saveToYML();
        }
        plugin.getLogger().info("✓ " + clans.size() + " clan kaydedildi");
    }

    
    public void saveClan(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getDatabaseManager().isMySQL()) {
                saveClanToMySQL(clan);
            } else {
                saveToYML();
            }
        });
    }
    private void loadFromYML() {
        clansFile = new File(plugin.getDataFolder(), "clans.yml");
        if (!clansFile.exists()) {
            try {
                clansFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "clans.yml oluşturulamadı!", e);
                return;
            }
        }

        clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        ConfigurationSection clansSection = clansConfig.getConfigurationSection("clans");

        if (clansSection == null) {
            return;
        }

        for (String clanId : clansSection.getKeys(false)) {
            try {
                ConfigurationSection clanData = clansSection.getConfigurationSection(clanId);
                if (clanData == null)
                    continue;

                Clan clan = new Clan(
                        clanId,
                        clanData.getString("name"),
                        clanData.getString("tag"),
                        UUID.fromString(clanData.getString("leader")),
                        clanData.getBoolean("premium", false),
                        clanData.getBoolean("private", true),
                        clanData.getLong("created-at", System.currentTimeMillis()),
                        clanData.getInt("member-limit", 10));
                ConfigurationSection membersSection = clanData.getConfigurationSection("members");
                if (membersSection != null) {
                    for (String uuidStr : membersSection.getKeys(false)) {
                        ConfigurationSection memberData = membersSection.getConfigurationSection(uuidStr);
                        if (memberData == null)
                            continue;

                        UUID uuid = UUID.fromString(uuidStr);
                        ClanMember member = new ClanMember(uuid, memberData.getString("role", "member"));
                        member.setJoinedAt(memberData.getLong("joined-at", System.currentTimeMillis()));
                        member.setLastOnline(memberData.getLong("last-online", System.currentTimeMillis()));
                        clan.getMembers();
                        try {
                            java.lang.reflect.Field membersField = Clan.class.getDeclaredField("members");
                            membersField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<UUID, ClanMember> members = (Map<UUID, ClanMember>) membersField.get(clan);
                            members.put(uuid, member);
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Üye yüklenirken hata: " + uuid, e);
                        }

                        playerClanMap.put(uuid, clan.getId());
                    }
                }
                ConfigurationSection rolesSection = clanData.getConfigurationSection("roles");
                if (rolesSection != null) {
                    for (String roleId : rolesSection.getKeys(false)) {
                        ConfigurationSection roleData = rolesSection.getConfigurationSection(roleId);
                        if (roleData == null)
                            continue;

                        ClanRole role = new ClanRole(
                                roleId,
                                roleData.getString("display-name", roleId),
                                roleData.getInt("priority", 0));
                        role.setPermissions(new HashSet<>(roleData.getStringList("permissions")));

                        try {
                            java.lang.reflect.Field rolesField = Clan.class.getDeclaredField("roles");
                            rolesField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<String, ClanRole> roles = (Map<String, ClanRole>) rolesField.get(clan);
                            roles.put(roleId, role);
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Rol yüklenirken hata: " + roleId, e);
                        }
                    }
                }
                String storageBase64 = clanData.getString("storage");
                if (storageBase64 != null && !storageBase64.isEmpty()) {
                    try {
                        ItemStack[] storage = itemStackArrayFromBase64(storageBase64);
                        clan.setStorage(storage);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Depo yüklenirken hata: " + clanId, e);
                    }
                }

                registerClan(clan);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Clan yüklenirken hata: " + clanId, e);
            }
        }
    }

    private void saveToYML() {
        if (clansFile == null) {
            clansFile = new File(plugin.getDataFolder(), "clans.yml");
        }

        clansConfig = new YamlConfiguration();

        for (Clan clan : clans.values()) {
            String path = "clans." + clan.getId();

            clansConfig.set(path + ".name", clan.getName());
            clansConfig.set(path + ".tag", clan.getTag());
            clansConfig.set(path + ".leader", clan.getLeaderUUID().toString());
            clansConfig.set(path + ".premium", clan.isPremium());
            clansConfig.set(path + ".private", clan.isPrivate());
            clansConfig.set(path + ".created-at", clan.getCreatedAt());
            clansConfig.set(path + ".member-limit", clan.getMemberLimit());
            for (ClanMember member : clan.getMembers()) {
                String memberPath = path + ".members." + member.getUuid().toString();
                clansConfig.set(memberPath + ".role", member.getRoleId());
                clansConfig.set(memberPath + ".joined-at", member.getJoinedAt());
                clansConfig.set(memberPath + ".last-online", member.getLastOnline());
            }
            for (ClanRole role : clan.getRoles()) {
                String rolePath = path + ".roles." + role.getId();
                clansConfig.set(rolePath + ".display-name", role.getDisplayName());
                clansConfig.set(rolePath + ".priority", role.getPriority());
                clansConfig.set(rolePath + ".permissions", new ArrayList<>(role.getPermissions()));
            }
            try {
                String storageBase64 = itemStackArrayToBase64(clan.getStorage());
                clansConfig.set(path + ".storage", storageBase64);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Depo kaydedilirken hata: " + clan.getId(), e);
            }
        }

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "clans.yml kaydedilemedi!", e);
        }
    }
    private void loadFromMySQL() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            if (conn == null)
                return;
            String clanQuery = "SELECT * FROM lexonclan_clans";
            try (PreparedStatement stmt = conn.prepareStatement(clanQuery);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Clan clan = new Clan(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("tag"),
                            UUID.fromString(rs.getString("leader_uuid")),
                            rs.getBoolean("premium"),
                            rs.getBoolean("is_private"),
                            rs.getLong("created_at"),
                            rs.getInt("member_limit"));
                    String storageBase64 = rs.getString("storage");
                    if (storageBase64 != null && !storageBase64.isEmpty()) {
                        try {
                            clan.setStorage(itemStackArrayFromBase64(storageBase64));
                        } catch (Exception ignored) {
                        }
                    }

                    clans.put(clan.getId(), clan);
                    tagClanMap.put(clan.getTag().toUpperCase(), clan.getId());
                    nameClanMap.put(clan.getName().toLowerCase(), clan.getId());
                }
            }
            String memberQuery = "SELECT * FROM lexonclan_members";
            try (PreparedStatement stmt = conn.prepareStatement(memberQuery);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String clanId = rs.getString("clan_id");
                    Clan clan = clans.get(clanId);
                    if (clan == null)
                        continue;

                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    ClanMember member = new ClanMember(uuid, rs.getString("role_id"));
                    member.setJoinedAt(rs.getLong("joined_at"));
                    member.setLastOnline(rs.getLong("last_online"));

                    try {
                        java.lang.reflect.Field membersField = Clan.class.getDeclaredField("members");
                        membersField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        Map<UUID, ClanMember> members = (Map<UUID, ClanMember>) membersField.get(clan);
                        members.put(uuid, member);
                    } catch (Exception ignored) {
                    }

                    playerClanMap.put(uuid, clanId);
                }
            }
            String roleQuery = "SELECT * FROM lexonclan_roles";
            try (PreparedStatement stmt = conn.prepareStatement(roleQuery);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String clanId = rs.getString("clan_id");
                    Clan clan = clans.get(clanId);
                    if (clan == null)
                        continue;

                    ClanRole role = new ClanRole(
                            rs.getString("id"),
                            rs.getString("display_name"),
                            rs.getInt("priority"));

                    String permsJson = rs.getString("permissions");
                    if (permsJson != null && !permsJson.isEmpty()) {
                        Set<String> perms = gson.fromJson(permsJson, new TypeToken<Set<String>>() {
                        }.getType());
                        role.setPermissions(perms);
                    }

                    try {
                        java.lang.reflect.Field rolesField = Clan.class.getDeclaredField("roles");
                        rolesField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        Map<String, ClanRole> roles = (Map<String, ClanRole>) rolesField.get(clan);
                        roles.put(role.getId(), role);
                    } catch (Exception ignored) {
                    }
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL'den yüklenirken hata!", e);
        }
    }

    private void saveToMySQL() {
        for (Clan clan : clans.values()) {
            saveClanToMySQL(clan);
        }
    }

    private void saveClanToMySQL(Clan clan) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            if (conn == null)
                return;
            String clanQuery = """
                    INSERT INTO lexonclan_clans (id, name, tag, leader_uuid, premium, is_private, created_at, member_limit, storage)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    tag = VALUES(tag),
                    leader_uuid = VALUES(leader_uuid),
                    premium = VALUES(premium),
                    is_private = VALUES(is_private),
                    member_limit = VALUES(member_limit),
                    storage = VALUES(storage)
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(clanQuery)) {
                stmt.setString(1, clan.getId());
                stmt.setString(2, clan.getName());
                stmt.setString(3, clan.getTag());
                stmt.setString(4, clan.getLeaderUUID().toString());
                stmt.setBoolean(5, clan.isPremium());
                stmt.setBoolean(6, clan.isPrivate());
                stmt.setLong(7, clan.getCreatedAt());
                stmt.setInt(8, clan.getMemberLimit());
                stmt.setString(9, itemStackArrayToBase64(clan.getStorage()));
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM lexonclan_members WHERE clan_id = ?")) {
                stmt.setString(1, clan.getId());
                stmt.executeUpdate();
            }

            String memberQuery = "INSERT INTO lexonclan_members (uuid, clan_id, role_id, joined_at, last_online) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(memberQuery)) {
                for (ClanMember member : clan.getMembers()) {
                    stmt.setString(1, member.getUuid().toString());
                    stmt.setString(2, clan.getId());
                    stmt.setString(3, member.getRoleId());
                    stmt.setLong(4, member.getJoinedAt());
                    stmt.setLong(5, member.getLastOnline());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM lexonclan_roles WHERE clan_id = ?")) {
                stmt.setString(1, clan.getId());
                stmt.executeUpdate();
            }

            String roleQuery = "INSERT INTO lexonclan_roles (id, clan_id, display_name, priority, permissions) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(roleQuery)) {
                for (ClanRole role : clan.getRoles()) {
                    stmt.setString(1, role.getId());
                    stmt.setString(2, clan.getId());
                    stmt.setString(3, role.getDisplayName());
                    stmt.setInt(4, role.getPriority());
                    stmt.setString(5, gson.toJson(role.getPermissions()));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Clan MySQL'e kaydedilirken hata: " + clan.getId(), e);
        }
    }

    
    public Clan createClan(String name, String tag, UUID leaderUUID, boolean premium) {
        if (playerClanMap.containsKey(leaderUUID)) {
            return null;
        }
        if (nameClanMap.containsKey(name.toLowerCase())) {
            return null;
        }
        if (tagClanMap.containsKey(tag.toUpperCase())) {
            return null;
        }
        int memberLimit = premium
                ? plugin.getConfig().getInt("settings.member-limits.premium-clan", 50)
                : plugin.getConfig().getInt("settings.member-limits.normal-clan", 10);

        Clan clan = new Clan(name, tag, leaderUUID, premium);
        clan.setMemberLimit(memberLimit);

        registerClan(clan);
        saveClan(clan);

        return clan;
    }

    
    public boolean deleteClan(String clanId) {
        Clan clan = clans.remove(clanId);
        if (clan == null) {
            return false;
        }
        tagClanMap.remove(clan.getTag().toUpperCase());
        nameClanMap.remove(clan.getName().toLowerCase());
        for (ClanMember member : clan.getMembers()) {
            playerClanMap.remove(member.getUuid());
        }
        if (plugin.getDatabaseManager().isMySQL()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                    if (conn == null)
                        return;
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM lexonclan_clans WHERE id = ?")) {
                        stmt.setString(1, clanId);
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Clan silinirken hata: " + clanId, e);
                }
            });
        } else {
            saveToYML();
        }

        return true;
    }

    
    private void registerClan(Clan clan) {
        clans.put(clan.getId(), clan);
        tagClanMap.put(clan.getTag().toUpperCase(), clan.getId());
        nameClanMap.put(clan.getName().toLowerCase(), clan.getId());

        for (ClanMember member : clan.getMembers()) {
            playerClanMap.put(member.getUuid(), clan.getId());
        }
    }

    
    public Clan getClan(String id) {
        return clans.get(id);
    }

    
    public Clan getClanByPlayer(UUID uuid) {
        String clanId = playerClanMap.get(uuid);
        if (clanId == null) {
            return null;
        }
        return clans.get(clanId);
    }

    
    public Clan getClanByTag(String tag) {
        String clanId = tagClanMap.get(tag.toUpperCase());
        if (clanId == null) {
            return null;
        }
        return clans.get(clanId);
    }

    
    public Clan getClanByName(String name) {
        String clanId = nameClanMap.get(name.toLowerCase());
        if (clanId == null) {
            return null;
        }
        return clans.get(clanId);
    }

    
    public boolean hasClan(UUID uuid) {
        return playerClanMap.containsKey(uuid);
    }

    
    public boolean isTagTaken(String tag) {
        return tagClanMap.containsKey(tag.toUpperCase());
    }

    
    public boolean isNameTaken(String name) {
        return nameClanMap.containsKey(name.toLowerCase());
    }

    
    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    
    public List<Clan> getPublicClans() {
        return clans.values().stream()
                .filter(c -> !c.isPrivate())
                .collect(Collectors.toList());
    }

    
    public int getClanCount() {
        return clans.size();
    }

    
    public boolean addMemberToClan(String clanId, UUID uuid) {
        Clan clan = clans.get(clanId);
        if (clan == null) {
            return false;
        }

        if (clan.addMember(uuid)) {
            playerClanMap.put(uuid, clanId);
            saveClan(clan);
            return true;
        }
        return false;
    }

    
    public boolean removeMemberFromClan(String clanId, UUID uuid) {
        Clan clan = clans.get(clanId);
        if (clan == null) {
            return false;
        }

        if (clan.removeMember(uuid)) {
            playerClanMap.remove(uuid);
            saveClan(clan);
            return true;
        }
        return false;
    }

    
    private String itemStackArrayToBase64(ItemStack[] items) {
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(
                    outputStream);

            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    
    private ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(
                    Base64.getDecoder().decode(data));
            org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(
                    inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();

            return items;
        } catch (Exception e) {
            return new ItemStack[54];
        }
    }
}
