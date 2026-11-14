package lamana.model.economy;

/**
 * Mengelola ekonomi satu tim (Gold dan EXP).
 * OOP Encapsulation: semua field private, akses via method dengan validasi ketat.
 */
public class MoneyExp {
    private int money;
    private int exp;

    public MoneyExp() {
        this(0, 0);
    }

    public MoneyExp(int initialMoney, int initialExp) {
        if (initialMoney < 0 || initialExp < 0) {
            throw new IllegalArgumentException("Saldo awal tidak boleh negatif.");
        }
        this.money = initialMoney;
        this.exp = initialExp;
    }

    public int getMoney() {
        return money;
    }

    public int getExp() {
        return exp;
    }

    public void addMoney(int amount) {
        validateStrictlyPositive(amount);
        this.money += amount;
    }

    public boolean spendMoney(int amount) {
        validateStrictlyPositive(amount);
        if (this.money < amount) {
            return false;
        }
        this.money -= amount;
        return true;
    }

    public void gainExp(int amount) {
        validateStrictlyPositive(amount);
        this.exp += amount;
    }

    public boolean spendExp(int amount) {
        validateStrictlyPositive(amount);
        if (this.exp < amount) {
            return false;
        }
        this.exp -= amount;
        return true;
    }

    private void validateStrictlyPositive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih dari 0, diterima: " + value);
        }
    }
}
