package lamana.contract;

import lamana.model.economy.MoneyExp;

public interface Upgradable {
    boolean upgrade(MoneyExp resources);
    boolean canUpgrade(MoneyExp resources);
    int getLevel();
}
