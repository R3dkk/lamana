package lamana.model.animal.ranged;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.AttackContext;
import lamana.model.animal.heavy.HeavyAnimal;

/**
 * Naga — hewan ranged level 3.
 * Special: Fireball (Ignore 50% armor / 1.5x damage vs target berarmor).
 * POLYMORPHISM: override attack() dengan bonus damage terhadap target HeavyAnimal/berarmor.
 */
public class Naga extends RangedAnimal implements SpecialAbility {

    public Naga() {
        super("Naga", 340, 65, 8, 2, 250, 70, 40, 3);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getEffectiveRange()) return false;

        int dmg = getDamage();
        // 1.5x damage jika target memiliki armor (HeavyAnimal)
        if (target instanceof HeavyAnimal) {
            dmg = (int) Math.round(dmg * 1.5);
        }
        target.takeDamage(dmg);
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Fireball (1.5x dmg vs Armor)";
    }
}
