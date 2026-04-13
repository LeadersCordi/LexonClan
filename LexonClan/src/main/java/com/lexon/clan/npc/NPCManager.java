package com.lexon.clan.npc;

import com.lexon.clan.LexonClan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class NPCManager {

    private final LexonClan plugin;
    private final NamespacedKey npcKey;
    private UUID npcUUID;
    private int fancyNpcId;

    public NPCManager(LexonClan plugin) {
        this.plugin = plugin;
        this.npcKey = new NamespacedKey(plugin, "clan_npc");

        fancyNpcId = plugin.getConfig().getInt("npc.fancynpc-id", -1);

        if (fancyNpcId > 0) {
            plugin.getLogger().info("✓ FancyNPC entegrasyonu aktif (ID: " + fancyNpcId + ")");
        } else if (plugin.getConfig().getBoolean("npc.manual-spawn.enabled", false)) {
            spawnManualNPC();
        }
    }

    
    private void spawnManualNPC() {
        String worldName = plugin.getConfig().getString("npc.manual-spawn.world", "world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            plugin.getLogger().warning("NPC spawn world bulunamadı: " + worldName);
            return;
        }

        double x = plugin.getConfig().getDouble("npc.manual-spawn.x", 0);
        double y = plugin.getConfig().getDouble("npc.manual-spawn.y", 64);
        double z = plugin.getConfig().getDouble("npc.manual-spawn.z", 0);

        Location location = new Location(world, x, y, z);
        removeExistingNPC(world);
        Villager npc = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setProfession(Villager.Profession.LIBRARIAN);
        npc.setVillagerLevel(5);
        npc.setCustomNameVisible(true);
        npc.customName(LexonClan.parseMessage(
                plugin.getConfig().getString("npc.display-name",
                        "<gradient:#9B59B6:#3498DB>📋 Clan Listesi</gradient>")));
        npc.getPersistentDataContainer().set(npcKey, PersistentDataType.BYTE, (byte) 1);
        npcUUID = npc.getUniqueId();

        plugin.getLogger().info("✓ Clan listesi NPC'si spawn edildi: " + location);
    }

    
    private void removeExistingNPC(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }

    
    public boolean isOurNPC(Entity entity) {
        if (fancyNpcId > 0) {
            try {
                de.oliver.fancynpcs.api.FancyNpcsPlugin fancyPlugin = de.oliver.fancynpcs.api.FancyNpcsPlugin.get();
                if (fancyPlugin != null) {
                    de.oliver.fancynpcs.api.Npc npc = fancyPlugin.getNpcManager().getNpc(entity.getEntityId());
                    return npc != null;
                }
            } catch (Exception ignored) {
            }
        }
        return entity.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE);
    }

    
    public Location getNPCLocation() {
        if (npcUUID != null) {
            Entity entity = Bukkit.getEntity(npcUUID);
            if (entity != null) {
                return entity.getLocation();
            }
        }
        return null;
    }

    
    public void cleanup() {
        npcUUID = null;
    }

    
    public void respawn() {
        if (plugin.getConfig().getBoolean("npc.manual-spawn.enabled", false)) {
            spawnManualNPC();
        }
    }
}
