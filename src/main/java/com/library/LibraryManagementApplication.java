package com.library;

import com.library.ui.LibraryFxApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.GraphicsEnvironment;

@SpringBootApplication
public class LibraryManagementApplication {

    public static void main(String[] args) {
        if (!GraphicsEnvironment.isHeadless() && !"true".equalsIgnoreCase(System.getProperty("app.headless"))) {
            // Launch JavaFX Desktop GUI Application
            Application.launch(LibraryFxApp.class, args);
        } else {
            // Headless fallback
            SpringApplication.run(LibraryManagementApplication.class, args);
        }
    }
}
