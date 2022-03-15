package com.danilafedotov.pacman;

import com.danilafedotov.pacman.listeners.ControlKeyListener;
import com.danilafedotov.pacman.ui.GamePanel;
import com.danilafedotov.pacman.ui.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Danila Fedotov (19267371)
 * @version 01.04.2020
 */
public class UIThread {

    private static final MainFrame gameFrame = new MainFrame();
    private static final GamePanel gamePanel = new GamePanel();

    private static BufferedImage mushroomImage;
    private static Font terminusFont = getTtfResource("ttf/terminus.ttf");

    private static ScheduledExecutorService uiUpdater = Executors.newSingleThreadScheduledExecutor();
    private static int maxFps = 120;
    private static int repeatMillis = 1000000000 / maxFps;
    private static ArrayList<Double> currentFps = new ArrayList<>();
    private static long startNanos = 0;

    private static Runnable runnable = () -> {
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        try {
            mushroomImage = getImageResource("graphics/bad_mushroom.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(terminusFont);
        gameFrame.add(gamePanel);
        gameFrame.setUndecorated(true);
        gameFrame.setVisible(true);
    };

    public static void start() {
        Thread thread = new Thread(runnable, "UIThread");
        thread.start();
    }

    public static int getFrameWidth() {
        return (int) gameFrame.getSize().getWidth();
    }

    public static int getFrameHeight() {
        return (int) gameFrame.getSize().getHeight();
    }

    public static double getCurrentFps() {
        if (currentFps.size() == 10) {
            double sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += currentFps.get(i);
            }
            return sum/10;
        } else {
            return 0;
        }

    }

    public static void startGameUiScheduler() {
        uiUpdater.scheduleAtFixedRate(() -> {
            long endNanos = System.nanoTime();
            currentFps.add(Math.pow((endNanos - startNanos) / 1000000000.0, -1));
            if (currentFps.size() > 10) {
                currentFps.remove(0);
            }
            startNanos = System.nanoTime();
            gameFrame.repaint();
        }, 0, repeatMillis, TimeUnit.NANOSECONDS);
    }

    public static void displayGameOver() {
        gamePanel.displayGameOver();

    }

    public static void initGame() {
        gameFrame.requestFocus();
        Game.resetGame(true);
        if (Game.isGamePlayed()) {
            startGameUiScheduler();
        }
    }

    public static BufferedImage getMushroomImage() {
        return mushroomImage;
    }

    public static BufferedImage getImageResource(String imagePath) throws IOException {
        try (InputStream is = Game.class.getResourceAsStream(imagePath)) {
            if (is == null) {
                System.out.println();
                throw new FileNotFoundException("Resource not found: " + imagePath);
            }
            return ImageIO.read(is);
        }
    }

    public static Font getTtfResource(String ttfPath) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Game.class.getResourceAsStream(ttfPath));
        } catch (IOException | FontFormatException ignored) { }
        return null;
    }

    public static void clearFpsArray() {
        currentFps.clear();
    }

    public static Font getTerminusFont() {
        return terminusFont;
    }
}
