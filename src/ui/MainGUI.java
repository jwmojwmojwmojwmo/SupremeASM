package ui;

import main.CPU;

import javax.swing.*;

public class MainGUI {
    public static void main(String[] args) {
        JFrame frame = new GUIFrame("SupremeIDE v0.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}