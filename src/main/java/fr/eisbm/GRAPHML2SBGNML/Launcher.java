package fr.eisbm.GRAPHML2SBGNML;

import javafx.application.Application;

/**
 * This class is needed to avoid having the main class extending Application.
 * This was causing some problem with maven packaging.
 * See: https://stackoverflow.com/a/38133937
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}