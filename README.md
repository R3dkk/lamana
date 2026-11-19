# Animal War — 2D Strategy Battle Game

Tugas Akhir Pemrograman Berorientasi Objek (PBO) — Semester 3  
**Stack:** Java 21, Swing, Java2D, Maven, JUnit 5

---

## 1. Deskripsi Game

**Animal War** adalah game strategi pertarungan dua markas horizontal (*battle lane*) yang terinspirasi dari mekanisme *Age of War*. Dua markas (Player Base dan Enemy Base) saling berhadapan. Pemain mengerahkan pasukan hewan khas Indonesia untuk menyerang markas musuh, sementara kecerdasan buatan (Enemy AI) secara otomatis mengelola ekonomi dan mengerahkan pasukannya.

- Setiap unit bergerak otomatis di jalur pertempuran, bertarung jika musuh berada dalam jangkauan, dan memberikan hadiah Gold serta EXP saat dikalahkan.
- Markas memiliki pertahanan Turret terintegrasi yang otomatis menembak unit terdekat dalam jangkauannya.
- Kumpulkan EXP untuk melakukan **Upgrade Base** membuka era baru dengan hewan-hewan yang lebih kuat.
- Hancurkan markas musuh untuk memenangkan permainan!

---

## 2. Cara Menjalankan

### Opsi A — Menggunakan Maven Wrapper (Direkomendasikan)

Proyek ini sudah dilengkapi dengan Maven Wrapper (`mvnw` / `mvnw.cmd`). Cukup buka terminal di folder proyek:

**Windows (PowerShell / Command Prompt):**
```powershell
# Jalankan pengujian unit (22 test methods)
.\mvnw.cmd test

# Build executable JAR
.\mvnw.cmd clean package

# Jalankan game dari JAR
java -jar target/animal-war-1.0.0.jar
```

**Linux / macOS:**
```bash
./mvnw clean package
java -jar target/animal-war-1.0.0.jar
```

---

### Opsi B — Kompilasi Manual (Tanpa Maven)

Jika Maven belum terpasang di sistem:

```powershell
# Kompilasi seluruh source code
$sources = Get-ChildItem src\main\java -Recurse -Filter *.java | ForEach-Object FullName
New-Item -ItemType Directory -Path target\classes -Force | Out-Null
javac -d target\classes $sources

# Jalankan aplikasi Swing
java -cp target\classes lamana.Main
```

### GUI dan aset

Layar game menampilkan arena yang mengikuti ukuran jendela, markas dan HP kedua tim,
antrean spawn, resource, serta kartu pasukan lengkap dengan statistik dan ability.
Gunakan **Jeda/Lanjutkan**, **Mulai ulang**, dan pilihan kesulitan AI untuk mencoba
mekanik pertandingan.

PNG contoh untuk arena, markas, dan tiga unit Era 1 tersedia di
`src/main/resources/assets/`. Sprite unit transparan dirender sebagai model 2D
tampak samping; kartu pasukan memakai PNG yang sama sebagai preview. Ganti file
unit di lokasi yang sama untuk memasang aset buatan tim:

| Aset | Lokasi resource |
|------|-----------------|
| Latar arena | `assets/background/arena.png` |
| Unit | `assets/units/ayam.png`, `landak.png`, `kurakura.png`, `harimau.png`, `monyet.png`, `badak.png`, `godzilla.png`, `naga.png`, `centaur.png` |
| Markas | `assets/buildings/player-base.png`, `enemy-base.png` |

Nama file enam unit era berikutnya sudah dicantumkan pada tabel dan otomatis dibaca
saat ditambahkan. Jika PNG belum ada atau gagal dibaca, GUI memakai bentuk 2D fallback.

---

## 3. Tema Karakter: Fauna Indonesia 🇮🇩

Seluruh 9 hewan menggunakan fauna asli kepulauan Indonesia, terbagi ke dalam 3 era kemajuan markas:

