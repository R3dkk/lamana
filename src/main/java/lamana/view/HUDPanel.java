package lamana.view;

import lamana.controller.GameController;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

/** Slim top bar for player Gold, EXP, and upgrade progress. */
public class HUDPanel extends JPanel {
    private final JLabel goldLabel;
    private final JLabel expLabel;
    private final JProgressBar expProgress;

    public HUDPanel() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 18, 8, 18));
        setPreferredSize(new Dimension(1000, 56));

        JPanel economyCard = new JPanel(new BorderLayout(12, 5));
        economyCard.setBackground(new Color(20, 38, 51));
        economyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(49, 73, 85)),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));

        JPanel figures = new JPanel(new GridLayout(1, 2, 16, 0));
        figures.setOpaque(false);
        goldLabel = new JLabel("GOLD  120");
        expLabel = new JLabel("EXP  0 / 100");
        styleFigure(goldLabel, new Color(246, 198, 106));
        styleFigure(expLabel, new Color(149, 220, 188));
        figures.add(goldLabel);
        figures.add(expLabel);

        expProgress = new JProgressBar(0, 1000);
        expProgress.setBorderPainted(false);
        expProgress.setForeground(new Color(93, 190, 145));
        expProgress.setBackground(new Color(37, 58, 69));
        expProgress.setPreferredSize(new Dimension(100, 6));

        economyCard.add(figures, BorderLayout.CENTER);
        economyCard.add(expProgress, BorderLayout.SOUTH);
        add(economyCard, BorderLayout.CENTER);
    }

    private static void styleFigure(JLabel label, Color color) {
        label.setForeground(color);
        label.setFont(new Font("Dialog", Font.BOLD, 14));
    }

    public void refresh(GameController game) {
        if (game == null) return;
        Base player = game.getPlayerBase();
        MoneyExp resources = game.getPlayerResources();
        goldLabel.setText(String.format(Locale.ROOT, "GOLD  %d  (+%d/s)",
                resources.getMoney(), game.getPassiveIncomePerSecond()));
        int needed = player.getExpNeededForNextLevel();
        if (needed == 0) {
            expLabel.setText(String.format(Locale.ROOT, "ERA MAX  ·  %d EXP", resources.getExp()));
            expProgress.setValue(1000);
        } else {
            expLabel.setText(String.format(Locale.ROOT, "EXP  %d / %d", resources.getExp(), needed));
            expProgress.setValue((int) Math.min(1000, resources.getExp() * 1000L / needed));
        }
    }
}
