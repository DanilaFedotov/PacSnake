package com.danilafedotov.pacman.ui;

import com.danilafedotov.pacman.listeners.ControlKeyListener;

import javax.swing.*;
import java.awt.*;

/**
 * @author Danila Fedotov (19267371)
 * @version 01.04.2020
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super();
        addKeyListener(new ControlKeyListener());
        setLayout(new GridLayout(1, 1));
        setMinimumSize(new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/3, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2));
    }

}
