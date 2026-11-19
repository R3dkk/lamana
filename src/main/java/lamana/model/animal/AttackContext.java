package lamana.model.animal;

import java.util.List;

/**
 * Konteks arena yang dipass saat serangan untuk mendukung efek khusus:
 * AoE (Monyet/GajahSumatera), Knockback (Badak), Stun (BuayaMuara).
 * Memisahkan efek dari combat system tanpa coupling langsung.
 */
public class AttackContext {
    private final List<Animal> allEnemiesInLane;
    private final boolean attackerIsPlayer;

    public AttackContext(List<Animal> allEnemiesInLane, boolean attackerIsPlayer) {
        if (allEnemiesInLane == null) throw new IllegalArgumentException("List musuh tidak boleh null.");
        this.allEnemiesInLane = allEnemiesInLane;
        this.attackerIsPlayer = attackerIsPlayer;
    }

    public List<Animal> getAllEnemiesInLane() { return allEnemiesInLane; }
    public boolean isAttackerPlayer() { return attackerIsPlayer; }
}
