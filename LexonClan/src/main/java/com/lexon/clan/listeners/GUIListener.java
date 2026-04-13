package com.lexon.clan.listeners;

import com.lexon.clan.LexonClan;
import com.lexon.clan.gui.GUIManager;
import com.lexon.clan.model.Clan;
import com.lexon.clan.model.ClanMember;
import com.lexon.clan.model.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIListener implements Listener {

    private final LexonClan plugin;

    public GUIListener(LexonClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GUIManager.GUIHolder holder)) {
            return;
        }

        String guiId = holder.getGuiId();
        if (guiId.equals(GUIManager.STORAGE_MENU)) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick() || event.getClick().isShiftClick()) {
                if (!clan.hasPermission(player.getUniqueId(), "clan.storage.withdraw")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessage("storage-no-permission"));
                }
            }
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String action = container.get(new NamespacedKey(plugin, "action"), PersistentDataType.STRING);

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        String inviteTarget = container.get(new NamespacedKey(plugin, "invite_target"), PersistentDataType.STRING);
        if (inviteTarget != null && clan != null) {
            handleInviteClick(player, clan, UUID.fromString(inviteTarget));
            return;
        }

        if (action == null) {
            String memberUuid = container.get(new NamespacedKey(plugin, "member_uuid"), PersistentDataType.STRING);
            if (memberUuid != null && clan != null) {
                ClanMember targetMember = clan.getMember(UUID.fromString(memberUuid));
                if (targetMember != null) {
                    plugin.getGuiManager().openMemberActionsMenu(player, clan, targetMember);
                }
            }
            return;
        }
        switch (action) {
            case "create_normal" -> {
                player.closeInventory();
                plugin.getChatIsolationManager().startClanCreation(player, false);
            }

            case "create_premium" -> {
                if (!player.hasPermission("lexonclan.premium")) {
                    player.closeInventory();
                    player.sendMessage(plugin.getMessage("premium-required"));
                    return;
                }
                player.closeInventory();
                plugin.getChatIsolationManager().startClanCreation(player, true);
            }
            case "open_members" -> {
                if (clan != null) {
                    plugin.getGuiManager().openMembersMenu(player, clan, 0);
                }
            }

            case "open_storage" -> {
                if (clan != null && clan.hasPermission(player.getUniqueId(), "clan.storage.view")) {
                    plugin.getGuiManager().openStorageMenu(player, clan);
                } else {
                    player.sendMessage(plugin.getMessage("no-permission"));
                }
            }

            case "open_invite" -> {
                if (clan != null && clan.hasPermission(player.getUniqueId(), "clan.invite")) {
                    plugin.getGuiManager().openInviteMenu(player, clan, 0);
                } else {
                    player.sendMessage(plugin.getMessage("no-permission"));
                }
            }

            case "open_settings" -> {
                if (clan != null && (clan.hasPermission(player.getUniqueId(), "clan.settings") ||
                        clan.getLeaderUUID().equals(player.getUniqueId()))) {
                    plugin.getGuiManager().openSettingsMenu(player, clan);
                } else {
                    player.sendMessage(plugin.getMessage("no-permission"));
                }
            }

            case "delete_clan" -> {
                if (clan != null && clan.getLeaderUUID().equals(player.getUniqueId())) {
                    plugin.getGuiManager().openDeleteConfirmMenu(player, clan);
                }
            }

            case "leave_clan" -> {
                if (clan != null && !clan.getLeaderUUID().equals(player.getUniqueId())) {
                    plugin.getClanManager().removeMemberFromClan(clan.getId(), player.getUniqueId());
                    player.closeInventory();
                    player.sendMessage(plugin.getMessage("member-left", "player", player.getName()));
                    for (ClanMember member : clan.getMembers()) {
                        Player memberPlayer = member.getPlayer();
                        if (memberPlayer != null) {
                            memberPlayer.sendMessage(plugin.getMessage("member-left", "player", player.getName()));
                        }
                    }
                }
            }
            case "toggle_privacy" -> {
                if (clan != null && clan.hasPermission(player.getUniqueId(), "clan.settings")) {
                    clan.setPrivate(!clan.isPrivate());
                    plugin.getClanManager().saveClan(clan);
                    String privacy = clan.isPrivate() ? "Özel" : "Açık";
                    player.sendMessage(LexonClan
                            .parseMessage("<gray>Gizlilik ayarlandı:</gray> <white>" + privacy + "</white>"));
                    plugin.getGuiManager().openSettingsMenu(player, clan);
                }
            }

            case "open_roles" -> {
                if (clan != null && clan.hasPermission(player.getUniqueId(), "clan.roles.manage")) {
                    plugin.getGuiManager().openRolesMenu(player, clan);
                } else {
                    player.sendMessage(plugin.getMessage("no-permission"));
                }
            }

            case "open_limit_increase" -> {
                if (clan != null && clan.getLeaderUUID().equals(player.getUniqueId())) {
                    plugin.getGuiManager().openLimitIncreaseMenu(player, clan);
                }
            }

            case "upgrade_premium" -> {
                if (clan != null && !clan.isPremium()) {
                    if (player.hasPermission("lexonclan.premium")) {
                        clan.setPremium(true);
                        int premiumLimit = plugin.getConfig().getInt("settings.member-limits.premium-clan", 50);
                        clan.setMemberLimit(premiumLimit);
                        plugin.getClanManager().saveClan(clan);
                        player.sendMessage(LexonClan.parseMessage("<gold>⭐ Klanınız premium'a yükseltildi!</gold>"));
                        plugin.getGuiManager().openSettingsMenu(player, clan);
                    } else {
                        player.sendMessage(plugin.getMessage("premium-required"));
                    }
                }
            }

            case "back_settings" -> {
                if (clan != null) {
                    plugin.getGuiManager().openSettingsMenu(player, clan);
                }
            }
            case "prev_page", "next_page" -> {
                String pageStr = container.get(new NamespacedKey(plugin, "page"), PersistentDataType.STRING);
                if (pageStr != null && clan != null) {
                    int page = Integer.parseInt(pageStr);
                    plugin.getGuiManager().openMembersMenu(player, clan, page);
                }
            }

            case "back_main" -> {
                if (clan != null) {
                    plugin.getGuiManager().openManagementMenu(player, clan);
                } else {
                    plugin.getGuiManager().openMainMenu(player);
                }
            }

            case "back_members" -> {
                if (clan != null) {
                    plugin.getGuiManager().openMembersMenu(player, clan, 0);
                }
            }
            case "prev_page_invite", "next_page_invite" -> {
                String pageStr = container.get(new NamespacedKey(plugin, "page"), PersistentDataType.STRING);
                if (pageStr != null && clan != null) {
                    int page = Integer.parseInt(pageStr);
                    plugin.getGuiManager().openInviteMenu(player, clan, page);
                }
            }
            case "promote" -> {
                String targetUuid = container.get(new NamespacedKey(plugin, "target"), PersistentDataType.STRING);
                if (targetUuid != null && clan != null) {
                    handlePromotion(player, clan, UUID.fromString(targetUuid));
                }
            }

            case "demote" -> {
                String targetUuid = container.get(new NamespacedKey(plugin, "target"), PersistentDataType.STRING);
                if (targetUuid != null && clan != null) {
                    handleDemotion(player, clan, UUID.fromString(targetUuid));
                }
            }

            case "kick" -> {
                String targetUuid = container.get(new NamespacedKey(plugin, "target"), PersistentDataType.STRING);
                if (targetUuid != null && clan != null) {
                    handleKick(player, clan, UUID.fromString(targetUuid));
                }
            }

            case "transfer" -> {
                if (!event.isShiftClick()) {
                    player.sendMessage(
                            LexonClan.parseMessage("<gray>Shift + Tıkla ile liderliği devredin.</gray>"));
                    return;
                }
                String targetUuid = container.get(new NamespacedKey(plugin, "target"), PersistentDataType.STRING);
                if (targetUuid != null && clan != null) {
                    handleTransfer(player, clan, UUID.fromString(targetUuid));
                }
            }
            case "edit_role" -> {
                String roleId = container.get(new NamespacedKey(plugin, "role_id"), PersistentDataType.STRING);
                if (roleId != null && clan != null) {
                    ClanRole role = clan.getRole(roleId);
                    if (role != null) {
                        plugin.getGuiManager().openRolePermissionsMenu(player, clan, role);
                    }
                }
            }

            case "create_role" -> {
                if (clan != null) {
                    player.closeInventory();
                    plugin.getChatIsolationManager().startRoleCreation(player, clan);
                }
            }

            case "toggle_perm" -> {
                String permission = container.get(new NamespacedKey(plugin, "permission"), PersistentDataType.STRING);
                String roleId = container.get(new NamespacedKey(plugin, "role_id"), PersistentDataType.STRING);
                if (permission != null && roleId != null && clan != null) {
                    ClanRole role = clan.getRole(roleId);
                    if (role != null && !roleId.equals("leader")) {
                        role.togglePermission(permission);
                        plugin.getClanManager().saveClan(clan);
                        plugin.getGuiManager().openRolePermissionsMenu(player, clan, role);
                    }
                }
            }

            case "back_roles" -> {
                if (clan != null) {
                    plugin.getGuiManager().openRolesMenu(player, clan);
                }
            }
            case "buy_limit" -> {
                String amountStr = container.get(new NamespacedKey(plugin, "amount"), PersistentDataType.STRING);
                String priceStr = container.get(new NamespacedKey(plugin, "price"), PersistentDataType.STRING);
                if (amountStr != null && priceStr != null && clan != null) {
                    handleLimitIncrease(player, clan, Integer.parseInt(amountStr), Integer.parseInt(priceStr));
                }
            }
            case "confirm_delete" -> {
                if (clan != null && clan.getLeaderUUID().equals(player.getUniqueId())) {
                    String clanName = clan.getName();
                    for (ClanMember member : clan.getMembers()) {
                        Player memberPlayer = member.getPlayer();
                        if (memberPlayer != null) {
                            memberPlayer.sendMessage(plugin.getMessage("clan-deleted", "clan", clanName));
                        }
                    }

                    plugin.getClanManager().deleteClan(clan.getId());
                    player.closeInventory();
                    player.sendMessage(plugin.getMessage("clan-deleted", "clan", clanName));
                }
            }

            case "cancel_delete" -> {
                if (clan != null) {
                    plugin.getGuiManager().openManagementMenu(player, clan);
                } else {
                    player.closeInventory();
                }
            }
            case "view_clan" -> {
                String clanId = container.get(new NamespacedKey(plugin, "clan_id"), PersistentDataType.STRING);
                if (clanId != null) {
                    Clan viewClan = plugin.getClanManager().getClan(clanId);
                    if (viewClan != null) {
                        plugin.getGuiManager().openClanDetailMenu(player, viewClan);
                    }
                }
            }

            case "join_clan" -> {
                if (plugin.getClanManager().hasClan(player.getUniqueId())) {
                    player.sendMessage(plugin.getMessage("clan-already-has"));
                    return;
                }

                String clanId = container.get(new NamespacedKey(plugin, "clan_id"), PersistentDataType.STRING);
                if (clanId != null) {
                    Clan joinClan = plugin.getClanManager().getClan(clanId);
                    if (joinClan != null && !joinClan.isPrivate()) {
                        if (plugin.getClanManager().addMemberToClan(clanId, player.getUniqueId())) {
                            player.closeInventory();
                            player.sendMessage(plugin.getMessage("joined-public-clan", "clan", joinClan.getName()));
                            for (ClanMember member : joinClan.getMembers()) {
                                Player memberPlayer = member.getPlayer();
                                if (memberPlayer != null && !memberPlayer.equals(player)) {
                                    memberPlayer.sendMessage(
                                            plugin.getMessage("member-joined", "player", player.getName()));
                                }
                            }
                        } else {
                            player.sendMessage(plugin.getMessage("clan-full"));
                        }
                    }
                }
            }

            case "prev_page_public", "next_page_public" -> {
                String pageStr = container.get(new NamespacedKey(plugin, "page"), PersistentDataType.STRING);
                if (pageStr != null) {
                    int page = Integer.parseInt(pageStr);
                    plugin.getGuiManager().openPublicClansMenu(player, page);
                }
            }

            case "back_public" -> plugin.getGuiManager().openPublicClansMenu(player, 0);

            case "close" -> player.closeInventory();
        }
    }

    
    private void handleInviteClick(Player player, Clan clan, UUID targetUuid) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            player.sendMessage(LexonClan.parseMessage("<red>Oyuncu çevrimiçi değil!</red>"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(LexonClan.parseMessage("<red>Kendinizi davet edemezsiniz!</red>"));
            return;
        }

        if (plugin.getClanManager().hasClan(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("already-in-clan"));
            plugin.getGuiManager().openInviteMenu(player, clan, 0);
            return;
        }

        if (clan.hasInvite(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("already-invited"));
            return;
        }

        if (clan.getMemberCount() >= clan.getMemberLimit()) {
            player.sendMessage(plugin.getMessage("clan-full"));
            return;
        }
        clan.sendInvite(target.getUniqueId(), 60);
        plugin.getClanManager().saveClan(clan);

        player.sendMessage(plugin.getMessage("invite-sent", "player", target.getName()));
        target.sendMessage(plugin.getMessage("invite-received", "clan", clan.getName()));
        plugin.getGuiManager().openInviteMenu(player, clan, 0);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIManager.GUIHolder holder)) {
            return;
        }
        if (!holder.getGuiId().equals(GUIManager.STORAGE_MENU)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIManager.GUIHolder holder)) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (holder.getGuiId().equals(GUIManager.STORAGE_MENU)) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                ItemStack[] contents = event.getInventory().getContents();
                clan.setStorage(contents);
                plugin.getClanManager().saveClan(clan);
            }
        }
    }
    private void handlePromotion(Player player, Clan clan, UUID targetUuid) {
        ClanMember target = clan.getMember(targetUuid);
        ClanMember executor = clan.getMember(player.getUniqueId());

        if (target == null || executor == null)
            return;

        ClanRole targetRole = clan.getRole(target.getRoleId());
        ClanRole executorRole = clan.getRole(executor.getRoleId());

        if (targetRole == null || executorRole == null)
            return;

        if (!executorRole.isHigherThan(targetRole)) {
            player.sendMessage(plugin.getMessage("cannot-promote-higher"));
            return;
        }
        List<ClanRole> roles = new ArrayList<>(clan.getRoles());
        roles.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));

        ClanRole nextRole = null;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getId().equals(target.getRoleId()) && i < roles.size() - 1) {
                ClanRole potentialNext = roles.get(i + 1);
                if (executorRole.isHigherThan(potentialNext) || executorRole.getId().equals("leader")) {
                    nextRole = potentialNext;
                }
                break;
            }
        }

        if (nextRole != null && !nextRole.getId().equals("leader")) {
            target.setRoleId(nextRole.getId());
            plugin.getClanManager().saveClan(clan);

            player.sendMessage(plugin.getMessage("member-promoted",
                    "player", target.getName(),
                    "role", nextRole.getDisplayName()));

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                targetPlayer.sendMessage(plugin.getMessage("member-promoted",
                        "player", target.getName(),
                        "role", nextRole.getDisplayName()));
            }

            plugin.getGuiManager().openMemberActionsMenu(player, clan, target);
        }
    }

    private void handleDemotion(Player player, Clan clan, UUID targetUuid) {
        ClanMember target = clan.getMember(targetUuid);
        ClanMember executor = clan.getMember(player.getUniqueId());

        if (target == null || executor == null)
            return;

        ClanRole targetRole = clan.getRole(target.getRoleId());
        ClanRole executorRole = clan.getRole(executor.getRoleId());

        if (targetRole == null || executorRole == null)
            return;

        if (!executorRole.isHigherThan(targetRole)) {
            player.sendMessage(plugin.getMessage("cannot-kick-higher"));
            return;
        }
        List<ClanRole> roles = new ArrayList<>(clan.getRoles());
        roles.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        ClanRole prevRole = null;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getId().equals(target.getRoleId()) && i < roles.size() - 1) {
                prevRole = roles.get(i + 1);
                break;
            }
        }

        if (prevRole != null) {
            target.setRoleId(prevRole.getId());
            plugin.getClanManager().saveClan(clan);

            player.sendMessage(plugin.getMessage("member-demoted",
                    "player", target.getName(),
                    "role", prevRole.getDisplayName()));

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                targetPlayer.sendMessage(plugin.getMessage("member-demoted",
                        "player", target.getName(),
                        "role", prevRole.getDisplayName()));
            }

            plugin.getGuiManager().openMemberActionsMenu(player, clan, target);
        }
    }

    private void handleKick(Player player, Clan clan, UUID targetUuid) {
        ClanMember target = clan.getMember(targetUuid);
        ClanMember executor = clan.getMember(player.getUniqueId());

        if (target == null || executor == null)
            return;

        ClanRole targetRole = clan.getRole(target.getRoleId());
        ClanRole executorRole = clan.getRole(executor.getRoleId());

        if (targetRole == null || executorRole == null)
            return;

        if (!executorRole.isHigherThan(targetRole)) {
            player.sendMessage(plugin.getMessage("cannot-kick-higher"));
            return;
        }

        String targetName = target.getName();
        Player targetPlayer = target.getPlayer();

        plugin.getClanManager().removeMemberFromClan(clan.getId(), targetUuid);

        player.sendMessage(plugin.getMessage("member-kicked", "player", targetName));

        if (targetPlayer != null) {
            targetPlayer.sendMessage(plugin.getMessage("member-kicked", "player", targetName));
        }

        plugin.getGuiManager().openMembersMenu(player, clan, 0);
    }

    private void handleTransfer(Player player, Clan clan, UUID targetUuid) {
        if (!clan.getLeaderUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        if (clan.transferLeadership(targetUuid)) {
            plugin.getClanManager().saveClan(clan);

            ClanMember newLeader = clan.getMember(targetUuid);
            String newLeaderName = newLeader != null ? newLeader.getName() : "Unknown";
            for (ClanMember member : clan.getMembers()) {
                Player memberPlayer = member.getPlayer();
                if (memberPlayer != null) {
                    memberPlayer.sendMessage(LexonClan.parseMessage(
                            "<gold>⭐ Liderlik devredildi! Yeni lider: " + newLeaderName + "</gold>"));
                }
            }

            player.closeInventory();
        }
    }

    private void handleLimitIncrease(Player player, Clan clan, int amount, int price) {
        String materialName = plugin.getConfig().getString("coin.material", "GOLD_BLOCK");
        Material coinMaterial;
        try {
            coinMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            coinMaterial = Material.GOLD_BLOCK;
        }

        int coinCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == coinMaterial) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    coinCount += item.getAmount();
                }
            }
        }

        if (coinCount < price) {
            player.sendMessage(plugin.getMessage("not-enough-coins",
                    "required", String.valueOf(price),
                    "current", String.valueOf(coinCount)));
            return;
        }
        int remaining = price;
        for (ItemStack item : player.getInventory().getContents()) {
            if (remaining <= 0)
                break;
            if (item != null && item.getType() == coinMaterial) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    if (item.getAmount() <= remaining) {
                        remaining -= item.getAmount();
                        item.setAmount(0);
                    } else {
                        item.setAmount(item.getAmount() - remaining);
                        remaining = 0;
                    }
                }
            }
        }
        clan.increaseMemberLimit(amount);
        plugin.getClanManager().saveClan(clan);

        player.sendMessage(plugin.getMessage("limit-increased", "limit", String.valueOf(clan.getMemberLimit())));
        plugin.getGuiManager().openLimitIncreaseMenu(player, clan);
    }
}
