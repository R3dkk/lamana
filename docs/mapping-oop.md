# Bukti Implementasi 4 Pilar OOP — Animal War

Dokumen ini disusun untuk dosen penguji dan asisten praktikum Pemrograman Berorientasi Objek (PBO). Berisi pemetaan eksak baris kode dari setiap pilar OOP dalam arsitektur game *Animal War*.

---

## 1. Abstraction (Abstraksi)

Abstraksi memisahkan kontrak antarmuka (*what to do*) dari detail implementasi konkret (*how to do it*).

| Interface / Abstract Class | Lokasi File | Baris | Penjelasan Teknis |
|----------------------------|-------------|-------|-------------------|
| `Attackable` | `src/main/java/lamana/contract/Attackable.java` | L12–L18 | Kontrak entitas yang dapat menerima damage (`takeDamage`, `isDefeated`, `getHp`, `getMaxHp`, `getPosition`). Diimplementasikan oleh `Animal` dan `Base`. |
| `Upgradable` | `src/main/java/lamana/contract/Upgradable.java` | L13–L17 | Kontrak entitas yang dapat ditingkatkan levelnya (`upgrade`, `canUpgrade`, `getLevel`). Diimplementasikan oleh `Base`. |
| `SpecialAbility` | `src/main/java/lamana/contract/SpecialAbility.java` | L11–L13 | Kontrak penamaan kemampuan khusus unit hewan (`getAbilityName`). |
| `Animal` | `src/main/java/lamana/model/animal/Animal.java` | L15, L122 | Abstract class yang mendeklarasikan method abstrak `attack(Attackable, double, AttackContext)` dan `getRequiredBaseLevel()`. |
| `MeleeAnimal` | `src/main/java/lamana/model/animal/melee/MeleeAnimal.java` | L13 | Abstraksi role petarung jarak dekat (range 1). |
| `RangedAnimal` | `src/main/java/lamana/model/animal/ranged/RangedAnimal.java` | L13 | Abstraksi role penyerang jarak jauh. |
| `HeavyAnimal` | `src/main/java/lamana/model/animal/heavy/HeavyAnimal.java` | L13 | Abstraksi role bertahanan tinggi / tank. |

---

## 2. Inheritance (Pewarisan)

Pewarisan mengorganisasikan konsep hierarkis dari umum ke spesifik, memungkinkan *code reuse* dan pembagian tanggung jawab yang rapi.

```
                  ┌───────────────┐
                  │ Attackable    │ (Interface)
                  └───────┬───────┘
                          │ implements
                  ┌───────▼───────┐
                  │ Animal        │ (Abstract Base)
                  └───┬───┬───┬───┘
          ┌───────────┘   │   └───────────┐
  extends │       extends │       extends │
┌─────────▼─────┐ ┌───────▼───────┐ ┌─────▼──────────┐
│  MeleeAnimal  │ │  RangedAnimal │ │  HeavyAnimal   │ (Abstract Roles)
└───┬───┬───┬───┘ └───┬───┬───┬───┘ └───┬───┬───┬───┘
    │   │   │         │   │   │         │   │   │
    │   │   └─────────┼───┼───┼─────────┼───┼───┼── HarimauSumatera
    │   └─────────────┼───┼───┼─────────┼───┼───┼── Harimau
    └─────────────────┼───┼───┼─────────┼───┼───┼── Ayam
                      │   │   └─────────┼───┼───┼── GajahSumatera
                      │   └─────────────┼───┼───┼── Monyet
                      └─────────────────┼───┼───┼── Landak
                                        │   │   └── BuayaMuara
                                        │   └────── Badak
                                        └────────── KuraKura
```

| Kelas Turunan | Kelas Induk | Lokasi File | Penjelasan |
|---------------|-------------|-------------|------------|
| `MeleeAnimal` | `Animal` | `lamana/model/animal/melee/MeleeAnimal.java` | Mewarisi seluruh atribut inti `Animal`, menetapkan range=1 dan cooldown melee. |
| `Ayam` | `MeleeAnimal` | `lamana/model/animal/melee/Ayam.java` | Rantai pewarisan 3 tingkat (`Ayam -> MeleeAnimal -> Animal -> Object`). |
| `Harimau` | `MeleeAnimal` | `lamana/model/animal/melee/Harimau.java` | Mewarisi MeleeAnimal dengan stat Era 2. |
| `HarimauSumatera` | `MeleeAnimal` | `lamana/model/animal/melee/HarimauSumatera.java` | Mewarisi MeleeAnimal dengan stat Era 3. |
| `RangedAnimal` | `Animal` | `lamana/model/animal/ranged/RangedAnimal.java` | Mewarisi `Animal`, menerima parameter konfigurasi jangkauan (`range`). |
| `Landak` | `RangedAnimal` | `lamana/model/animal/ranged/Landak.java` | Hewan Ranged Era 1. |
| `Monyet` | `RangedAnimal` | `lamana/model/animal/ranged/Monyet.java` | Hewan Ranged Era 2. |
| `GajahSumatera` | `RangedAnimal` | `lamana/model/animal/ranged/GajahSumatera.java` | Hewan Ranged Era 3. |
| `HeavyAnimal` | `Animal` | `lamana/model/animal/heavy/HeavyAnimal.java` | Mewarisi `Animal` untuk unit tank. |
| `KuraKura` | `HeavyAnimal` | `lamana/model/animal/heavy/KuraKura.java` | Hewan Heavy Era 1. |
| `Badak` | `HeavyAnimal` | `lamana/model/animal/heavy/Badak.java` | Hewan Heavy Era 2. |
| `BuayaMuara` | `HeavyAnimal` | `lamana/model/animal/heavy/BuayaMuara.java` | Hewan Heavy Era 3. |

