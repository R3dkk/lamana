package lamana.view;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/** Small, flat icons used by the game controls and resource bar. */
final class UiIcon implements Icon {
    enum Type { GOLD, EXP, UPGRADE, PAUSE, PLAY, RESTART, DIFFICULTY }

    private final Type type;
    private final Color color;
    private final int level;
    private final int size;

    UiIcon(Type type, Color color) {
        this(type, color, 1, 18);
    }

    UiIcon(Type type, Color color, int level, int size) {
        this.type = type;
        this.color = color;
        this.level = level;
        this.size = size;
    }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int c = size / 2;
        switch (type) {
            case GOLD:
                g.fillOval(2, 2, size - 4, size - 4);
                g.setColor(new Color(110, 77, 24));
                g.drawOval(4, 4, size - 8, size - 8);
                g.drawLine(c, 5, c, size - 5);
                break;
            case EXP:
                Path2D star = new Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double angle = -Math.PI / 2 + i * Math.PI / 5;
                    double radius = i % 2 == 0 ? size * .46 : size * .21;
                    double px = c + Math.cos(angle) * radius;
                    double py = c + Math.sin(angle) * radius;
                    if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
                }
                star.closePath();
                g.fill(star);
                break;
            case UPGRADE:
                g.drawLine(c, size - 3, c, 5);
                g.drawLine(c, 5, 4, 12);
                g.drawLine(c, 5, size - 4, 12);
                g.drawLine(4, size - 4, size - 4, size - 4);
                break;
            case PAUSE:
                g.fillRoundRect(4, 3, 4, size - 6, 2, 2);
                g.fillRoundRect(size - 8, 3, 4, size - 6, 2, 2);
                break;
            case PLAY:
                Path2D triangle = new Path2D.Double();
                triangle.moveTo(5, 3);
                triangle.lineTo(size - 3, c);
                triangle.lineTo(5, size - 3);
                triangle.closePath();
                g.fill(triangle);
                break;
            case RESTART:
                g.drawArc(3, 3, size - 6, size - 6, 45, 285);
                g.drawLine(size - 4, 3, size - 4, 9);
                g.drawLine(size - 4, 3, size - 10, 3);
                break;
            case DIFFICULTY:
                int barWidth = Math.max(2, size / 6);
                for (int i = 0; i < 3; i++) {
                    int h = 5 + i * 4;
                    g.setColor(i < level ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 55));
                    g.fillRoundRect(2 + i * (barWidth + 3), size - 3 - h, barWidth, h, 2, 2);
                }
                break;
            default:
                break;
        }
        g.dispose();
    }
}
