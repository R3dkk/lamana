package lamana.view;

import lamana.controller.GameController;
import lamana.model.economy.MoneyExp;

import javax.swing.*;
import java.awt.*;

/**
 * HUD Panel untuk menampilkan status ekonomi pemain: Gold dan EXP.
 * Layout: GridLayout(1, 2) dengan latar belakang gelap.
 */
public class HUDPanel extends JPanel {
    private final JLabel goldLabel;
    private final JLabel expLabel;

    public HUDPanel() {
        setLayout(new GridLayout(1, 2, 8, 2));
        setBackground(new Color(30, 30, 45));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        goldLabel = makeLabel("Gold: 0 (+0/s)");
        expLabel = makeLabel("EXP: 0");

        add(goldLabel);
        add(expLabel);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 14));
        return lbl;
    }

    public void refresh(GameController game) {
        if (game == null) return;
        MoneyExp pr = game.getPlayerResources();
        goldLabel.setText(String.format("Gold: %d (+%d/s)", pr.getMoney(), game.getPassiveIncomePerSecond()));
        expLabel.setText(String.format("EXP: %d", pr.getExp()));
    }
}
