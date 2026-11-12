package lamana;

public class MoneyExp {
    private int money;
    private int exp;

    public MoneyExp() {
        this.money = 0;
        this.exp = 0;
    }

    public int getMoney() {
        return money;
    }

    public int getExp() {
        return exp;
    }

    public void addMoney(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah money harus lebih dari 0.");
        }
        money += amount;
    }

    public boolean spendMoney(int amount) {
        validatePositiveAmount(amount);
        if (money < amount) {
            return false;
        }
        money -= amount;
        return true;
    }

    public void gainExp(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah EXP harus lebih dari 0.");
        }
        exp += amount;
    }

    public boolean spendExp(int amount) {
        validatePositiveAmount(amount);
        if (exp < amount) {
            return false;
        }
        exp -= amount;
        return true;
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih dari 0.");
        }
    }
}
