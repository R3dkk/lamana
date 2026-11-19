package lamana.view;

import lamana.contract.SpecialAbility;
import lamana.model.animal.Animal;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Shared art helpers. PNGs in resources/assets override the built-in vector art. */
final class GameArt {
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();
    private static final Map<String, BufferedImage[]> FRAME_CACHE = new HashMap<>();
    private GameArt() {}

    static String assetKey(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    static String abilityFor(Animal animal) {
        return animal instanceof SpecialAbility
                ? ((SpecialAbility) animal).getAbilityName()
                : "Serangan standar";
    }

    static void drawAnimal(Graphics2D source, Animal animal, int centerX, int groundY,
                           int height, boolean playerSide, String action, int animationFrame) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String key = assetKey(animal.getName());
        String folder = key.equals("kurakura") ? "kura-kura" : key;
        BufferedImage image = spriteFrame("/assets/units/" + folder + "/" + action + ".png",
                animationFrame, 4);
        if (image == null) image = image("/assets/units/" + key + ".png");
        double aspect = image == null ? 1.35 : (double) image.getWidth() / image.getHeight();
        int spriteWidth = Math.max(height / 2, Math.min((int) (height * 1.75), (int) (height * aspect)));
        Color faction = playerSide ? new Color(92, 203, 233) : new Color(248, 127, 105);

        g.setColor(new Color(11, 25, 27, 95));
        g.fillOval(centerX - spriteWidth / 3, groundY - 5, spriteWidth * 2 / 3, 9);
        if (image != null) {
            int x = centerX - spriteWidth / 2;
            if (playerSide) g.drawImage(image, x, groundY - height, spriteWidth, height, null);
            else g.drawImage(image, x + spriteWidth, groundY - height, -spriteWidth, height, null);
        } else {
            drawFallbackCreature(g, centerX, groundY, height, playerSide, faction);
        }
        g.setColor(faction);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(centerX - spriteWidth / 4, groundY + 3, centerX + spriteWidth / 4, groundY + 3);
        g.dispose();
    }