| Era | Hewan | Tipe Role | Kemampuan Khusus (*Polymorphic Trait*) |
|:---:|-------|:---------:|----------------------------------------|
| **1** | 🐓 **Ayam** | Melee | **Double Strike:** Peluang 20% melakukan serangan ganda. |
| **1** | 🦔 **Landak** | Ranged | **Quill Range +1:** Jangkauan efektif serangan bertambah +1 unit jarak. |
| **1** | 🐢 **Kura-kura** | Heavy | **Hard Shell:** Reduksi kerusakan masuk flat 20% (`takeDamage` override). |
| **2** | 🐆 **Harimau** | Melee | **Critical Claws:** Peluang 30% menghasilkan kerusakan ganda (2.0x DMG). |
| **2** | 🐒 **Monyet** | Ranged | **Banana Splash:** Serangan mengenai target utama dan unit di belakangnya. |
| **2** | 🦏 **Badak** | Heavy | **Horn Knockback:** Mendorong mundur unit lawan sejauh 1 slot jarak. |
| **3** | godzila | Melee | **Apex Execution:** Kerusakan bertambah +50% jika target memiliki HP < 30%. |
| **3** | naga | Ranged | **Earthquake Stomp:** Kerusakan area (AoE) ke semua lawan dalam radius 2.0 unit. |
| **3** | centaur | Heavy | **Death Roll:** Mengunci dan melumpuhkan target (*Stun*) selama 1.5 detik. |

---

## 4. Pemetaan 4 Pilar OOP (Untuk Dosen Penguji)

| Pilar | File Sumber | Baris | Penjelasan Teknis |
|-------|-------------|:-----:|-------------------|
| **Abstraction** | `contract/Attackable.java` | L12 | Interface kontrak serang — menyatukan `Animal` dan `Base` dalam satu tipe generik. |
| **Abstraction** | `contract/Upgradable.java` | L13 | Interface kontrak progres markas tanpa membocorkan rincian internal. |
| **Abstraction** | `model/animal/Animal.java` | L15, L122 | Abstract class induk yang mewajibkan implementasi `attack()` dan `getRequiredBaseLevel()`. |
| **Inheritance** | `model/animal/melee/Ayam.java` | L14 | Rantai pewarisan bertingkat: `Ayam -> MeleeAnimal -> Animal -> Object`. |
| **Inheritance** | `model/animal/heavy/KuraKura.java` | L14 | Pewarisan dari `HeavyAnimal` untuk unit tank. |
| **Inheritance** | `model/animal/ranged/GajahSumatera.java` | L14 | Pewarisan dari `RangedAnimal` untuk unit artileri jarak jauh. |
| **Encapsulation** | `model/animal/Animal.java` | L16–L29 | Seluruh atribut berstatus `private`. Validasi defensif pada konstruktor dan `takeDamage()`. |
| **Encapsulation** | `model/economy/MoneyExp.java` | L10–L55 | Proteksi saldo emas dan EXP. Method `spendMoney()` mengembalikan boolean tanpa mengubah state jika saldo kurang. |
| **Encapsulation** | `model/building/Base.java` | L20–L38 | Komposisi internal `Turret` yang tersinkronisasi saat Base naik level. |
| **Polymorphism** | `model/animal/melee/Ayam.java` | L21 | `@Override attack()`: Logika serangan ganda 20%. |
| **Polymorphism** | `model/animal/heavy/KuraKura.java` | L21 | `@Override takeDamage()`: Reduksi damage pasif 20%. |
| **Polymorphism** | `model/animal/melee/HarimauSumatera.java` | L21 | `@Override attack()`: Algoritma eksekusi finisher HP rendah. |
| **Polymorphism** | `model/battle/BattleLane.java` | L205 | Dynamic dispatch seragam: `attacker.attack(target, dist, ctx)` memanggil perilaku unik tanpa `instanceof`. |
| **Polymorphism** | `factory/UnitFactory.java` | L48, L54 | *Compile-time Polymorphism* via method overloading: `create(type)` dan `create(type, qty)`. |

