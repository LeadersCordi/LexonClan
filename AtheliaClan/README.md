# ⚔ LexonClan

Minecraft 1.21.4 Paper/Leaf sunucuları için gelişmiş klan yönetim plugini.

## Özellikler

- **Klan Oluşturma & Silme** — Normal ve Premium klan desteği
- **Üye Yönetimi** — Davet, atma, yükseltme, düşürme, liderlik devretme
- **Rol & İzin Sistemi** — Özelleştirilebilir roller ve 17 farklı izin
- **Public/Private Klanlar** — Açık klanlara doğrudan katılım, özel klanlar davet ile
- **Klan Kasası** — 54 slotluk ortak depo
- **GUI Arayüz** — Sade ve şık envanter tabanlı menüler
- **FancyNPC Entegrasyonu** — NPC üzerinden public klan listesi
- **PlaceholderAPI Desteği** — 15+ placeholder
- **Chat Klan Etiketi** — Normal klanlar gri, premium klanlar altın rengi
- **MySQL & YML Desteği** — İki farklı veri saklama seçeneği
- **LPC Uyumlu** — `%lexonclan_tag_formatted%` ile chat formatı desteği

## Gereksinimler

- Java 21+
- Paper/Leaf 1.21.4+
- (Opsiyonel) PlaceholderAPI
- (Opsiyonel) FancyNpcs
- (Opsiyonel) Vault

## Kurulum

1. `LexonClan.jar` dosyasını `plugins/` klasörüne koyun
2. Sunucuyu başlatın
3. `plugins/LexonClan/config.yml` dosyasını düzenleyin
4. `/klan admin reload` ile config'i yeniden yükleyin

## Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/klan` | Ana menüyü aç | `lexonclan.use` |
| `/klan ayarlar` | Klan ayarları menüsü | `lexonclan.use` |
| `/klan kabul <klan>` | Daveti kabul et | `lexonclan.use` |
| `/klan reddet <klan>` | Daveti reddet | `lexonclan.use` |
| `/klan liste` | Public klanları listele | `lexonclan.use` |
| `/klan admin reload` | Config yeniden yükle | `lexonclan.admin` |
| `/klan admin delete <klan>` | Klanı sil | `lexonclan.admin` |
| `/klan admin setlimit <klan> <limit>` | Üye limiti ayarla | `lexonclan.admin` |

## Yetkiler

| Yetki | Açıklama | Varsayılan |
|-------|----------|------------|
| `lexonclan.use` | Klan sistemini kullanma | Herkes |
| `lexonclan.premium` | Premium klan oluşturma | OP |
| `lexonclan.admin` | Admin komutları | OP |
| `lexonclan.bypass` | Limit atlama | OP |

## Placeholder'lar

| Placeholder | Açıklama |
|-------------|----------|
| `%lexonclan_name%` | Klan ismi |
| `%lexonclan_tag%` | Klan etiketi |
| `%lexonclan_tag_formatted%` | Renkli klan etiketi (LPC uyumlu) |
| `%lexonclan_role%` | Oyuncunun rolü |
| `%lexonclan_role_display%` | Rol görünen adı |
| `%lexonclan_members%` | Üye sayısı |
| `%lexonclan_members_online%` | Online üye sayısı |
| `%lexonclan_members_max%` | Maksimum üye limiti |
| `%lexonclan_leader%` | Lider ismi |
| `%lexonclan_is_leader%` | Lider mi (true/false) |
| `%lexonclan_is_premium%` | Premium mu (true/false) |
| `%lexonclan_is_private%` | Özel mi (true/false) |
| `%lexonclan_has_clan%` | Klanı var mı (true/false) |
| `%lexonclan_total_clans%` | Toplam klan sayısı |

## Rol İzinleri

Klan içi roller için 17 farklı izin:

`clan.manage` · `clan.invite` · `clan.kick` · `clan.promote` · `clan.demote` · `clan.storage.view` · `clan.storage.deposit` · `clan.storage.withdraw` · `clan.roles.manage` · `clan.roles.create` · `clan.roles.delete` · `clan.settings` · `clan.privacy` · `clan.delete` · `clan.chat` · `clan.announce`

## Derleme

```bash
mvn clean package
```

JAR dosyası `target/LexonClan.jar` konumunda oluşturulur.

## Lisans

Bu proje özel kullanım içindir.

---

Developed by **CordiDev**
