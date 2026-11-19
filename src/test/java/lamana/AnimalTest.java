package lamana;

import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;
import lamana.model.animal.heavy.Badak;
import lamana.model.animal.heavy.Centaur;
import lamana.model.animal.heavy.KuraKura;
import lamana.model.animal.melee.Ayam;
import lamana.model.animal.melee.Godzilla;
import lamana.model.animal.melee.Harimau;
import lamana.model.animal.ranged.Landak;
import lamana.model.animal.ranged.Monyet;
import lamana.model.animal.ranged.Naga;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pengujian Unit untuk hierarki Animal dan pembuktian 4 Pilar OOP:
 * - Abstraction: Animal abstract, Attackable interface
 * - Inheritance: Subclass mewarisi Melee/Ranged/Heavy
 * - Encapsulation: Validasi constructor, getter terkontrol, takeDamage()
 * - Polymorphism: Perilaku attack() dan takeDamage() unik per hewan
 */
public class AnimalTest {

    @Test
    public void testAyamPolymorphicDoubleHit() {
        Ayam ayam = new Ayam();
        assertEquals("Ayam", ayam.getName());
        assertEquals(10, ayam.getDamage());

        int doubleHitCount = 0;
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        for (int i = 0; i < 200; i++) {
            KuraKura target = new KuraKura();
            int hpAwal = target.getHp();
            ayam.attack(target, 1.0, ctx);
            int damageDiterima = hpAwal - target.getHp();

            if (damageDiterima == 16) {
                doubleHitCount++;
            }
        }

        assertTrue(doubleHitCount > 10, "Ayam harus memiliki peluang double strike (20%)");
    }

    @Test
    public void testHarimauPolymorphicCriticalHit() {
        Harimau harimau = new Harimau();
        assertEquals(30, harimau.getDamage());

        int critCount = 0;
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        for (int i = 0; i < 200; i++) {
            KuraKura target = new KuraKura();
            int hpAwal = target.getHp();
            harimau.attack(target, 1.0, ctx);
            int damageDiterima = hpAwal - target.getHp();

            if (damageDiterima == 48) {
                critCount++;
            }
        }

        assertTrue(critCount > 15, "Harimau harus memiliki peluang critical strike (30%)");
    }

    @Test
    public void testKuraKuraDamageReduction() {
        KuraKura kura = new KuraKura();
        assertEquals(200, kura.getHp());

        // Normal damage 50 -> reduksi 20% = 40 damage masuk
        kura.takeDamage(50);
        assertEquals(160, kura.getHp(), "Kura-kura harus mereduksi damage masuk sebesar 20%");
    }

    @Test
    public void testCentaurExecutionFinisher() {
        Centaur centaur = new Centaur();
        assertEquals(75, centaur.getDamage());
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        // Target HP tinggi (> 30%)
        Ayam targetSehat = new Ayam(); // maxHp 80
        targetSehat.setPosition(1.0);
        centaur.attack(targetSehat, 1.0, ctx);
        // Damage normal = 75 -> Ayam HP 80 - 75 = 5
        assertEquals(5, targetSehat.getHp());

        // Target HP kritis (< 30%)
        KuraKura targetKritis = new KuraKura(); // maxHp 200, 30% = 60
        targetKritis.takeDamage(180); // HP tinggal 56 (kritis < 60)
        int hpSebelum = targetKritis.getHp();

        centaur.attack(targetKritis, 1.0, ctx);
        // Execute: 75 + 37 = 112 dmg -> KuraKura (-20%) = 89 dmg -> mati
        assertTrue(targetKritis.isDefeated());
    }

    @Test
    public void testGodzillaAoE() {
        Godzilla godzilla = new Godzilla();
        assertEquals(70, godzilla.getDamage());

        List<Animal> enemies = new ArrayList<>();
        Ayam primary = new Ayam();
        primary.setPosition(5.0);
        Ayam secondary = new Ayam();
        secondary.setPosition(6.0); // radius <= 2.0
        Ayam farAway = new Ayam();
        farAway.setPosition(10.0); // radius > 2.0

        enemies.add(primary);
        enemies.add(secondary);
        enemies.add(farAway);

        AttackContext ctx = new AttackContext(enemies, true);
        godzilla.attack(primary, 1.0, ctx);

        assertEquals(10, primary.getHp()); // 80 - 70 = 10
        assertEquals(10, secondary.getHp()); // AoE hit: 80 - 70 = 10
        assertEquals(80, farAway.getHp()); // Far away untouched
    }

    @Test
    public void testNagaArmorPiercing() {
        Naga naga = new Naga();
        assertEquals(70, naga.getDamage());
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        // Target non-heavy
        Ayam ayam = new Ayam();
        naga.attack(ayam, 2.0, ctx);
        assertEquals(10, ayam.getHp()); // 80 - 70 = 10

        // Target heavy (KuraKura) -> Naga 1.5x damage = 105 dmg -> KuraKura -20% = 84 dmg
        KuraKura kura = new KuraKura();
        int initialHp = kura.getHp();
        naga.attack(kura, 2.0, ctx);
        int damageReceived = initialHp - kura.getHp();
        assertEquals(84, damageReceived);
    }

    @Test
    public void testLandakBonusRange() {
        Landak landak = new Landak();
        assertEquals(3, landak.getRange());
        assertEquals(4, landak.getEffectiveRange());
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        Ayam targetJauh = new Ayam();
        // Jarak 4 (di atas range 3, tapi di dalam bonus range +1)
        boolean hit = landak.attack(targetJauh, 4.0, ctx);
        assertTrue(hit, "Landak harus dapat menyerang pada jarak effective range (jarak 4.0)");

        // Jarak 5 (di luar bonus)
        boolean miss = landak.attack(targetJauh, 5.0, ctx);
        assertFalse(miss, "Landak tidak boleh mengenai target di luar jarak 4.0");
    }

    @Test
    public void testBadakKnockback() {
        Badak badak = new Badak();
        AttackContext ctx = new AttackContext(new ArrayList<>(), true);

        Ayam target = new Ayam();
        target.setPosition(5.0);
        badak.attack(target, 1.0, ctx);

        // Knockback memundurkan target 1 slot: untuk target musuh bertambah ke arah 20.0
        assertEquals(6.0, target.getPosition(), 0.001, "Target harus terdorong mundur 1 unit");
    }

    @Test
    public void testAnimalCooldownAndDefeated() {
        Ayam ayam = new Ayam();
        assertTrue(ayam.isReadyToAttack());

        ayam.consumeAttack();
        assertFalse(ayam.isReadyToAttack());

        ayam.advanceCooldown(GameConfig.MELEE_ATTACK_COOLDOWN_MS);
        assertTrue(ayam.isReadyToAttack());

        ayam.takeDamage(ayam.getHp());
        assertTrue(ayam.isDefeated());
    }

    @Test
    public void testAnimalConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Ayam().takeDamage(-5));
        assertThrows(IllegalArgumentException.class, () -> new Ayam().takeDamage(0));
    }

    @Test
    public void testAnimalMovedThisTickFlag() {
        Ayam ayam = new Ayam();
        assertFalse(ayam.hasMovedThisTick());
        ayam.setMovedThisTick(true);
        assertTrue(ayam.hasMovedThisTick());
        ayam.setMovedThisTick(false);
        assertFalse(ayam.hasMovedThisTick());
    }
}