---

## 3. Encapsulation (Enkapsulasi)

Seluruh atribut disembunyikan menggunakan akses `private`, diakses melalui getter/setter terkontrol dengan validasi defensif (*defensive programming*).

| Kelas | Atribut Terproteksi | Lokasi File | Mekanisme & Validasi Pertahanan |
|-------|---------------------|-------------|---------------------------------|
| `Animal` | `hp`, `maxHp`, `damage`, `position`, `attackCooldownMs`, dll. | `lamana/model/animal/Animal.java` | Seluruh field `private`. Konstruktor melempar `IllegalArgumentException` jika nama null/kosong atau stat <= 0. `takeDamage(amount)` memvalidasi `amount > 0` dan menjaga HP di batas `Math.max(0, hp - amount)`. |
| `MoneyExp` | `money`, `exp` | `lamana/model/economy/MoneyExp.java` | Field `private int money, exp`. `spendMoney(amount)` mengecek kecukupan saldo dan mengembalikan `false` tanpa mengubah saldo jika tidak cukup. Validasi `validateStrictlyPositive` melempar exception bila `amount <= 0`. |
| `Base` | `hp`, `level`, `turret`, `position` | `lamana/model/building/Base.java` | Posisi divalidasi `[0, LANE_LENGTH]`. Level dinaikkan hanya lewat method `upgrade()` yang memverifikasi kecukupan EXP secara otomatis. |
| `Turret` | `level`, `attackTimerMillis` | `lamana/model/building/Turret.java` | Timer internal dikontrol melalui `advance(deltaMs)` dan `consumeShot()`. Level dibatasi 1–3. |
| `BattleLane` | `playerUnits`, `enemyUnits` | `lamana/model/battle/BattleLane.java` | List unit berstatus `private final`. Getter mengembalikan *defensive copy* (`new ArrayList<>(units)`) untuk mencegah modifikasi eksternal tak terkontrol. |

---

## 4. Polymorphism (Polimorfisme)

### A. Polimorfisme Dinamis (*Runtime Polymorphism / Dynamic Dispatch*)

Terbukti nyata pada kesembilan hewan konkret yang meng-override `attack()` atau `takeDamage()` dengan perilaku unik:

| Hewan | Method Override | Efek Unik | Bukti Pengujian Unit |
|-------|-----------------|-----------|----------------------|
| **Ayam** | `@Override attack(...)` | Peluang 20% *Double Strike* (menyerang 2 kali). | `AnimalTest.testAyamPolymorphicDoubleHit` |
| **Landak** | `@Override attack(...)` | *Quill Range +1* (dapat menyerang hingga jarak `range + 1`). | `AnimalTest.testLandakBonusRange` |
| **Kura-kura** | `@Override takeDamage(...)` | *Hard Shell* (reduksi damage masuk flat 20%). | `AnimalTest.testKuraKuraDamageReduction` |
| **Harimau** | `@Override attack(...)` | Peluang 30% *Critical Claws* (damage dikalikan 2.0x). | `AnimalTest.testHarimauPolymorphicCriticalHit` |
| **Monyet** | `@Override attack(...)` | *Banana Splash* (serang target utama + splash damage ke unit di belakang). | `Monyet.java:27–42` |
| **Badak** | `@Override attack(...)` | *Horn Knockback* (mendorong target mundur 1 unit slot menjauh). | `AnimalTest.testBadakKnockback` |
| **Harimau Sumatera** | `@Override attack(...)` | *Apex Execution* (bonus damage +50% jika HP target < 30%). | `AnimalTest.testHarimauSumateraExecutionFinisher` |
| **Gajah Sumatera** | `@Override attack(...)` | *Earthquake Stomp* (AoE damage ke seluruh musuh dalam radius 2.0). | `GajahSumatera.java:27–40` |
| **Buaya Muara** | `@Override attack(...)` | *Death Roll* (memberikan efek status Stun 1.5 detik ke target). | `AnimalTest.testBuayaMuaraStun` |

### B. Polimorfisme Interface (*Subtyping*)

Pada `BattleLane.java:185–235`, satu pemanggilan:
```java
attacker.attack(target, distance, context);
```
Dapat menargetkan sesama `Animal` **atau** `Base` musuh tanpa pemeriksaan `instanceof`, karena keduanya bertipe `Attackable`.

### C. Polimorfisme Statis (*Compile-time / Method Overloading*)

Pada `UnitFactory.java`:
```java
public static Animal create(String type)
public static List<Animal> create(String type, int quantity)
```
Method dengan nama sama namun parameter berbeda (*overloading*).
