package lamana;

import lamana.core.GameConfig;
import lamana.model.animal.melee.Ayam;
import lamana.model.animal.melee.Godzilla;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pengujian unit untuk markas (Base) dan Turret internal.
 * Membuktikan Abstraction (Upgradable & Attackable), Encapsulation, dan Komposisi.
 */
public class BaseTest {

    @Test
    public void testBaseInitialState() {
        Base base = new Base(0.0);
        assertEquals(GameConfig.BASE_HP, base.getHp());
        assertEquals(GameConfig.BASE_HP, base.getMaxHp());
        assertEquals(1, base.getLevel());
        assertEquals(0.0, base.getPosition());
        assertFalse(base.isDefeated());
        assertFalse(base.isDestroyed());

        assertNotNull(base.getTurret());
        assertEquals(1, base.getTurret().getLevel());
        assertEquals(GameConfig.TURRET_DMG_LV1, base.getTurret().getDamage());
    }

    @Test
    public void testBaseTakeDamageAndDestruction() {
        Base base = new Base(0.0);
        base.takeDamage(250);
        assertEquals(750, base.getHp());

        base.takeDamage(750);
        assertEquals(0, base.getHp());
        assertTrue(base.isDefeated());
        assertTrue(base.isDestroyed());
    }

    @Test
    public void testBaseUpgradeSyncTurret() {
        Base base = new Base(0.0);
        MoneyExp resources = new MoneyExp(0, 0);

        // Tidak cukup EXP
        assertFalse(base.canUpgrade(resources));
        assertFalse(base.upgrade(resources));

        // Berikan 100 EXP (kebutuhan Lv 1 -> 2)
        resources.gainExp(100);
        assertTrue(base.canUpgrade(resources));
        assertTrue(base.upgrade(resources));

        assertEquals(2, base.getLevel());
        assertEquals(0, resources.getExp()); // EXP terpotong
        assertEquals(2, base.getTurret().getLevel()); // Turret tersinkronisasi
        assertEquals(GameConfig.TURRET_DMG_LV2, base.getTurret().getDamage());

        // Upgrade ke Lv 3 butuh 200 EXP
        resources.gainExp(200);
        assertTrue(base.upgrade(resources));
        assertEquals(3, base.getLevel());
        assertEquals(3, base.getTurret().getLevel());
        assertEquals(GameConfig.TURRET_DMG_LV3, base.getTurret().getDamage());

        // Sudah maksimum level 3
        assertEquals(0, base.getExpNeededForNextLevel());
        assertFalse(base.canUpgrade(resources));
    }

    @Test
    public void testBaseSpawnAnimalValidation() {
        Base base = new Base(0.0); // Lv 1
        MoneyExp resources = new MoneyExp(100, 0);

        Ayam ayam = new Ayam(); // Cost 30, reqLevel 1
        assertTrue(base.canSpawn(ayam, resources));
        assertTrue(base.spawnAnimal(ayam, resources));
        assertEquals(70, resources.getMoney()); // 100 - 30 = 70

        // Hewan Era 3 (Godzilla, reqLevel 3) tidak boleh bisa di-spawn di Base Lv 1
        Godzilla hs = new Godzilla();
        resources.addMoney(500);
        assertFalse(base.canSpawn(hs, resources));
        assertFalse(base.spawnAnimal(hs, resources));
    }

    @Test
    public void testBasePositionBounds() {
        assertThrows(IllegalArgumentException.class, () -> new Base(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new Base(GameConfig.LANE_LENGTH + 1.0));
    }

    @Test
    public void testBaseSyncLevel() {
        Base base = new Base(0.0);
        assertEquals(1, base.getLevel());
        assertEquals(1, base.getTurret().getLevel());

        base.syncLevel(2);
        assertEquals(2, base.getLevel());
        assertEquals(2, base.getTurret().getLevel());

        base.syncLevel(3);
        assertEquals(3, base.getLevel());
        assertEquals(3, base.getTurret().getLevel());

        // Out of bounds check
        base.syncLevel(4);
        assertEquals(3, base.getLevel(), "Level tidak boleh melebihi max level");

        base.syncLevel(0);
        assertEquals(3, base.getLevel(), "Level tidak boleh di bawah 1");
    }
}