*Dokumentasi detail pemetaan dapat dilihat di [docs/mapping-oop.md](docs/mapping-oop.md).*

---

## 5. Arsitektur Sistem

Aplikasi dibangun dengan prinsip **Separation of Concerns (SoC)** dan **Model-View-Controller (MVC)** yang memisahkan grafis, logika alur, dan data:

```
lamana/
├── Main.java                        <-- Entry Point murni
├── contract/                        <-- Interface Kontrak OOP
│   ├── Attackable.java
│   ├── SpecialAbility.java
│   └── Upgradable.java
├── core/                            <-- Konfigurasi & State
│   ├── GameConfig.java              (Konstanta terpusat, zero magic numbers)
│   └── GameState.java               (Enum: MENU, PLAYING, VICTORY, DEFEAT, DRAW)
├── model/                           <-- Model Data & Entitas Game
│   ├── animal/
│   │   ├── Animal.java              (Abstract Base Unit)
│   │   ├── AttackContext.java       (Context arena untuk efek AoE/Knockback)
│   │   ├── melee/                   (MeleeAnimal, Ayam, Harimau, HarimauSumatera)
│   │   ├── ranged/                  (RangedAnimal, Landak, Monyet, GajahSumatera)
│   │   └── heavy/                   (HeavyAnimal, KuraKura, Badak, BuayaMuara)
│   ├── building/
│   │   ├── Base.java                (Markas, 2 instances: Player & Enemy)
│   │   └── Turret.java              (Senjata pertahanan markas)
│   ├── economy/
│   │   └── MoneyExp.java            (Pengelola Gold & EXP tiap tim)
│   └── battle/
│       └── BattleLane.java          (Arena 1D, pergerakan, spacing & combat)
├── controller/
│   └── GameController.java          (Orkestrator alur logika permainan)
├── ai/
│   ├── EnemyAIController.java       (AI musuh: bayar Gold, evaluasi counter)
│   └── EnemyDifficulty.java         (Tingkat kesulitan AI)
├── factory/
│   └── UnitFactory.java             (Creational Pattern & Overloading)
└── view/                            <-- Presentasi GUI Swing & Java2D
    ├── GameFrame.java               (Window utama, kontrol pertandingan)
    ├── GamePanel.java               (Arena responsif dan render pasukan)
    ├── GameArt.java                 (Pemuatan PNG dan ilustrasi fallback)
    ├── HUDPanel.java                (HP markas, Gold, EXP, progres upgrade)
    ├── SpawnQueuePanel.java         (Antrean spawn dan cooldown)
    └── UnitCard.java                (Tombol spawn dengan statistik dan ability)
```

---

## 6. Pengujian Unit (JUnit 5)

Proyek ini memiliki **22 skenario pengujian unit** yang menguji seluruh aspek inti:
- **`AnimalTest` (9 tests):** Membuktikan polimorfisme `attack()` Ayam (double hit), Harimau (crit), Harimau Sumatera (finisher), Landak (bonus range), Badak (knockback), Buaya Muara (stun), serta reduksi damage Kura-kura dan validasi input.
- **`BaseTest` (5 tests):** Pengujian siklus hidup Base, kalkulasi damage, batas koordinat, upgrade sinkronisasi Turret, dan validasi level spawn.
- **`MoneyExpTest` (4 tests):** Enkapsulasi transaksi keuangan, validasi input non-positif, serta konsistensi saldo.
- **`BattleLaneTest` (4 tests):** Pengujian koordinat awal unit, pergerakan tanpa tabrakan silang (*no front crossing*), dan penyerahan reward saat unit mati.

Jalankan pengujian kapan saja dengan:
```powershell
.\mvnw.cmd test
```
