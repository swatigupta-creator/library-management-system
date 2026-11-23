package com.library.ui;

import com.library.LibraryManagementApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;

/**
 * Main JavaFX Application bootstrap integrated with Spring Boot context.
 * Enables full Spring Data JPA, services, entities, and H2 database operations
 * inside a modern desktop JavaFX graphical interface.
 */
public class LibraryFxApp extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Start Spring Boot context in non-headless mode
        springContext = new SpringApplicationBuilder(LibraryManagementApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Babasaheb Bhimrao Ambedkar University - Gautam Buddha Central Library");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(1150);
        primaryStage.setHeight(750);

        try {
            InputStream iconStream = getClass().getResourceAsStream("/static/images/bbau-logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
        }

        ViewNavigator.init(primaryStage);
        ViewNavigator.showLogin();

        primaryStage.show();
    }

    @Override
    public void stop() {
        if (springContext != null && springContext.isActive()) {
            springContext.close();
        }
        Platform.exit();
    }

    /**
     * Retrieve any Spring-managed Bean (Service, Repository, etc.) from JavaFX UI views.
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (springContext == null) {
            throw new IllegalStateException("Spring application context is not yet initialized!");
        }
        return springContext.getBean(beanClass);
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}