    private static BufferedImage spriteFrame(String path, int frame, int frameCount) {
        BufferedImage sheet = image(path);
        if (sheet == null) return null;
        BufferedImage[] frames = FRAME_CACHE.get(path);
        if (frames == null) {
            frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int left = i * sheet.getWidth() / frameCount;
                int right = (i + 1) * sheet.getWidth() / frameCount;
                frames[i] = sheet.getSubimage(left, 0, right - left, sheet.getHeight());
            }
            FRAME_CACHE.put(path, frames);
        }
        return frames[Math.floorMod(frame, frames.length)];
    }

    static void drawCardSprite(Graphics2D source, Animal animal, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage image = image("/assets/units/" + assetKey(animal.getName()) + ".png");
        double aspect = image == null ? 1.35 : (double) image.getWidth() / image.getHeight();
        int spriteHeight = height;
        int spriteWidth = Math.min(width, (int) (spriteHeight * aspect));
        int drawX = x + (width - spriteWidth) / 2;
        g.setColor(new Color(10, 22, 28, 75));
        g.fillOval(x + width / 4, y + height - 5, width / 2, 7);
        if (image != null) {
            g.drawImage(image, drawX, y, spriteWidth, spriteHeight, null);
        } else {
            drawFallbackCreature(g, x + width / 2, y + height, height, true,
                    new Color(117, 190, 155));
        }
        g.dispose();
    }

    private static void drawFallbackCreature(Graphics2D g, int centerX, int groundY, int height,
                                              boolean facingRight, Color color) {
        int direction = facingRight ? 1 : -1;
        int bodyWidth = (int) (height * .64);
        int bodyHeight = (int) (height * .40);
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = groundY - (int) (height * .57);
        g.setColor(color);
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, height / 5, height / 5);
        g.fillOval(centerX + direction * bodyWidth / 4 - height / 7,
                groundY - (int) (height * .78), height / 3, height / 3);
        g.setStroke(new BasicStroke(Math.max(3, height / 13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int leg = 0; leg < 4; leg++) {
            int legX = bodyX + bodyWidth * (leg + 1) / 5;
            g.drawLine(legX, bodyY + bodyHeight - 3, legX, groundY - 3);
        }
        g.drawLine(centerX - direction * bodyWidth / 2, bodyY + bodyHeight / 2,
                centerX - direction * (int) (height * .48), bodyY + bodyHeight / 3);
        g.setColor(new Color(37, 49, 42));
        g.fillOval(centerX + direction * (int) (height * .25), groundY - (int) (height * .71),
                Math.max(3, height / 18), Math.max(3, height / 18));
    }

    static void drawBase(Graphics2D source, int centerX, int groundY, boolean playerSide, int size) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = size;
        int height = (int) (size * 1.28);
        int x = centerX - width / 2;
        int y = groundY - height;
        String assetName = playerSide ? "player-base" : "enemy-base";
        BufferedImage image = image("/assets/buildings/" + assetName + ".png");

        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            Color stone = playerSide ? new Color(72, 137, 167) : new Color(179, 91, 78);
            Color shadow = playerSide ? new Color(37, 86, 111) : new Color(119, 54, 49);
            g.setColor(new Color(7, 18, 27, 60));
            g.fillOval(x - 8, groundY - 8, width + 16, 22);

            // Main keep and side towers.
            g.setColor(shadow);
            g.fillRoundRect(x + 8, y + 34, width - 16, height - 34, 8, 8);
            g.fillRoundRect(x - 1, y + 18, width / 4, height - 18, 7, 7);
            g.fillRoundRect(x + width - width / 4 + 1, y + 18, width / 4, height - 18, 7, 7);
            g.setColor(stone);
            g.fillRoundRect(x + 12, y + 30, width - 24, height - 38, 7, 7);
            g.fillRoundRect(x + 3, y + 14, width / 4 - 4, height - 20, 6, 6);
            g.fillRoundRect(x + width - width / 4 + 1, y + 14, width / 4 - 4, height - 20, 6, 6);

            // Battlements and central gate.
            g.setColor(stone.brighter());
            int towerWidth = width / 4 - 4;
            for (int towerX : new int[]{x + 3, x + width - width / 4 + 1}) {
                for (int i = 0; i < 3; i++) {
                    g.fillRect(towerX + 3 + i * (towerWidth - 6) / 3, y + 7, 6, 12);
                }
            }
            g.fillRect(x + width / 2 - 5, groundY - 33, 10, 29);
            g.setColor(new Color(25, 38, 48));
            g.fillRoundRect(x + width / 2 - 5, groundY - 25, 10, 21, 5, 5);

            // Small flag gives the two teams a clear visual identity.
            int flagX = centerX + (playerSide ? -5 : 5);
            g.setColor(new Color(225, 232, 221));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(flagX, y + 8, flagX, y - 17);
            g.setColor(playerSide ? new Color(66, 195, 230) : new Color(252, 113, 92));
            int[] flagXs = playerSide
                    ? new int[]{flagX, flagX - 17, flagX}
                    : new int[]{flagX, flagX + 17, flagX};
            int[] flagYs = new int[]{y - 16, y - 10, y - 3};
            g.fillPolygon(flagXs, flagYs, 3);

            // Stone seams.
            g.setColor(new Color(235, 241, 226, 75));
            g.drawLine(x + 15, y + 51, x + width - 15, y + 51);
            g.drawLine(x + 15, y + 68, x + width - 15, y + 68);
        }
        g.dispose();
    }

    private static BufferedImage image(String resourcePath) {
        if (IMAGE_CACHE.containsKey(resourcePath)) return IMAGE_CACHE.get(resourcePath);
        BufferedImage loaded = null;
        try (InputStream stream = GameArt.class.getResourceAsStream(resourcePath)) {
            if (stream != null) loaded = ImageIO.read(stream);
        } catch (IOException ignored) {
            // Keep the vector fallback if an optional art file cannot be read.
        }
        IMAGE_CACHE.put(resourcePath, loaded);
        return loaded;
    }
}
