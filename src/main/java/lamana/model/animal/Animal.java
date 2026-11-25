package lamana.model.animal;

import lamana.contract.Attackable;
import lamana.core.GameConfig;

/**
 * Base class abstrak untuk semua unit hewan di arena.
 *
 * OOP Pillars demonstrated:
 * - Abstraction: class abstract + method attack() abstract
 * - Encapsulation: semua field private, akses via getter terkontrol
 * - Inheritance: MeleeAnimal/RangedAnimal/HeavyAnimal extends Animal
 * - Polymorphism: setiap subclass override attack() dengan perilaku UNIK
 */
public abstract class Animal implements Attackable {
    private final String name;
    private final int maxHp;
    private int hp;
    private boolean movedThisTick = false;
    private final int damage;
    private final int range;
    private final int movementSpeed;
    private final int cost;
    private final int rewardMoney;
    private final int rewardExp;
    private double position;
    // Per-unit attack cooldown (Polymorphism proof: tidak ada global timer)
    private long attackCooldownMs;
    private long attackTimerMs;
    private long attackAnimationRemainingMs;
    // Efek status
    private long stunRemainingMs;

    protected Animal(
            String name,
            int hp,
            int damage,
            int range,
            int movementSpeed,
            int cost,
            int rewardMoney,
            int rewardExp,
            long attackCooldownMs) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama hewan tidak boleh kosong.");
        }
        if (hp <= 0 || damage <= 0 || range < 0 || movementSpeed <= 0
                || cost <= 0 || rewardMoney <= 0 || rewardExp <= 0) {
            throw new IllegalArgumentException("Statistik hewan tidak valid.");
        }
        if (attackCooldownMs <= 0) {
            throw new IllegalArgumentException("Attack cooldown harus > 0.");
        }
        this.name = name.trim();
        this.maxHp = hp;
        this.hp = hp;
        this.damage = damage;
        this.range = range;
        this.movementSpeed = movementSpeed;
        this.cost = cost;
        this.rewardMoney = rewardMoney;
        this.rewardExp = rewardExp;
        this.attackCooldownMs = attackCooldownMs;
        this.attackTimerMs = attackCooldownMs; // siap serang dari awal
        this.attackAnimationRemainingMs = 0;
        this.stunRemainingMs = 0;
    }

    // Getters (Encapsulation)
    public String getName() { return name; }
    @Override public int getHp() { return hp; }
    @Override public int getMaxHp() { return maxHp; }
    public int getDamage() { return damage; }
    public int getRange() { return range; }
    public int getEffectiveRange() { return this.range; }
    public int getMovementSpeed() { return movementSpeed; }
    public double getCollisionRadius() { return 1.45; }
    public int getCost() { return cost; }
    public int getRewardMoney() { return rewardMoney; }
    public int getRewardExp() { return rewardExp; }
    @Override public double getPosition() { return position; }
    public void setPosition(double position) { this.position = position; }

    public abstract int getRequiredBaseLevel();

    public boolean isUnlockedAtLevel(int baseLevel) {
        return baseLevel >= getRequiredBaseLevel();
    }

    // Encapsulation: takeDamage dengan validasi, bisa di-override (KuraKura)
    @Override
    public void takeDamage(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Damage harus > 0.");
        hp = Math.max(0, hp - amount);
    }

    @Override
    public boolean isDefeated() { return hp == 0; }

    // Stun management
    public boolean isStunned() { return stunRemainingMs > 0; }
    public void applyStun(long durationMs) {
        stunRemainingMs = Math.max(stunRemainingMs, durationMs);
    }
    public void advanceStun(long deltaMs) {
        stunRemainingMs = Math.max(0, stunRemainingMs - deltaMs);
    }

    // Per-unit cooldown management
    public void advanceCooldown(long deltaMs) {
        attackTimerMs = Math.min(attackCooldownMs, attackTimerMs + deltaMs);
    }
    public boolean isReadyToAttack() { return attackTimerMs >= attackCooldownMs; }
    public void consumeAttack() { attackTimerMs = 0; }
    public long getAttackCooldownMs() { return attackCooldownMs; }
    public boolean isAttackingAnimation() { return attackAnimationRemainingMs > 0; }
    public void triggerAttackAnimation(long durationMs) {
        attackAnimationRemainingMs = Math.max(attackAnimationRemainingMs, durationMs);
    }
    public void advanceAttackAnimation(long deltaMs) {
        attackAnimationRemainingMs = Math.max(0, attackAnimationRemainingMs - deltaMs);
    }

    /**
     * POLYMORPHISM RUNTIME — wajib override di setiap subclass konkret.
     * Setiap hewan memiliki perilaku attack() yang UNIK dan berbeda.
     * Dipanggil oleh CombatSystem via dynamic dispatch.
     *
     * @param target   entitas yang diserang (Animal ATAU Base — via Attackable)
     * @param distance jarak unit ke target
     * @param context  konteks jalur untuk efek AoE/knockback
     * @return true jika serangan berhasil
     */
    public abstract boolean attack(Attackable target, double distance, AttackContext context);

    public boolean hasMovedThisTick() { return movedThisTick; }
    public void setMovedThisTick(boolean moved) { this.movedThisTick = moved; }
}
