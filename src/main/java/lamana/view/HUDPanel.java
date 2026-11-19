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

/** Score strip for both bases plus player resources and upgrade progress. */
public class HUDPanel extends JPanel {
    private final BaseCard playerCard;
    private final BaseCard enemyCard;
    private final JLabel goldLabel;
    private final JLabel expLabel;
    private final JProgressBar expProgress;

    public HUDPanel() {
        super(new GridLayout(1, 3, 12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 18, 11, 18));
        setPreferredSize(new Dimension(1000, 90));

        playerCard = new BaseCard("PLAYER BASE", new Color(67, 189, 225));
        enemyCard = new BaseCard("ENEMY BASE", new Color(245, 109, 97));
        add(playerCard);

        JPanel economyCard = new JPanel(new BorderLayout(5, 7));
        economyCard.setBackground(new Color(20, 38, 51));
        economyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(49, 73, 85)),
                BorderFactory.createEmptyBorder(9, 14, 8, 14)));
        JLabel resourcesTitle = new JLabel("RESOURCES & PROGRESSION");
        resourcesTitle.setForeground(new Color(150, 177, 188));
        resourcesTitle.setFont(new Font("Dialog", Font.BOLD, 10));
        JPanel figures = new JPanel(new GridLayout(1, 2, 12, 0));
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
        expProgress.setPreferredSize(new Dimension(100, 7));
        JPanel resourceContent = new JPanel(new BorderLayout(0, 7));
        resourceContent.setOpaque(false);
        resourceContent.add(figures, BorderLayout.CENTER);
        resourceContent.add(expProgress, BorderLayout.SOUTH);
        economyCard.add(resourcesTitle, BorderLayout.NORTH);
        economyCard.add(resourceContent, BorderLayout.CENTER);
        add(economyCard);
        add(enemyCard);
    }

    private static void styleFigure(JLabel label, Color color) {
        label.setForeground(color);
        label.setFont(new Font("Dialog", Font.BOLD, 14));
    }

    public void refresh(GameController game) {
        if (game == null) return;
        Base player = game.getPlayerBase();
        Base enemy = game.getEnemyBase();
        playerCard.refresh(player);
        enemyCard.refresh(enemy);

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

    private static final class BaseCard extends JPanel {
        private final JLabel title;
        private final JLabel hpText;
        private final JProgressBar hpBar;
        private final Color accent;
        private final String faction;

        BaseCard(String titleText, Color accent) {
            super(new BorderLayout(0, 5));
            this.accent = accent;
            this.faction = titleText;
            setBackground(new Color(20, 38, 51));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(49, 73, 85)),
                    BorderFactory.createEmptyBorder(9, 14, 8, 14)));
            title = new JLabel(titleText + "  ·  ERA 1");
            title.setForeground(accent);
            title.setFont(new Font("Dialog", Font.BOLD, 11));
            hpText = new JLabel("1000 / 1000 HP");
            hpText.setForeground(new Color(231, 239, 238));
            hpText.setFont(new Font("Dialog", Font.BOLD, 14));
            hpBar = new JProgressBar(0, 1000);
            hpBar.setValue(1000);
            hpBar.setBorderPainted(false);
            hpBar.setBackground(new Color(39, 58, 67));
            hpBar.setForeground(accent);
            hpBar.setPreferredSize(new Dimension(100, 8));
            add(title, BorderLayout.NORTH);
            add(hpText, BorderLayout.CENTER);
            add(hpBar, BorderLayout.SOUTH);
        }

        void refresh(Base base) {
            title.setText(faction + "  ·  ERA " + base.getLevel());
            hpText.setText(String.format(Locale.ROOT, "%,d / %,d HP", base.getHp(), base.getMaxHp()));
            hpBar.setMaximum(base.getMaxHp());
            hpBar.setValue(base.getHp());
        }
    }
}
