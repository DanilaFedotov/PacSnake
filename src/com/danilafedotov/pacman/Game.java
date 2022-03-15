package com.danilafedotov.pacman;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Danila Fedotov (19267371)
 * @version 01.04.2020
 */
public class Game {

    public static final int EAST = 0;
    public static final int NORTH = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;

    private static int pacSnakeDir = EAST;
    private static int pacSnakePosX = 15;
    private static int pacSnakePosY = 15;

    private static int pacSnakeMouthState = 0;
    private static int pacSnakeMouthStateModifier = 2;

    private static final ArrayList<int[]> tailPoints = new ArrayList<>();
    private static int tailLength = 50;

    private static final ArrayList<int[]> foodPoints = new ArrayList<>();

    private static final ArrayList<int[]> mushroomPoints = new ArrayList<>();

    private static int score = 0;

    private static Date currentDate;

    private static boolean inGame = false;
    private static boolean gamePlayed = false;

    private static final int GAME_TICK = 20; //Update game every x milliseconds or every 1 tick (1000/x)

    private static double currentTickMillis = 0;
    private static long currentTick = 0;

    private static final int[] mushroomDirChangeInterval = {40, 60, 70, 80};
    private static int[] mushroomDirs = {EAST, SOUTH, WEST, NORTH};

    private static long tickBeginNanos = 0;

    private static final int HEAD_RADIUS = 15;
    private static final int FOOD_RADIUS = 8;
    private static final int TAIL_WIDTH = 25;
    private static final int MUSHROOM_RADIUS = 20;

    private static final int PACSNAKE_SPEED = 3;
    private static final int MUSHROOM_SPEED = 2;


    private static ScheduledExecutorService gameScheduler = Executors.newSingleThreadScheduledExecutor();

    private static Runnable gameUpdater = () -> {
        Date date = new Date();
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        currentDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        if (isInGame()) {
            currentTickMillis = (System.nanoTime() - tickBeginNanos)/1000000.0;
            currentTick++;
            updateMushroomDir();
            moveMushrooms();
            if (checkTailCollision() || checkMushroomCollision()) {
                resetGame(false);
            }
            tailPoints.add(new int[]{getPacSnakePosX(), getPacSnakePosY()});
            switch (getPacSnakeDir()) {
                case EAST:
                    movePacSnake(getPacSnakeSpeed(), 0);
                    break;
                case NORTH:
                    movePacSnake(0, -getPacSnakeSpeed());
                    break;
                case WEST:
                    movePacSnake(-getPacSnakeSpeed(), 0);
                    break;
                case SOUTH:
                    movePacSnake(0, getPacSnakeSpeed());
                    break;
            }
            if (tailPoints.size() > tailLength) {
                tailPoints.remove(0);
            }
            tickBeginNanos = System.nanoTime();
        }
    };


    public static void main(String[] args) {
        UIThread.start();
        gameScheduler.scheduleAtFixedRate(gameUpdater, 0, GAME_TICK, TimeUnit.MILLISECONDS);
    }

    public static void setPacSnakeDir(int dir) {
        if (isInGame()) {
            pacSnakeDir = dir;
//            tailPoints.add(new int[]{getPacSnakePosX(), getPacSnakePosY()});
        }
    }

    public static int getPacSnakeDir() {
        return pacSnakeDir;
    }

    public static void setPacSnakePosX(int pacSnakePosX) {
        Game.pacSnakePosX = pacSnakePosX;
    }

    public static int getPacSnakePosX() {
        return pacSnakePosX;
    }

    public static void setPacSnakePosY(int pacSnakePosY) {
        Game.pacSnakePosY = pacSnakePosY;
    }

    public static int getPacSnakePosY() {
        return pacSnakePosY;
    }

