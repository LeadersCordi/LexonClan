package com.lexon.clan.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClanRole {

    private final String id;
    private String displayName;
    private int priority;
    private final Set<String> permissions;

    
    public ClanRole(String id, String displayName, int priority) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.priority = priority;
        this.permissions = new HashSet<>();
    }

    
    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }

    
    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }

    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toLowerCase())
                || permissions.contains("clan.*")
                || permissions.contains("*");
    }

    
    public boolean togglePermission(String permission) {
        String perm = permission.toLowerCase();
        if (permissions.contains(perm)) {
            permissions.remove(perm);
            return false;
        } else {
            permissions.add(perm);
            return true;
        }
    }

    
    public void setPermissions(Set<String> permissions) {
        this.permissions.clear();
        permissions.forEach(p -> this.permissions.add(p.toLowerCase()));
    }

    
    public boolean isHigherThan(ClanRole other) {
        if (other == null)
            return true;
        return this.priority > other.priority;
    }

    
    public boolean isSameOrHigherThan(ClanRole other) {
        if (other == null)
            return true;
        return this.priority >= other.priority;
    }
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClanRole clanRole = (ClanRole) o;
        return Objects.equals(id, clanRole.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClanRole{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", priority=" + priority +
                ", permissions=" + permissions.size() +
                '}';
    }
}
