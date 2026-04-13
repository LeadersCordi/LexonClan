package com.lexon.clan.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Clan {

    private final String id;
    private String name;
    private String tag;
    private UUID leaderUUID;
    private boolean premium;
    private boolean isPrivate;
    private long createdAt;
    private int memberLimit;
    private final Map<UUID, ClanMember> members;
    private final Map<String, ClanRole> roles;
    private ItemStack[] storage;
    private final Map<UUID, Long> pendingInvites;

    
    public Clan(String name, String tag, UUID leaderUUID, boolean premium) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.tag = tag.toUpperCase();
        this.leaderUUID = leaderUUID;
        this.premium = premium;
        this.isPrivate = true;
        this.createdAt = System.currentTimeMillis();
        this.memberLimit = premium ? 50 : 10;

        this.members = new ConcurrentHashMap<>();
        this.roles = new ConcurrentHashMap<>();
        this.storage = new ItemStack[54];
        this.pendingInvites = new ConcurrentHashMap<>();
        ClanMember leader = new ClanMember(leaderUUID, "leader");
        leader.setJoinedAt(System.currentTimeMillis());
        this.members.put(leaderUUID, leader);
        initializeDefaultRoles();
    }

    
    public Clan(String id, String name, String tag, UUID leaderUUID, boolean premium,
            boolean isPrivate, long createdAt, int memberLimit) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leaderUUID = leaderUUID;
        this.premium = premium;
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
        this.memberLimit = memberLimit;

        this.members = new ConcurrentHashMap<>();
        this.roles = new ConcurrentHashMap<>();
        this.storage = new ItemStack[54];
        this.pendingInvites = new ConcurrentHashMap<>();
    }

    
    private void initializeDefaultRoles() {
        ClanRole leaderRole = new ClanRole("leader", "<gold>★ Lider</gold>", 100);
        leaderRole.addPermission("clan.manage");
        leaderRole.addPermission("clan.invite");
        leaderRole.addPermission("clan.kick");
        leaderRole.addPermission("clan.promote");
        leaderRole.addPermission("clan.demote");
        leaderRole.addPermission("clan.storage.view");
        leaderRole.addPermission("clan.storage.deposit");
        leaderRole.addPermission("clan.storage.withdraw");
        leaderRole.addPermission("clan.roles.manage");
        leaderRole.addPermission("clan.settings");
        leaderRole.addPermission("clan.delete");
        leaderRole.addPermission("clan.chat");
        leaderRole.addPermission("clan.announce");
        roles.put("leader", leaderRole);
        ClanRole memberRole = new ClanRole("member", "<gray>Üye</gray>", 10);
        memberRole.addPermission("clan.storage.view");
        memberRole.addPermission("clan.storage.deposit");
        memberRole.addPermission("clan.chat");
        roles.put("member", memberRole);
    }
    
    public boolean addMember(UUID uuid) {
        if (members.size() >= memberLimit) {
            return false;
        }

        ClanMember member = new ClanMember(uuid, "member");
        member.setJoinedAt(System.currentTimeMillis());
        members.put(uuid, member);
        pendingInvites.remove(uuid);
        return true;
    }

    
    public boolean removeMember(UUID uuid) {
        if (uuid.equals(leaderUUID)) {
            return false;
        }
        return members.remove(uuid) != null;
    }

    
    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    
    public ClanMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    
    public Collection<ClanMember> getMembers() {
        return members.values();
    }

    
    public int getMemberCount() {
        return members.size();
    }

    
    public int getOnlineMemberCount() {
        return (int) members.keySet().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .count();
    }

    
    public boolean sendInvite(UUID uuid, int expireSeconds) {
        if (members.containsKey(uuid) || pendingInvites.containsKey(uuid)) {
            return false;
        }
        pendingInvites.put(uuid, System.currentTimeMillis() + (expireSeconds * 1000L));
        return true;
    }

    
    public boolean hasInvite(UUID uuid) {
        Long expireTime = pendingInvites.get(uuid);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            pendingInvites.remove(uuid);
            return false;
        }
        return true;
    }

    
    public void cancelInvite(UUID uuid) {
        pendingInvites.remove(uuid);
    }

    
    public Map<UUID, Long> getPendingInvites() {
        long now = System.currentTimeMillis();
        pendingInvites.entrySet().removeIf(entry -> entry.getValue() < now);
        return new HashMap<>(pendingInvites);
    }
    
    public boolean addRole(ClanRole role) {
        if (roles.containsKey(role.getId())) {
            return false;
        }
        roles.put(role.getId(), role);
        return true;
    }

    
    public boolean removeRole(String roleId) {
        if (roleId.equals("leader") || roleId.equals("member")) {
            return false;
        }
        ClanRole removedRole = roles.remove(roleId);
        if (removedRole != null) {
            members.values().stream()
                    .filter(m -> m.getRoleId().equals(roleId))
                    .forEach(m -> m.setRoleId("member"));
            return true;
        }
        return false;
    }

    
    public ClanRole getRole(String roleId) {
        return roles.get(roleId);
    }

    
    public Collection<ClanRole> getRoles() {
        return roles.values();
    }

    
    public int getCustomRoleCount() {
        return (int) roles.values().stream()
                .filter(r -> !r.getId().equals("leader") && !r.getId().equals("member"))
                .count();
    }

    
    public boolean hasPermission(UUID uuid, String permission) {
        ClanMember member = members.get(uuid);
        if (member == null) {
            return false;
        }

        ClanRole role = roles.get(member.getRoleId());
        if (role == null) {
            return false;
        }

        return role.hasPermission(permission);
    }
    
    public ItemStack[] getStorage() {
        return storage;
    }

    
    public void setStorage(ItemStack[] storage) {
        this.storage = storage;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag.toUpperCase();
    }

    public UUID getLeaderUUID() {
        return leaderUUID;
    }

    public void setLeaderUUID(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    
    public boolean transferLeadership(UUID newLeaderUUID) {
        if (!members.containsKey(newLeaderUUID)) {
            return false;
        }
        ClanMember currentLeader = members.get(leaderUUID);
        if (currentLeader != null) {
            currentLeader.setRoleId("co-leader");
        }
        ClanMember newLeader = members.get(newLeaderUUID);
        if (newLeader != null) {
            newLeader.setRoleId("leader");
            this.leaderUUID = newLeaderUUID;
            return true;
        }

        return false;
    }

    
    public void increaseMemberLimit(int amount) {
        this.memberLimit += amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Clan clan = (Clan) o;
        return Objects.equals(id, clan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Clan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", members=" + members.size() +
                ", premium=" + premium +
                '}';
    }
}
