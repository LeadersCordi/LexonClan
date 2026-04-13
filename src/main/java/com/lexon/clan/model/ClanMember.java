package com.lexon.clan.model;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class ClanMember {

    private final UUID uuid;
    private String roleId;
    private long joinedAt;
    private long lastOnline;

    
    public ClanMember(UUID uuid, String roleId) {
        this.uuid = uuid;
        this.roleId = roleId;
        this.joinedAt = System.currentTimeMillis();
        this.lastOnline = System.currentTimeMillis();
    }

    
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    
    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    
    public String getName() {
        OfflinePlayer offlinePlayer = getOfflinePlayer();
        String name = offlinePlayer.getName();
        return name != null ? name : "Unknown";
    }

    
    public void updateLastOnline() {
        this.lastOnline = System.currentTimeMillis();
    }
    public UUID getUuid() {
        return uuid;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClanMember that = (ClanMember) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "ClanMember{" +
                "uuid=" + uuid +
                ", roleId='" + roleId + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }
}
