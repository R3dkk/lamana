package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Tombol kartu spawn untuk satu jenis hewan di HUD bawah.
 * Mengontrol enable/disable secara otomatis berdasarkan ketersediaan Gold, Level Base, dan GameState.
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
