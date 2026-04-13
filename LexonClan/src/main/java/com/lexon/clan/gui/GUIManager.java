package com.lexon.clan.gui;

import com.lexon.clan.LexonClan;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import com.lexon.clan.model.ClanRole;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class GUIManager {

        private final LexonClan plugin;
        public static final String MAIN_MENU = "lexonclan:main";
        public static final String CREATE_MENU = "lexonclan:create";
        public static final String MEMBERS_MENU = "lexonclan:members";
        public static final String MEMBER_ACTIONS = "lexonclan:member_actions";
        public static final String STORAGE_MENU = "lexonclan:storage";
        public static final String SETTINGS_MENU = "lexonclan:settings";
        public static final String ROLES_MENU = "lexonclan:roles";
        public static final String ROLE_EDIT = "lexonclan:role_edit";
        public static final String ROLE_PERMISSIONS = "lexonclan:role_perms";
        public static final String DELETE_CONFIRM = "lexonclan:delete";
        public static final String PUBLIC_CLANS = "lexonclan:public";
        public static final String CLAN_DETAIL = "lexonclan:clan_detail";
        public static final String INVITE_MENU = "lexonclan:invite";

        public GUIManager(LexonClan plugin) {
                this.plugin = plugin;
        }
        public void openMainMenu(Player player) {
                Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

                if (clan == null) {
                        openCreateMenu(player);
                } else {
                        openManagementMenu(player, clan);
                }
        }

        
        public void openCreateMenu(Player player) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(CREATE_MENU), 27,
                                LexonClan.parseMessage("<dark_gray>Klan Oluştur</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 27);
                ItemStack normalClan = createItem(
                                Material.SHIELD,
                                "<white>Normal Klan</white>",
                                Arrays.asList(
                                                "",
                                                "<gray>Standart bir klan oluşturun.</gray>",
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Üye Limiti:</gray> <white>"
                                                                + plugin.getConfig().getInt(
                                                                                "settings.member-limits.normal-clan",
                                                                                10)
                                                                + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Maliyet:</gray> <green>Ücretsiz</green>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(normalClan, "action", "create_normal");
                inv.setItem(11, normalClan);
                ItemStack premiumClan = createItem(
                                Material.NETHER_STAR,
                                "<gold>⭐ Premium Klan</gold>",
                                Arrays.asList(
                                                "",
                                                "<gray>Özel ayrıcalıklara sahip VIP klan.</gray>",
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Üye Limiti:</gray> <white>"
                                                                + plugin.getConfig().getInt(
                                                                                "settings.member-limits.premium-clan",
                                                                                50)
                                                                + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Yetki:</gray> <gold>lexonclan.premium</gold>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(premiumClan, "action", "create_premium");
                inv.setItem(15, premiumClan);

                player.openInventory(inv);
        }

        
        public void openManagementMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(MAIN_MENU), 45,
                                LexonClan.parseMessage("<dark_gray>" + clan.getName() + "</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 45);
                ItemStack clanInfo = createItem(
                                clan.isPremium() ? Material.NETHER_STAR : Material.SHIELD,
                                (clan.isPremium() ? "<gold>⭐ </gold>" : "") + "<white>" + clan.getName()
                                                + "</white> <dark_gray>[" + clan.getTag() + "]</dark_gray>",
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Üyeler:</gray> <white>"
                                                                + clan.getMemberCount() + "<gray>/</gray>"
                                                                + clan.getMemberLimit() + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Online:</gray> <green>"
                                                                + clan.getOnlineMemberCount() + "</green>",
                                                "<dark_gray>►</dark_gray> <gray>Tür:</gray> "
                                                                + (clan.isPremium() ? "<gold>Premium</gold>"
                                                                                : "<gray>Normal</gray>"),
                                                "<dark_gray>►</dark_gray> <gray>Gizlilik:</gray> "
                                                                + (clan.isPrivate() ? "<red>Özel</red>"
                                                                                : "<green>Açık</green>"),
                                                ""));
                inv.setItem(4, clanInfo);
                ItemStack membersBtn = createItem(
                                Material.PLAYER_HEAD,
                                "<white>Üyeler</white>",
                                Arrays.asList(
                                                "",
                                                "<gray>Klan üyelerini görüntüle ve yönet.</gray>",
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Toplam:</gray> <white>"
                                                                + clan.getMemberCount() + "</white>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(membersBtn, "action", "open_members");
                inv.setItem(19, membersBtn);
                ItemStack storageBtn = createItem(
                                Material.CHEST,
                                "<white>Klan Kasası</white>",
                                Arrays.asList(
                                                "",
                                                "<gray>Ortak klan kasasını görüntüle.</gray>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(storageBtn, "action", "open_storage");
                inv.setItem(21, storageBtn);
                if (clan.hasPermission(player.getUniqueId(), "clan.invite")) {
                        ItemStack inviteBtn = createItem(
                                        Material.WRITABLE_BOOK,
                                        "<white>Davet Et</white>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Oyuncuları klana davet et.</gray>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(inviteBtn, "action", "open_invite");
                        inv.setItem(23, inviteBtn);
                }
                if (clan.hasPermission(player.getUniqueId(), "clan.settings") ||
                                clan.getLeaderUUID().equals(player.getUniqueId())) {
                        ItemStack settingsBtn = createItem(
                                        Material.COMPARATOR,
                                        "<white>Ayarlar</white>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Klan ayarlarını yönet.</gray>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(settingsBtn, "action", "open_settings");
                        inv.setItem(25, settingsBtn);
                }
                if (clan.getLeaderUUID().equals(player.getUniqueId())) {
                        ItemStack deleteBtn = createItem(
                                        Material.BARRIER,
                                        "<red>Klanı Sil</red>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Klanı kalıcı olarak sil.</gray>",
                                                        "",
                                                        "<dark_red>⚠ Bu işlem geri alınamaz!</dark_red>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(deleteBtn, "action", "delete_clan");
                        inv.setItem(40, deleteBtn);
                } else {
                        ItemStack leaveBtn = createItem(
                                        Material.OAK_DOOR,
                                        "<red>Ayrıl</red>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Bu klandan ayrılın.</gray>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(leaveBtn, "action", "leave_clan");
                        inv.setItem(40, leaveBtn);
                }

                player.openInventory(inv);
        }
        public void openSettingsMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(SETTINGS_MENU), 27,
                                LexonClan.parseMessage("<dark_gray>Klan Ayarları</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 27);
                int slot = 11;
                ItemStack privacyBtn = createItem(
                                clan.isPrivate() ? Material.IRON_DOOR : Material.OAK_DOOR,
                                "<white>Gizlilik</white>",
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Mevcut:</gray> "
                                                                + (clan.isPrivate() ? "<red>Özel</red>"
                                                                                : "<green>Açık</green>"),
                                                "",
                                                clan.isPrivate()
                                                                ? "<gray>Sadece davetli oyuncular katılabilir.</gray>"
                                                                : "<gray>Herkes NPC üzerinden katılabilir.</gray>",
                                                "",
                                                "<gold>> Tıkla - Değiştir</gold>"));
                setItemData(privacyBtn, "action", "toggle_privacy");
                inv.setItem(slot++, privacyBtn);
                if (clan.hasPermission(player.getUniqueId(), "clan.roles.manage")) {
                        ItemStack rolesBtn = createItem(
                                        Material.NAME_TAG,
                                        "<white>Roller</white>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Klan rollerini yönet.</gray>",
                                                        "",
                                                        "<dark_gray>►</dark_gray> <gray>Roller:</gray> <white>"
                                                                        + clan.getRoles().size() + "</white>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(rolesBtn, "action", "open_roles");
                        inv.setItem(slot++, rolesBtn);
                }
                if (clan.getLeaderUUID().equals(player.getUniqueId())) {
                        ItemStack limitBtn = createItem(
                                        Material.GOLD_INGOT,
                                        "<white>Üye Limiti</white>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Coin kullanarak üye limitinizi arttırın.</gray>",
                                                        "",
                                                        "<dark_gray>►</dark_gray> <gray>Mevcut:</gray> <white>"
                                                                        + clan.getMemberLimit() + "</white>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(limitBtn, "action", "open_limit_increase");
                        inv.setItem(slot++, limitBtn);
                        if (!clan.isPremium()) {
                                ItemStack premiumBtn = createItem(
                                                Material.NETHER_STAR,
                                                "<gold>⭐ Premium'a Yükselt</gold>",
                                                Arrays.asList(
                                                                "",
                                                                "<gray>Klanınızı premium'a yükseltin.</gray>",
                                                                "",
                                                                "<dark_gray>►</dark_gray> <gray>Yetki:</gray> <gold>lexonclan.premium</gold>",
                                                                "",
                                                                "<gold>> Tıkla</gold>"));
                                setItemData(premiumBtn, "action", "upgrade_premium");
                                inv.setItem(slot++, premiumBtn);
                        }
                }
                ItemStack statsInfo = createItem(
                                Material.BOOK,
                                "<white>İstatistikler</white>",
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Üyeler:</gray> <white>"
                                                                + clan.getMemberCount() + "<gray>/</gray>"
                                                                + clan.getMemberLimit() + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Online:</gray> <green>"
                                                                + clan.getOnlineMemberCount() + "</green>",
                                                "<dark_gray>►</dark_gray> <gray>Roller:</gray> <white>"
                                                                + clan.getRoles().size() + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Tür:</gray> "
                                                                + (clan.isPremium() ? "<gold>Premium</gold>"
                                                                                : "<gray>Normal</gray>"),
                                                ""));
                inv.setItem(slot, statsInfo);
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Ana menüye dön</dark_gray>"));
                setItemData(backBtn, "action", "back_main");
                inv.setItem(22, backBtn);

                player.openInventory(inv);
        }
        public void openMembersMenu(Player player, Clan clan, int page) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(MEMBERS_MENU), 54,
                                LexonClan.parseMessage("<dark_gray>Üyeler</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                List<ClanMember> members = new ArrayList<>(clan.getMembers());
                members.sort((a, b) -> {
                        ClanRole roleA = clan.getRole(a.getRoleId());
                        ClanRole roleB = clan.getRole(b.getRoleId());
                        int priorityA = roleA != null ? roleA.getPriority() : 0;
                        int priorityB = roleB != null ? roleB.getPriority() : 0;
                        return Integer.compare(priorityB, priorityA);
                });

                int itemsPerPage = 28;
                int totalPages = (int) Math.ceil((double) members.size() / itemsPerPage);
                int startIndex = page * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, members.size());

                int[] slots = { 10, 11, 12, 13, 14, 15, 16,
                                19, 20, 21, 22, 23, 24, 25,
                                28, 29, 30, 31, 32, 33, 34,
                                37, 38, 39, 40, 41, 42, 43 };

                int slotIndex = 0;
                for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++) {
                        ClanMember member = members.get(i);
                        ClanRole role = clan.getRole(member.getRoleId());

                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();

                        OfflinePlayer offlinePlayer = member.getOfflinePlayer();
                        meta.setOwningPlayer(offlinePlayer);

                        String roleName = role != null ? role.getDisplayName() : "<gray>Üye</gray>";
                        boolean isOnline = member.isOnline();

                        meta.displayName(LexonClan.parseMessage(
                                        "<!italic>" + (isOnline ? "<green>●</green> " : "<red>●</red> ") +
                                                        "<white>" + member.getName() + "</white>"));

                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.empty());
                        lore.add(LexonClan.parseMessage(
                                        "<!italic><dark_gray>►</dark_gray> <gray>Rol:</gray> " + roleName));
                        lore.add(LexonClan.parseMessage("<!italic><dark_gray>►</dark_gray> <gray>Durum:</gray> "
                                        + (isOnline ? "<green>Çevrimiçi</green>" : "<red>Çevrimdışı</red>")));
                        lore.add(Component.empty());

                        if (clan.hasPermission(player.getUniqueId(), "clan.kick") ||
                                        clan.hasPermission(player.getUniqueId(), "clan.promote")) {
                                lore.add(LexonClan.parseMessage("<!italic><gold>> Tıkla - İşlemler</gold>"));
                        }

                        meta.lore(lore);

                        meta.getPersistentDataContainer().set(
                                        new NamespacedKey(plugin, "member_uuid"),
                                        PersistentDataType.STRING,
                                        member.getUuid().toString());

                        head.setItemMeta(meta);
                        inv.setItem(slots[slotIndex], head);
                        slotIndex++;
                }
                if (page > 0) {
                        ItemStack prevPage = createItem(Material.ARROW, "<white>◄ Önceki</white>",
                                        Collections.singletonList("<dark_gray>Sayfa " + page + "/" + totalPages
                                                        + "</dark_gray>"));
                        setItemData(prevPage, "page", String.valueOf(page - 1));
                        setItemData(prevPage, "action", "prev_page");
                        inv.setItem(45, prevPage);
                }

                if (page < totalPages - 1) {
                        ItemStack nextPage = createItem(Material.ARROW, "<white>Sonraki ►</white>",
                                        Collections.singletonList("<dark_gray>Sayfa " + (page + 2) + "/" + totalPages
                                                        + "</dark_gray>"));
                        setItemData(nextPage, "page", String.valueOf(page + 1));
                        setItemData(nextPage, "action", "next_page");
                        inv.setItem(53, nextPage);
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Ana menüye dön</dark_gray>"));
                setItemData(backBtn, "action", "back_main");
                inv.setItem(49, backBtn);

                player.openInventory(inv);
        }

        
        public void openMemberActionsMenu(Player player, Clan clan, ClanMember targetMember) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(MEMBER_ACTIONS), 27,
                                LexonClan.parseMessage("<dark_gray>" + targetMember.getName() + "</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 27);

                ClanRole targetRole = clan.getRole(targetMember.getRoleId());
                ClanMember playerMember = clan.getMember(player.getUniqueId());
                ClanRole playerRole = playerMember != null ? clan.getRole(playerMember.getRoleId()) : null;

                boolean canManage = playerRole != null && targetRole != null &&
                                playerRole.isHigherThan(targetRole) &&
                                !targetMember.getUuid().equals(player.getUniqueId());
                ItemStack memberInfo = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) memberInfo.getItemMeta();
                meta.setOwningPlayer(targetMember.getOfflinePlayer());
                meta.displayName(LexonClan.parseMessage("<!italic><white>" + targetMember.getName() + "</white>"));
                meta.lore(Arrays.asList(
                                Component.empty(),
                                LexonClan.parseMessage("<!italic><dark_gray>►</dark_gray> <gray>Rol:</gray> "
                                                + (targetRole != null ? targetRole.getDisplayName() : "Üye")),
                                LexonClan.parseMessage("<!italic><dark_gray>►</dark_gray> <gray>Durum:</gray> "
                                                + (targetMember.isOnline() ? "<green>Çevrimiçi</green>"
                                                                : "<red>Çevrimdışı</red>"))));
                memberInfo.setItemMeta(meta);
                inv.setItem(4, memberInfo);

                String targetUuid = targetMember.getUuid().toString();

                if (canManage) {
                        if (clan.hasPermission(player.getUniqueId(), "clan.promote")) {
                                ItemStack promoteBtn = createItem(
                                                Material.LIME_DYE,
                                                "<green>Yükselt</green>",
                                                Arrays.asList(
                                                                "",
                                                                "<gray>Bu üyeyi bir sonraki role yükselt.</gray>",
                                                                "",
                                                                "<gold>> Tıkla</gold>"));
                                setItemData(promoteBtn, "action", "promote");
                                setItemData(promoteBtn, "target", targetUuid);
                                inv.setItem(11, promoteBtn);
                        }
                        if (clan.hasPermission(player.getUniqueId(), "clan.demote")) {
                                ItemStack demoteBtn = createItem(
                                                Material.ORANGE_DYE,
                                                "<gold>Düşür</gold>",
                                                Arrays.asList(
                                                                "",
                                                                "<gray>Bu üyeyi bir alt role düşür.</gray>",
                                                                "",
                                                                "<gold>> Tıkla</gold>"));
                                setItemData(demoteBtn, "action", "demote");
                                setItemData(demoteBtn, "target", targetUuid);
                                inv.setItem(13, demoteBtn);
                        }
                        if (clan.hasPermission(player.getUniqueId(), "clan.kick")) {
                                ItemStack kickBtn = createItem(
                                                Material.RED_DYE,
                                                "<red>Klandan At</red>",
                                                Arrays.asList(
                                                                "",
                                                                "<gray>Bu üyeyi klandan at.</gray>",
                                                                "",
                                                                "<dark_red>⚠ Bu işlem geri alınamaz!</dark_red>",
                                                                "",
                                                                "<gold>> Tıkla</gold>"));
                                setItemData(kickBtn, "action", "kick");
                                setItemData(kickBtn, "target", targetUuid);
                                inv.setItem(15, kickBtn);
                        }
                }
                if (clan.getLeaderUUID().equals(player.getUniqueId()) &&
                                !targetMember.getUuid().equals(player.getUniqueId())) {
                        ItemStack transferBtn = createItem(
                                        Material.GOLDEN_HELMET,
                                        "<gold>Liderliği Devret</gold>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Liderliği bu oyuncuya devret.</gray>",
                                                        "",
                                                        "<dark_red>⚠ Bu işlem geri alınamaz!</dark_red>",
                                                        "",
                                                        "<gold>> Shift + Tıkla</gold>"));
                        setItemData(transferBtn, "action", "transfer");
                        setItemData(transferBtn, "target", targetUuid);
                        inv.setItem(22, transferBtn);
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Üye listesine dön</dark_gray>"));
                setItemData(backBtn, "action", "back_members");
                inv.setItem(18, backBtn);

                player.openInventory(inv);
        }
        public void openStorageMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(STORAGE_MENU), 54,
                                LexonClan.parseMessage("<dark_gray>Klan Kasası</dark_gray>"));

                ItemStack[] storage = clan.getStorage();
                if (storage != null) {
                        for (int i = 0; i < Math.min(storage.length, 54); i++) {
                                if (storage[i] != null) {
                                        inv.setItem(i, storage[i]);
                                }
                        }
                }

                player.openInventory(inv);
        }
        public void openInviteMenu(Player player, Clan clan, int page) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(INVITE_MENU), 54,
                                LexonClan.parseMessage("<dark_gray>Oyuncu Davet Et</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                List<Player> availablePlayers = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!plugin.getClanManager().hasClan(p.getUniqueId()) && !p.equals(player)) {
                                availablePlayers.add(p);
                        }
                }

                if (availablePlayers.isEmpty()) {
                        ItemStack noPlayers = createItem(
                                        Material.BARRIER,
                                        "<red>Davet edilebilecek oyuncu yok</red>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Şu anda klansız çevrimiçi oyuncu yok.</gray>"));
                        inv.setItem(22, noPlayers);
                } else {
                        int itemsPerPage = 28;
                        int totalPages = (int) Math.ceil((double) availablePlayers.size() / itemsPerPage);
                        int startIndex = page * itemsPerPage;
                        int endIndex = Math.min(startIndex + itemsPerPage, availablePlayers.size());

                        int[] slots = { 10, 11, 12, 13, 14, 15, 16,
                                        19, 20, 21, 22, 23, 24, 25,
                                        28, 29, 30, 31, 32, 33, 34,
                                        37, 38, 39, 40, 41, 42, 43 };

                        int slotIndex = 0;
                        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++) {
                                Player target = availablePlayers.get(i);

                                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                                SkullMeta meta = (SkullMeta) head.getItemMeta();
                                meta.setOwningPlayer(target);
                                meta.displayName(LexonClan
                                                .parseMessage("<!italic><white>" + target.getName() + "</white>"));
                                meta.lore(Arrays.asList(
                                                Component.empty(),
                                                LexonClan.parseMessage("<!italic><gold>> Tıkla - Davet Et</gold>")));

                                meta.getPersistentDataContainer().set(
                                                new NamespacedKey(plugin, "invite_target"),
                                                PersistentDataType.STRING,
                                                target.getUniqueId().toString());

                                head.setItemMeta(meta);
                                inv.setItem(slots[slotIndex], head);
                                slotIndex++;
                        }

                        if (page > 0) {
                                ItemStack prevPage = createItem(Material.ARROW, "<white>◄ Önceki</white>", null);
                                setItemData(prevPage, "action", "prev_page_invite");
                                setItemData(prevPage, "page", String.valueOf(page - 1));
                                inv.setItem(45, prevPage);
                        }

                        if (page < totalPages - 1) {
                                ItemStack nextPage = createItem(Material.ARROW, "<white>Sonraki ►</white>", null);
                                setItemData(nextPage, "action", "next_page_invite");
                                setItemData(nextPage, "page", String.valueOf(page + 1));
                                inv.setItem(53, nextPage);
                        }
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Ana menüye dön</dark_gray>"));
                setItemData(backBtn, "action", "back_main");
                inv.setItem(49, backBtn);

                player.openInventory(inv);
        }
        public void openRolesMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(ROLES_MENU), 45,
                                LexonClan.parseMessage("<dark_gray>Roller</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 45);

                List<ClanRole> roles = new ArrayList<>(clan.getRoles());
                roles.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

                int slot = 10;
                for (ClanRole role : roles) {
                        long memberCount = clan.getMembers().stream()
                                        .filter(m -> m.getRoleId().equals(role.getId()))
                                        .count();

                        ItemStack roleItem = createItem(
                                        Material.NAME_TAG,
                                        role.getDisplayName(),
                                        Arrays.asList(
                                                        "",
                                                        "<dark_gray>►</dark_gray> <gray>ID:</gray> <white>"
                                                                        + role.getId() + "</white>",
                                                        "<dark_gray>►</dark_gray> <gray>Öncelik:</gray> <white>"
                                                                        + role.getPriority() + "</white>",
                                                        "<dark_gray>►</dark_gray> <gray>Üyeler:</gray> <white>"
                                                                        + memberCount + "</white>",
                                                        "<dark_gray>►</dark_gray> <gray>İzinler:</gray> <white>"
                                                                        + role.getPermissions().size() + "</white>",
                                                        "",
                                                        "<gold>> Sol Tık - Düzenle</gold>",
                                                        "<red>> Shift + Sağ Tık - Sil</red>"));
                        setItemData(roleItem, "action", "edit_role");
                        setItemData(roleItem, "role_id", role.getId());
                        inv.setItem(slot, roleItem);

                        slot++;
                        if ((slot + 1) % 9 == 0)
                                slot += 2;
                        if (slot >= 35)
                                break;
                }
                int maxRoles = plugin.getConfig().getInt("settings.max-roles", 7);
                if (clan.getCustomRoleCount() < maxRoles) {
                        ItemStack newRoleBtn = createItem(
                                        Material.LIME_DYE,
                                        "<green>+ Yeni Rol</green>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Yeni bir özel rol oluştur.</gray>",
                                                        "",
                                                        "<dark_gray>►</dark_gray> <gray>Mevcut:</gray> <white>"
                                                                        + clan.getCustomRoleCount() + "<gray>/</gray>"
                                                                        + maxRoles + "</white>",
                                                        "",
                                                        "<gold>> Tıkla</gold>"));
                        setItemData(newRoleBtn, "action", "create_role");
                        inv.setItem(4, newRoleBtn);
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Ayarlara dön</dark_gray>"));
                setItemData(backBtn, "action", "back_settings");
                inv.setItem(40, backBtn);

                player.openInventory(inv);
        }

        
        public void openRolePermissionsMenu(Player player, Clan clan, ClanRole role) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(ROLE_PERMISSIONS), 54,
                                LexonClan.parseMessage("<dark_gray>" + role.getId() + " - İzinler</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                ItemStack roleInfo = createItem(
                                Material.NAME_TAG,
                                role.getDisplayName(),
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>ID:</gray> <white>" + role.getId()
                                                                + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Öncelik:</gray> <white>"
                                                                + role.getPriority() + "</white>"));
                inv.setItem(4, roleInfo);
                ConfigurationSection permsSection = plugin.getConfig().getConfigurationSection("role-permissions");
                if (permsSection != null) {
                        int slot = 19;
                        for (String configKey : permsSection.getKeys(false)) {
                                ConfigurationSection permConfig = permsSection.getConfigurationSection(configKey);
                                if (permConfig == null)
                                        continue;

                                String permKey = permConfig.getString("permission", configKey);
                                String permName = permConfig.getString("name", configKey);
                                String permDesc = permConfig.getString("description", "");

                                boolean hasPermission = role.hasPermission(permKey);

                                ItemStack permItem = createItem(
                                                hasPermission ? Material.LIME_DYE : Material.GRAY_DYE,
                                                (hasPermission ? "<green>✔ " : "<red>✘ ") + "<white>" + permName
                                                                + "</white>",
                                                Arrays.asList(
                                                                "",
                                                                "<gray>" + permDesc + "</gray>",
                                                                "",
                                                                "<dark_gray>►</dark_gray> <gray>Durum:</gray> "
                                                                                + (hasPermission ? "<green>Aktif</green>"
                                                                                                : "<red>Pasif</red>"),
                                                                "",
                                                                "<gold>> Tıkla - Değiştir</gold>"));
                                setItemData(permItem, "action", "toggle_perm");
                                setItemData(permItem, "permission", permKey);
                                setItemData(permItem, "role_id", role.getId());

                                inv.setItem(slot, permItem);
                                slot++;
                                if ((slot + 1) % 9 == 0)
                                        slot += 2;
                                if (slot >= 44)
                                        break;
                        }
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Rol listesine dön</dark_gray>"));
                setItemData(backBtn, "action", "back_roles");
                inv.setItem(49, backBtn);

                player.openInventory(inv);
        }

        public void openLimitIncreaseMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder("lexonclan:limit_increase"), 27,
                                LexonClan.parseMessage("<dark_gray>Üye Limiti</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 27);

                int currentCoins = countCoins(player);

                ItemStack infoItem = createItem(
                                Material.GOLD_INGOT,
                                "<white>Bilgi</white>",
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Mevcut Limit:</gray> <white>"
                                                                + clan.getMemberLimit() + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Coin'iniz:</gray> <gold>" + currentCoins
                                                                + "</gold>",
                                                ""));
                inv.setItem(4, infoItem);

                List<Map<?, ?>> options = plugin.getConfig().getMapList("coin.increase-options");
                int slot = 10;

                for (Map<?, ?> option : options) {
                        int amount = ((Number) option.get("amount")).intValue();
                        int price = ((Number) option.get("price")).intValue();

                        boolean canAfford = currentCoins >= price;

                        ItemStack optionItem = createItem(
                                        canAfford ? Material.LIME_DYE : Material.GRAY_DYE,
                                        (canAfford ? "<green>" : "<red>") + "+" + amount + " Üye",
                                        Arrays.asList(
                                                        "",
                                                        "<dark_gray>►</dark_gray> <gray>Fiyat:</gray> <gold>" + price
                                                                        + " Coin</gold>",
                                                        "",
                                                        canAfford ? "<green>✔ Satın alabilirsiniz</green>"
                                                                        : "<red>✘ Yetersiz coin</red>",
                                                        "",
                                                        canAfford ? "<gold>> Tıkla - Satın Al</gold>" : ""));
                        setItemData(optionItem, "action", "buy_limit");
                        setItemData(optionItem, "amount", String.valueOf(amount));
                        setItemData(optionItem, "price", String.valueOf(price));
                        inv.setItem(slot, optionItem);

                        slot += 2;
                        if (slot > 16)
                                break;
                }
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Ayarlara dön</dark_gray>"));
                setItemData(backBtn, "action", "back_settings");
                inv.setItem(22, backBtn);

                player.openInventory(inv);
        }

        public void openDeleteConfirmMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(DELETE_CONFIRM), 27,
                                LexonClan.parseMessage("<dark_gray>Klan Silme Onayı</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                ItemStack warning = createItem(
                                Material.BARRIER,
                                "<red>⚠ Dikkat!</red>",
                                Arrays.asList(
                                                "",
                                                "<gray>Klanınızı silmek üzeresiniz:</gray>",
                                                "<white>" + clan.getName() + " [" + clan.getTag() + "]</white>",
                                                "",
                                                "<dark_red>Bu işlem geri alınamaz!</dark_red>",
                                                "<dark_red>Tüm üyeler, roller ve depo silinecek!</dark_red>"));
                inv.setItem(4, warning);
                ItemStack confirmBtn = createItem(
                                Material.LIME_DYE,
                                "<green>Evet, Sil</green>",
                                Arrays.asList(
                                                "",
                                                "<gray>Klanı kalıcı olarak sil.</gray>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(confirmBtn, "action", "confirm_delete");
                inv.setItem(11, confirmBtn);
                ItemStack cancelBtn = createItem(
                                Material.RED_DYE,
                                "<red>Hayır, İptal</red>",
                                Arrays.asList(
                                                "",
                                                "<gray>İşlemi iptal et.</gray>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(cancelBtn, "action", "cancel_delete");
                inv.setItem(15, cancelBtn);

                player.openInventory(inv);
        }

        public void openPublicClansMenu(Player player, int page) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(PUBLIC_CLANS), 54,
                                LexonClan.parseMessage("<dark_gray>Açık Klanlar</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);

                List<Clan> publicClans = plugin.getClanManager().getPublicClans();

                if (publicClans.isEmpty()) {
                        ItemStack noClans = createItem(
                                        Material.BARRIER,
                                        "<red>Herkese açık klan yok</red>",
                                        Arrays.asList(
                                                        "",
                                                        "<gray>Şu anda katılabileceğiniz</gray>",
                                                        "<gray>herkese açık klan bulunmuyor.</gray>"));
                        inv.setItem(22, noClans);
                } else {
                        int itemsPerPage = 28;
                        int totalPages = (int) Math.ceil((double) publicClans.size() / itemsPerPage);
                        int startIndex = page * itemsPerPage;
                        int endIndex = Math.min(startIndex + itemsPerPage, publicClans.size());

                        int[] slots = { 10, 11, 12, 13, 14, 15, 16,
                                        19, 20, 21, 22, 23, 24, 25,
                                        28, 29, 30, 31, 32, 33, 34,
                                        37, 38, 39, 40, 41, 42, 43 };

                        int slotIndex = 0;
                        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++) {
                                Clan clan = publicClans.get(i);

                                ItemStack clanItem = createItem(
                                                clan.isPremium() ? Material.NETHER_STAR : Material.SHIELD,
                                                (clan.isPremium() ? "<gold>⭐ </gold>" : "") + "<white>" + clan.getName()
                                                                + "</white> <dark_gray>[" + clan.getTag()
                                                                + "]</dark_gray>",
                                                Arrays.asList(
                                                                "",
                                                                "<dark_gray>►</dark_gray> <gray>Lider:</gray> <white>"
                                                                                + Bukkit.getOfflinePlayer(
                                                                                                clan.getLeaderUUID())
                                                                                                .getName()
                                                                                + "</white>",
                                                                "<dark_gray>►</dark_gray> <gray>Üyeler:</gray> <white>"
                                                                                + clan.getMemberCount()
                                                                                + "<gray>/</gray>"
                                                                                + clan.getMemberLimit() + "</white>",
                                                                "<dark_gray>►</dark_gray> <gray>Online:</gray> <green>"
                                                                                + clan.getOnlineMemberCount()
                                                                                + "</green>",
                                                                "",
                                                                "<gold>> Tıkla - Detaylar</gold>"));
                                setItemData(clanItem, "action", "view_clan");
                                setItemData(clanItem, "clan_id", clan.getId());
                                inv.setItem(slots[slotIndex], clanItem);
                                slotIndex++;
                        }

                        if (page > 0) {
                                ItemStack prevPage = createItem(Material.ARROW, "<white>◄ Önceki</white>", null);
                                setItemData(prevPage, "action", "prev_page_public");
                                setItemData(prevPage, "page", String.valueOf(page - 1));
                                inv.setItem(45, prevPage);
                        }

                        if (page < totalPages - 1) {
                                ItemStack nextPage = createItem(Material.ARROW, "<white>Sonraki ►</white>", null);
                                setItemData(nextPage, "action", "next_page_public");
                                setItemData(nextPage, "page", String.valueOf(page + 1));
                                inv.setItem(53, nextPage);
                        }
                }
                ItemStack closeBtn = createItem(Material.BARRIER, "<gray>Kapat</gray>", null);
                setItemData(closeBtn, "action", "close");
                inv.setItem(49, closeBtn);

                player.openInventory(inv);
        }

        
        public void openClanDetailMenu(Player player, Clan clan) {
                Inventory inv = Bukkit.createInventory(new GUIHolder(CLAN_DETAIL), 27,
                                LexonClan.parseMessage("<dark_gray>" + clan.getName() + "</dark_gray>"));

                fillBackground(inv, Material.BLACK_STAINED_GLASS_PANE);
                addDecoration(inv, 27);
                ItemStack clanInfo = createItem(
                                clan.isPremium() ? Material.NETHER_STAR : Material.SHIELD,
                                (clan.isPremium() ? "<gold>⭐ </gold>" : "") + "<white>" + clan.getName()
                                                + "</white> <dark_gray>[" + clan.getTag() + "]</dark_gray>",
                                Arrays.asList(
                                                "",
                                                "<dark_gray>►</dark_gray> <gray>Lider:</gray> <white>" + Bukkit
                                                                .getOfflinePlayer(clan.getLeaderUUID()).getName()
                                                                + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Üyeler:</gray> <white>"
                                                                + clan.getMemberCount() + "<gray>/</gray>"
                                                                + clan.getMemberLimit() + "</white>",
                                                "<dark_gray>►</dark_gray> <gray>Online:</gray> <green>"
                                                                + clan.getOnlineMemberCount() + "</green>",
                                                "<dark_gray>►</dark_gray> <gray>Tür:</gray> "
                                                                + (clan.isPremium() ? "<gold>Premium</gold>"
                                                                                : "<gray>Normal</gray>"),
                                                ""));
                inv.setItem(4, clanInfo);
                boolean isFull = clan.getMemberCount() >= clan.getMemberLimit();
                ItemStack joinBtn = createItem(
                                isFull ? Material.BARRIER : Material.LIME_DYE,
                                isFull ? "<red>Klan Dolu!</red>" : "<green>Katıl</green>",
                                Arrays.asList(
                                                "",
                                                isFull ? "<gray>Bu klana katılamazsınız, dolu!</gray>"
                                                                : "<gray>Bu klana katılın.</gray>",
                                                "",
                                                isFull ? "" : "<gold>> Tıkla</gold>"));
                if (!isFull) {
                        setItemData(joinBtn, "action", "join_clan");
                        setItemData(joinBtn, "clan_id", clan.getId());
                }
                inv.setItem(11, joinBtn);
                ItemStack membersBtn = createItem(
                                Material.PLAYER_HEAD,
                                "<white>Üyeleri Gör</white>",
                                Arrays.asList(
                                                "",
                                                "<gray>Klan üyelerini görüntüle.</gray>",
                                                "",
                                                "<gold>> Tıkla</gold>"));
                setItemData(membersBtn, "action", "view_members");
                setItemData(membersBtn, "clan_id", clan.getId());
                inv.setItem(15, membersBtn);
                ItemStack backBtn = createItem(Material.ARROW, "<gray>Geri</gray>",
                                Collections.singletonList("<dark_gray>Klan listesine dön</dark_gray>"));
                setItemData(backBtn, "action", "back_public");
                inv.setItem(22, backBtn);

                player.openInventory(inv);
        }

        private ItemStack createItem(Material material, String displayName, List<String> loreLines) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                        meta.displayName(LexonClan.parseMessage("<!italic>" + displayName));

                        if (loreLines != null && !loreLines.isEmpty()) {
                                List<Component> lore = new ArrayList<>();
                                for (String line : loreLines) {
                                        if (line != null && !line.isEmpty()) {
                                                lore.add(LexonClan.parseMessage("<!italic>" + line));
                                        }
                                }
                                meta.lore(lore);
                        }
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                        item.setItemMeta(meta);
                }

                return item;
        }

        private void setItemData(ItemStack item, String key, String value) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                        meta.getPersistentDataContainer().set(
                                        new NamespacedKey(plugin, key),
                                        PersistentDataType.STRING,
                                        value);
                        item.setItemMeta(meta);
                }
        }

        private void fillBackground(Inventory inv, Material material) {
                ItemStack bg = new ItemStack(material);
                ItemMeta meta = bg.getItemMeta();
                if (meta != null) {
                        meta.displayName(Component.empty());
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                        bg.setItemMeta(meta);
                }

                for (int i = 0; i < inv.getSize(); i++) {
                        inv.setItem(i, bg);
                }
        }

        
        private void addDecoration(Inventory inv, int size) {
                ItemStack gold = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
                ItemMeta meta = gold.getItemMeta();
                if (meta != null) {
                        meta.displayName(Component.empty());
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                        gold.setItemMeta(meta);
                }
                if (size >= 27) {
                        inv.setItem(0, gold);
                        inv.setItem(8, gold);
                        inv.setItem(size - 9, gold);
                        inv.setItem(size - 1, gold);
                }
        }

        private int countCoins(Player player) {
                String materialName = plugin.getConfig().getString("coin.material", "GOLD_BLOCK");
                Material coinMaterial;
                try {
                        coinMaterial = Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                        coinMaterial = Material.GOLD_BLOCK;
                }

                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == coinMaterial) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null && meta.hasDisplayName()) {
                                        count += item.getAmount();
                                }
                        }
                }
                return count;
        }

        public static class GUIHolder implements InventoryHolder {
                private final String guiId;

                public GUIHolder(String guiId) {
                        this.guiId = guiId;
                }

                public String getGuiId() {
                        return guiId;
                }

                @Override
                public Inventory getInventory() {
                        return null;
                }
        }
}
