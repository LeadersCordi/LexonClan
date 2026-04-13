package com.lexon.clan;

import com.lexon.clan.commands.KlanCommand;
import com.lexon.clan.data.ClanManager;
import com.lexon.clan.data.DatabaseManager;
import com.lexon.clan.gui.GUIManager;
import com.lexon.clan.listeners.ChatListener;
import com.lexon.clan.listeners.GUIListener;
import com.lexon.clan.listeners.NPCListener;
import com.lexon.clan.listeners.PlayerListener;
import com.lexon.clan.npc.NPCManager;
import com.lexon.clan.placeholders.ClanPlaceholders;
import com.lexon.clan.util.ChatIsolationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class LexonClan extends JavaPlugin {

    private static LexonClan instance;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private GUIManager guiManager;
    private NPCManager npcManager;
    private ChatIsolationManager chatIsolationManager;

    @Override
    public void onEnable() {
        instance = this;
        printBanner();
        saveDefaultConfig();
        reloadConfig();
        initializeManagers();
        registerCommands();
        registerListeners();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this).register();
            getLogger().info("✓ PlaceholderAPI desteği aktif!");
        }

        getLogger().info("═══════════════════════════════════════════");
        getLogger().info("  LexonClan başarıyla yüklendi!");
        getLogger().info("  Versiyon: " + getDescription().getVersion());
        getLogger().info("  Developed by CordiDev");
        getLogger().info("═══════════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        if (clanManager != null) {
            clanManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (npcManager != null) {
            npcManager.cleanup();
        }

        getLogger().info("LexonClan kapatıldı. Developed by CordiDev");
    }

    
    private void initializeManagers() {
        try {
            String dbType = getConfig().getString("database.type", "yml");
            databaseManager = new DatabaseManager(this, dbType);
            getLogger().info("✓ Veritabanı başlatıldı: " + dbType.toUpperCase());
            clanManager = new ClanManager(this);
            clanManager.loadAll();
            getLogger().info("✓ Clan Manager başlatıldı");
            guiManager = new GUIManager(this);
            getLogger().info("✓ GUI Manager başlatıldı");
            chatIsolationManager = new ChatIsolationManager(this);
            getLogger().info("✓ Chat Isolation Manager başlatıldı");
            if (getConfig().getBoolean("npc.enabled", true)) {
                npcManager = new NPCManager(this);
                getLogger().info("✓ NPC Manager başlatıldı");
            }

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Manager başlatılırken hata oluştu!", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    
    private void registerCommands() {
        KlanCommand klanCommand = new KlanCommand(this);
        getCommand("klan").setExecutor(klanCommand);
        getCommand("klan").setTabCompleter(klanCommand);
        getLogger().info("✓ Komutlar kaydedildi");
    }

    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        if (npcManager != null) {
            Bukkit.getPluginManager().registerEvents(new NPCListener(this), this);
        }

        getLogger().info("✓ Event listener'lar kaydedildi");
    }

    
    private void printBanner() {
        getLogger().info("");
        getLogger().info("╔═══════════════════════════════════════════════════════════════╗");
        getLogger().info("║                                                               ║");
        getLogger().info("║    ██╗     ███████╗██╗  ██╗ ██████╗ ███╗   ██╗               ║");
        getLogger().info("║    ██║     ██╔════╝╚██╗██╔╝██╔═══██╗████╗  ██║               ║");
        getLogger().info("║    ██║     █████╗   ╚███╔╝ ██║   ██║██╔██╗ ██║               ║");
        getLogger().info("║    ██║     ██╔══╝   ██╔██╗ ██║   ██║██║╚██╗██║               ║");
        getLogger().info("║    ███████╗███████╗██╔╝ ██╗╚██████╔╝██║ ╚████║               ║");
        getLogger().info("║    ╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝               ║");
        getLogger().info("║                                                               ║");
        getLogger().info("║                    ██████╗██╗      █████╗ ███╗   ██╗          ║");
        getLogger().info("║                   ██╔════╝██║     ██╔══██╗████╗  ██║          ║");
        getLogger().info("║                   ██║     ██║     ███████║██╔██╗ ██║          ║");
        getLogger().info("║                   ██║     ██║     ██╔══██║██║╚██╗██║          ║");
        getLogger().info("║                   ╚██████╗███████╗██║  ██║██║ ╚████║          ║");
        getLogger().info("║                    ╚═════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝          ║");
        getLogger().info("║                                                               ║");
        getLogger().info("║                    Developed by CordiDev                      ║");
        getLogger().info("╚═══════════════════════════════════════════════════════════════╝");
        getLogger().info("");
    }

    
    public static Component parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return miniMessage.deserialize(message);
    }

    
    public Component getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + key, "<red>Message not found: " + key + "</red>");
        return parseMessage(prefix + message);
    }

    
    public Component getMessage(String key, String... placeholders) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + key, "<red>Message not found: " + key + "</red>");

        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        return parseMessage(prefix + message);
    }

    
    public Component getRawMessage(String key) {
        String message = getConfig().getString("messages." + key, "<red>Message not found: " + key + "</red>");
        return parseMessage(message);
    }

    
    public Component getRawMessage(String key, String... placeholders) {
        String message = getConfig().getString("messages." + key, "<red>Message not found: " + key + "</red>");

        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        return parseMessage(message);
    }
    public static LexonClan getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public ChatIsolationManager getChatIsolationManager() {
        return chatIsolationManager;
    }
}
