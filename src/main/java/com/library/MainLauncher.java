package com.library;

import com.library.ui.LibraryFxApp;
import javafx.application.Application;

/**
 * Standalone entry point for launching the JavaFX Desktop GUI application.
 * Calling Application.launch from a class that doesn't extend Application
 * ensures compatibility across Java 11 through Java 26.
 */
public class MainLauncher {

    public static void main(String[] args) {
        Application.launch(LibraryFxApp.class, args);
    }
}