    public static void movePacSnake(int x, int y) {

        if (isInGame() && !checkBorderCollisions(getPacSnakePosX() + x, getPacSnakePosY() + y, getHeadRadius())) {
            setPacSnakePosX(getPacSnakePosX() + x);
            setPacSnakePosY(getPacSnakePosY() + y);
            if (getFoodCollision(getFoodPoints(), getPacSnakePosX(), getPacSnakePosY()) > -1) {
                getFoodPoints().remove(getFoodCollision(getFoodPoints(), getPacSnakePosX(), getPacSnakePosY()));
                tailLength += 25;
                score++;
                generateFood(1, 20, 20, UIThread.getFrameWidth()-30, UIThread.getFrameHeight()-30);
            }
            updatePacSnakeMouth();
        } else if (checkBorderCollisions(getPacSnakePosX() + x, getPacSnakePosY() + y, getHeadRadius())) {
            resetGame(false);
        }

    }

    public static void updatePacSnakeMouth() {
        if (pacSnakeMouthState == 40) {
            pacSnakeMouthStateModifier = -2;
        } else if (pacSnakeMouthState == 0) {
            pacSnakeMouthStateModifier = 2;
        }
        pacSnakeMouthState = pacSnakeMouthState + pacSnakeMouthStateModifier;
    }

    public static int getPacSnakeMouthState() {
        return pacSnakeMouthState;
    }

    public static boolean isInGame() {
        return inGame;
    }

    public static void resetGame(boolean inGame) {
        UIThread.clearFpsArray();
        if (inGame && !isGamePlayed()) {
            gamePlayed = true;
            prepareGame();
            generateFood(1, 20, 20, UIThread.getFrameWidth()-30, UIThread.getFrameHeight()-30);
        } else if (inGame) {
            score = 0;
            tailLength = 50;
            mushroomPoints.clear();
            prepareGame();
            tailPoints.clear();
            foodPoints.clear();
            generateFood(1, 20, 20, UIThread.getFrameWidth()-30, UIThread.getFrameHeight()-30);
        } else {
            UIThread.displayGameOver();
        }
        Game.inGame = inGame;
    }
Q
    private static void prepareGame() {
        mushroomPoints.add(new int[]{getMushroomRadius(), UIThread.getFrameHeight()/2});
        mushroomPoints.add(new int[]{UIThread.getFrameWidth()/2, getMushroomRadius()});
        mushroomPoints.add(new int[]{UIThread.getFrameWidth() - getMushroomRadius(), UIThread.getFrameHeight()/2});
        mushroomPoints.add(new int[]{UIThread.getFrameWidth()/2, UIThread.getFrameHeight() - getMushroomRadius()});
        setPacSnakePosX(UIThread.getFrameWidth()/2);
        setPacSnakePosY(UIThread.getFrameHeight()/2);
    }

    public static boolean checkBorderCollisions(int x, int y, int objectBoundary) {
        return !((x <= UIThread.getFrameWidth() - objectBoundary && x >= objectBoundary) && (y <= UIThread.getFrameHeight() - objectBoundary && y >= objectBoundary));
    }

    private static boolean checkTailCollision() {
        for (int i = 0; i < tailPoints.size()-getTailWidth(); i++) {
            if (Math.hypot((tailPoints.get(i)[0] - getHeadRadius()) - (getPacSnakePosX() - getHeadRadius()), (tailPoints.get(i)[1] - getHeadRadius()) - (getPacSnakePosY() - getHeadRadius())) < getHeadRadius()+(getTailWidth()/2.0)) {
                return true;
            }
        }
        return false;
    }

    private static int getFoodCollision(ArrayList<int[]> foodPoints, int x, int y) {
        int size = foodPoints.size();
        for (int i = 0; i < size; i++) {
            if (Math.sqrt(Math.pow(foodPoints.get(i)[0] - x, 2) + Math.pow(foodPoints.get(i)[1] - y, 2)) < (getHeadRadius() + getFoodRadius())/2.0) {
                return i;
            }
        }
        return -1;
    }

    public static ArrayList<int[]> getTailPoints() {
        return tailPoints;
    }

    public static double getCurrentTickMillis() {
        return currentTickMillis;
    }

