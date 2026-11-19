package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Tombol untuk memasukkan satu jenis hewan ke antrean spawn.
 * Mengontrol enable/disable berdasarkan Gold, Level Base, kapasitas antrean, dan GameState.
 */
public class UnitCard extends JButton {

    private final String unitType;
    private final String displayName;
    private final int cost;

    public UnitCard(String unitType, String displayName, int cost) {
        super(String.format("%s ($%d)", displayName, cost));
        this.unitType = unitType;
        this.displayName = displayName;
        this.cost = cost;
        setFont(new Font("Arial", Font.BOLD, 11));
        setFocusPainted(false);
    }

    public String getUnitType() {
        return unitType;
    }

    public int getCost() {
        return cost;
    }

    public void refresh(GameController game) {
        if (game == null) {
            setEnabled(false);
            return;
        }
        boolean canSpawn = game.getState() == GameState.PLAYING
                && game.canSpawnPlayerAnimal(unitType);
        setEnabled(canSpawn);
    }
}
