package lamana;

import lamana.core.GameConfig;
import lamana.model.animal.heavy.KuraKura;
import lamana.model.animal.melee.Ayam;
import lamana.model.animal.ranged.Landak;
import lamana.model.battle.BattleLane;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pengujian unit untuk arena pertempuran BattleLane:
 * - Spawn unit di koordinat yang benar (0.0 untuk player, 20.0 untuk musuh)
 * - Pergerakan unit dan pencegahan crossing
 * - Pruning unit kalah dan penyerahan reward
 */
public class BattleLaneTest {

    @Test
    public void testAddUnitsAndInitialPositions() {
        Base playerBase = new Base(0.0);
        Base enemyBase = new Base(GameConfig.LANE_LENGTH);
        MoneyExp playerRes = new MoneyExp(100, 0);
        MoneyExp enemyRes = new MoneyExp(100, 0);

        BattleLane lane = new BattleLane(playerBase, enemyBase, playerRes, enemyRes);

        Ayam playerAyam = new Ayam();
        Landak enemyLandak = new Landak();

        lane.addPlayerAnimal(playerAyam);
        lane.addEnemyAnimal(enemyLandak);

        assertEquals(1, lane.getPlayerAnimals().size());
        assertEquals(1, lane.getEnemyAnimals().size());

        assertEquals(0.0, playerAyam.getPosition(), 0.001);
        assertEquals(GameConfig.LANE_LENGTH, enemyLandak.getPosition(), 0.001);

        assertSame(playerAyam, lane.getPlayerFront());
        assertSame(enemyLandak, lane.getEnemyFront());
    }

    @Test
    public void testMovementAndFrontNoCrossing() {
        Base playerBase = new Base(0.0);
        Base enemyBase = new Base(GameConfig.LANE_LENGTH);
        MoneyExp playerRes = new MoneyExp(100, 0);
        MoneyExp enemyRes = new MoneyExp(100, 0);

        BattleLane lane = new BattleLane(playerBase, enemyBase, playerRes, enemyRes);

        // Gunakan KuraKura (HP 200) agar tidak mati cepat saat bertemu
        KuraKura playerTank = new KuraKura(); // speed 2, HP 200
        KuraKura enemyTank  = new KuraKura(); // speed 2, HP 200

        lane.addPlayerAnimal(playerTank);
        lane.addEnemyAnimal(enemyTank);

        // Simulasi bergerak mendekati satu sama lain dan bertarung (8 detik)
        for (int i = 0; i < 8; i++) {
            lane.update(1000);
            if (!lane.getPlayerAnimals().isEmpty() && !lane.getEnemyAnimals().isEmpty()) {
                assertTrue(playerTank.getPosition() <= enemyTank.getPosition(),
                        "Unit terdepan player tidak boleh melewati unit musuh saat berhadapan");
            }
        }
    }

    @Test
    public void testRemoveDefeatedAndAwardRewards() {
        Base playerBase = new Base(0.0);
        Base enemyBase = new Base(GameConfig.LANE_LENGTH);
        MoneyExp playerRes = new MoneyExp(0, 0);
        MoneyExp enemyRes = new MoneyExp(0, 0);

        BattleLane lane = new BattleLane(playerBase, enemyBase, playerRes, enemyRes);

        Ayam enemyAyam = new Ayam(); // rewardMoney=15, rewardExp=10
        lane.addEnemyAnimal(enemyAyam);
        assertEquals(1, lane.getEnemyAnimals().size());

        // Musuh kalah
        enemyAyam.takeDamage(enemyAyam.getHp());
        assertTrue(enemyAyam.isDefeated());

        lane.removeDefeated();

        assertEquals(0, lane.getEnemyAnimals().size());
        assertEquals(15, playerRes.getMoney(), "Player harus mendapat reward money dari kill");
        assertEquals(10, playerRes.getExp(), "Player harus mendapat reward exp dari kill");
    }

    @Test
    public void testBothBasesAssigned() {
        Base playerBase = new Base(0.0);
        Base enemyBase = new Base(GameConfig.LANE_LENGTH);
        MoneyExp playerRes = new MoneyExp();
        MoneyExp enemyRes = new MoneyExp();

        BattleLane lane = new BattleLane(playerBase, enemyBase, playerRes, enemyRes);

        assertSame(playerBase, lane.getPlayerBase());
        assertSame(enemyBase, lane.getEnemyBase());
    }
}
