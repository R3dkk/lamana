package lamana.view;

import lamana.controller.GameController;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Locale;

/** Compact resource row: icons, values, and a single progress strip. */
public class HUDPanel extends JPanel {
    private static final Color GOLD = new Color(232, 187, 91);
    private static final Color EXP = new Color(116, 204, 160);

    private final JLabel goldLabel;
    private final JLabel expLabel;
    private final JProgressBar expProgress;

    public HUDPanel() {
        super(new BorderLayout(14, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 4, 14));
        setPreferredSize(new Dimension(1000, 42));

        JPanel values = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        values.setOpaque(false);
        goldLabel = makeValueLabel("120", UiIcon.Type.GOLD, GOLD);
        expLabel = makeValueLabel("0", UiIcon.Type.EXP, EXP);
        values.add(goldLabel);
        values.add(expLabel);
        add(values, BorderLayout.CENTER);

        expProgress = new JProgressBar(0, 1000);
        expProgress.setBorderPainted(false);
        expProgress.setStringPainted(false);
        expProgress.setForeground(EXP);
        expProgress.setBackground(new Color(70, 86, 85));
        expProgress.setPreferredSize(new Dimension(170, 5));
        expProgress.setToolTipText("Progres EXP ke upgrade markas berikutnya");
        add(expProgress, BorderLayout.EAST);
    }

    private static JLabel makeValueLabel(String value, UiIcon.Type icon, Color color) {
        JLabel label = new JLabel(value, new UiIcon(icon, color), SwingConstants.LEFT);
        label.setIconTextGap(6);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Dialog", Font.BOLD, 13));
        return label;
    }

    public void refresh(GameController game) {
        if (game == null) return;
        Base player = game.getPlayerBase();
        MoneyExp resources = game.getPlayerResources();
        goldLabel.setText(String.format(Locale.ROOT, "%d", resources.getMoney()));
        goldLabel.setToolTipText(String.format(Locale.ROOT, "Emas · +%d/detik", game.getPassiveIncomePerSecond()));

        int needed = player.getExpNeededForNextLevel();
        expLabel.setText(String.format(Locale.ROOT, "%d", resources.getExp()));
        if (needed == 0) {
            expProgress.setValue(1000);
            expLabel.setToolTipText("EXP · Era maksimum");
        } else {
            expProgress.setValue((int) Math.min(1000, resources.getExp() * 1000L / needed));
            expLabel.setToolTipText(String.format(Locale.ROOT, "EXP · %d dari %d", resources.getExp(), needed));
        }
    }
}
