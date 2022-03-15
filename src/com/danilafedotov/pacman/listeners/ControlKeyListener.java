package com.danilafedotov.pacman.listeners;

import com.danilafedotov.pacman.Game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Danila Fedotov (19267371)
 * @version 01.04.2020
 */
public class ControlKeyListener extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (Game.isInGame()) {
            switch (keyEvent.getKeyChar()) {
                case ('d'):
                    if (Game.getPacSnakeDir() != Game.EAST && Game.getPacSnakeDir() != Game.WEST) {
                        Game.setPacSnakeDir(Game.EAST);
                    }
                    break;
                case ('w'):
                    if (Game.getPacSnakeDir() != Game.NORTH && Game.getPacSnakeDir() != Game.SOUTH) {
                        Game.setPacSnakeDir(Game.NORTH);
                    }
                    break;
                case ('a'):
                    if (Game.getPacSnakeDir() != Game.WEST && Game.getPacSnakeDir() != Game.EAST) {
                        Game.setPacSnakeDir(Game.WEST);
                    }
                    break;
                case ('s'):
                    if (Game.getPacSnakeDir() != Game.SOUTH && Game.getPacSnakeDir() != Game.NORTH) {
                        Game.setPacSnakeDir(Game.SOUTH);
                    }
                    break;
            }
        } else {
            if (keyEvent.getKeyChar() == 'q') {
                System.exit(0);
            }
        }
    }

}
