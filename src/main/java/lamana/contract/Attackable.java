package lamana.contract;

public interface Attackable {
    void takeDamage(int amount);
    boolean isDefeated();
    int getHp();
    int getMaxHp();
    double getPosition();
}
