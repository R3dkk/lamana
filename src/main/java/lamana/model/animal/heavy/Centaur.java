package lamana.model.animal.heavy;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.AttackContext;

/**
 * Centaur — hewan heavy level 3.
 * Special: Executioner Strike (+50% dmg jika HP target < 30%).
 * POLYMORPHISM: override attack() dengan finisher execute.
 */
public class Centaur extends HeavyAnimal implements SpecialAbility {

    public Centaur() {
        super("Centaur", 750, 55, 2, 2, 300, 90, 50, 3);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getEffectiveRange()) return false;

        int dmg = getDamage();
        // Execute: +50% damage jika target HP < 30% maxHp
        if (target.getHp() < (target.getMaxHp() * 0.30)) {
            dmg = dmg + dmg / 2;
        }
        target.takeDamage(dmg);
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Executioner Strike (+50% dmg <30% HP)";
    }
}