    public static int getGameTick() {
        return GAME_TICK;
    }

    public static void placeFood(int x, int y) {
        foodPoints.add(new int[]{x, y});
    }

    public static void generateFood(int amount, int minX, int minY, int maxX, int maxY) {
        Random rnd = new Random();
        for (int num = 0; num < amount; num++) {
            int x;
            int y;
            while (true) {
                x = minX + rnd.nextInt(maxX - minX + 1);
                y = minY + rnd.nextInt(maxY - minY + 1);
                boolean pointValid = getTailPoints().size() == 0;
                for (int i = 0; i < getTailPoints().size(); i++) {
                    if (Math.hypot((tailPoints.get(i)[0] - 15) - x, (tailPoints.get(i)[1] - 15) - y) > 20) {
                        pointValid = true;
                        break;
                    }
                }
                if (pointValid) {
                    break;
                }
            }
            placeFood(x, y);
        }
    }

    public static ArrayList<int[]> getFoodPoints() {
        return foodPoints;
    }

    public static int getScore() {
        return score;
    }

    public static Date getCurrentDate() {
        return currentDate;
    }

    public static ArrayList<int[]> getMushroomPoints() {
        return mushroomPoints;
    }

    public static void updateMushroomDir() {
        for (int i = 0; i < getMushroomPoints().size(); i++) {
            if (currentTick % (mushroomDirChangeInterval[i]) == 0) {
                setMushroomDirs(i, new Random().nextInt(4));
            }
        }
    }

    public static void setMushroomDirs(int mushroom, int dir) {
        mushroomDirs[mushroom] = dir;
    }

    public static boolean moveMushroom(int mushroom, int x, int y) {
        if (!checkBorderCollisions(getMushroomPoint(mushroom)[0] + x, getMushroomPoint(mushroom)[1] + y, getMushroomRadius())) {
            mushroomPoints.set(mushroom, new int[]{getMushroomPoint(mushroom)[0] + x, getMushroomPoint(mushroom)[1] + y});
            return true;
        } else {
            return false;
        }
    }

    public static void moveMushrooms() {
        for (int i = 0; i < mushroomPoints.size(); i++) {
            int xDir = 0;
            int yDir = 0;
            switch (mushroomDirs[i]) {
                case EAST:
                    xDir = 1;
                    break;
                case NORTH:
                    yDir = -1;
                    break;
                case WEST:
                    xDir = -1;
                    break;
                case SOUTH:
                    yDir = 1;
                    break;
            }
            if (!moveMushroom(i, xDir*getMushroomSpeed(), yDir*getMushroomSpeed())) {
                mushroomPoints.set(i, new int[]{getMushroomPoint(i)[0] - (xDir*(UIThread.getFrameWidth()-getMushroomRadius()*2-1)), getMushroomPoint(i)[1] - (yDir*(UIThread.getFrameHeight()-getMushroomRadius()*2-1))});
            }
        }
    }

    public static int[] getMushroomPoint(int mushroom) {
        return getMushroomPoints().get(mushroom);
    }

    public static boolean checkMushroomCollision() {
        for (int[] mushroomPoint : mushroomPoints) {
            if (Math.hypot(mushroomPoint[0] - getPacSnakePosX(), mushroomPoint[1] - getPacSnakePosY()) < (getMushroomRadius()+getHeadRadius())/1.5) {
                return true;
            }
        }
        return false;
    }

    public static int getHeadRadius() {
        return HEAD_RADIUS;
    }

    public static int getTailWidth() {
        return TAIL_WIDTH;
    }

    public static int getMushroomRadius() {
        return MUSHROOM_RADIUS;
    }

    public static int getFoodRadius() {
        return FOOD_RADIUS;
    }

    public static int getPacSnakeSpeed() {
        return PACSNAKE_SPEED;
    }

    public static int getMushroomSpeed() {
        return MUSHROOM_SPEED;
    }

    public static boolean isGamePlayed() {
        return gamePlayed;
    }
}
