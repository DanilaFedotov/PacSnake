package com.danilafedotov.pacman.ui;

import com.danilafedotov.pacman.Game;
import com.danilafedotov.pacman.UIThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * @author Danila Fedotov (19267371)
 * @version 01.04.2020
 */
public class GamePanel extends JPanel {

    private static boolean gameOver = false;

    final JButton startButton = new JButton() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            if (!Game.isInGame() && !Game.isGamePlayed()) {
                g.setColor(Color.WHITE);
                String welcomeString =
                        "Welcome to PacSnake:\n" +
                                "To move around use the W, A, S and D keys.\n" +
                                "Avoid the sad mushrooms,\n" +
                                "don't run into yourself and\n" +
                                "keep away from the border.\n" +
                                "Good luck and have fun\n\n" +
                                "Created by Danila Fedotov (19267371)\n\n" +
                                "Click anywhere to begin...";
                Font welcomeFont = new Font(null, Font.BOLD, 12);
                g.setFont(welcomeFont);
                int y = 10;
                for (String line : welcomeString.split("\n"))
                    g.drawString(line, getWidth() / 2 - g.getFontMetrics().stringWidth(line) / 2, y += g.getFontMetrics().getHeight());
            } else if (Game.isGamePlayed() && !Game.isInGame()) {
                Font stringFont = new Font(UIThread.getTerminusFont().getFontName(), Font.BOLD, 48);
                FontMetrics stringFontMetrics = getFontMetrics(stringFont);
                String gameOverString = "GAME OVER";
                int stringWidth = stringFontMetrics.stringWidth(gameOverString);
                int stringHeight = stringFontMetrics.getAscent();

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect((UIThread.getFrameWidth() / 2) - (stringWidth / 2) - 8, UIThread.getFrameHeight() / 2 - stringHeight, stringWidth + 16, stringHeight + stringFontMetrics.getDescent(), 8, 8);

                g2d.setColor(Color.RED);
                g2d.setFont(stringFont);
                g2d.drawString(gameOverString, (UIThread.getFrameWidth() / 2) - (stringWidth / 2), (UIThread.getFrameHeight() / 2));

                String helpString = "Click anywhere to start again\n" +
                        "or press 'Q' to exit";
                int y = UIThread.getFrameHeight() - 3*g2d.getFontMetrics().getHeight();
                for (String line : helpString.split("\n"))
                    g2d.drawString(line, getWidth() / 2 - g2d.getFontMetrics().stringWidth(line) / 2, y += g2d.getFontMetrics().getHeight());
            }
        }
    };

    public GamePanel() {
        super(new GridLayout(1, 1));
        initGamePanel();
        initButtons();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.YELLOW);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        paintFood(g2d, Game.getFoodPoints(), Game.getFoodRadius());
        paintPacSnakeTail(g2d, Game.getTailPoints(), Game.getTailWidth());
        paintPacSnakeHead(g2d, Game.getPacSnakePosX(), Game.getPacSnakePosY(), Game.getPacSnakeDir(), Game.getHeadRadius());
        paintMushrooms(g2d, Game.getMushroomPoints(), Game.getMushroomRadius());
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("Frames Per Second: %.1f", UIThread.getCurrentFps()), 10, 20);
        g2d.drawString(String.format("Game Tick (ms): %.2f/%d", Game.getCurrentTickMillis(), Game.getGameTick()), 10, 32);
        g2d.drawString(String.format("Your Score: %d", Game.getScore()), 10, UIThread.getFrameHeight() - 20);
        g2d.drawString(String.format("%s", Game.getCurrentDate().toString()), 10, UIThread.getFrameHeight() - 32);
        if (gameOver) {
            startButton.setVisible(true);
            startButton.setEnabled(true);
        }
    }

    private void paintPacSnakeHead(Graphics2D g, int x, int y, int dir, int radius) {

        int startAngle = 0;
        switch (dir) {
            case Game.EAST:
                startAngle = Game.getPacSnakeMouthState();
                break;

            case Game.NORTH:
                startAngle = 90 + Game.getPacSnakeMouthState();
                break;

            case Game.WEST:
                startAngle = 180 + Game.getPacSnakeMouthState();
                break;

            case Game.SOUTH:
                startAngle = 270 + Game.getPacSnakeMouthState();
                break;
        }
//        g.setColor(Color.MAGENTA);
//        g.fillOval(x-radius/2-2, y-radius/2-2, radius+4, radius+4);
        g.setColor(Color.WHITE);
        g.fillArc(x - radius, y - radius, radius * 2, radius * 2, startAngle, 360 - 2 * Game.getPacSnakeMouthState());


    }

    private void paintPacSnakeTail(Graphics2D g, ArrayList<int[]> tailPoints, int width) {
        try {
            for (int i = 0; i < tailPoints.size() - 4; i++) {
                g.setColor(new Color(128 + (int) (128 * Math.sin(i / 5.0)), 128 + (int) (128 * Math.sin(i / 6.0)), 128 + (int) (128 * Math.sin(i / 7.0))));
                g.fillOval(tailPoints.get(i)[0] - width / 2, tailPoints.get(i)[1] - width / 2, width, width);
                //paintRectangleBetweenPoints(g, tailPoints.get(i - 1)[0], tailPoints.get(i)[0], tailPoints.get(i - 1)[1], tailPoints.get(i)[1], width);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void paintFood(Graphics2D g, ArrayList<int[]> foodPoints, int radius) {
        try {
            g.setColor(Color.RED);
            for (int[] foodPoint : foodPoints) {
                g.fillOval(foodPoint[0] - radius, foodPoint[1] - radius, radius * 2, radius * 2);
            }
        } catch (ConcurrentModificationException e) { // List may  be modified inside the other thread
            //System.getLogger(Logger.GLOBAL_LOGGER_NAME).log(System.Logger.Level.ERROR, e);
        }
    }

    private void paintMushrooms(Graphics2D g, ArrayList<int[]> mushroomPoints, int radius) {
        for (int[] mushroomPoint : mushroomPoints) {
            g.drawImage(UIThread.getMushroomImage(), mushroomPoint[0] - radius, mushroomPoint[1] - radius, radius * 2, radius * 2, null);
        }
    }

//    private void paintRectangleBetweenPoints(Graphics2D g, int x1, int x2, int y1, int y2, int lineWidth) {
//        int pointX = Math.min(x1, x2) - lineWidth/2;
//        int pointY = Math.min(y1, y2) - lineWidth/2;
//        int width = Math.abs(x1 - x2) + lineWidth;
//        int height = Math.abs(y1 - y2) + lineWidth;
//        g.fillRect(pointX, pointY, width, height);
//    }

    private void initGamePanel() {
        setBackground(Color.BLACK);
        setBorder(null);
        setFocusable(false);
    }

    private void initButtons() {

        startButton.addActionListener(actionEvent -> {
            startButton.setOpaque(false);
            startButton.setContentAreaFilled(false);
            startButton.setBorderPainted(false);
            startButton.setVisible(false);
            startButton.setEnabled(false);
            startButton.setFocusable(false);
            if (!Game.isInGame()) {
                UIThread.initGame();
            }
        });


        startButton.setBackground(Color.BLACK);
        startButton.setBorder(null);
        add(startButton);
    }

    public void displayGameOver() {
        gameOver = true;
    }
}
