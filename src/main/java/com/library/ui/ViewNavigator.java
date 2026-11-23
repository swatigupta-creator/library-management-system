package com.library.ui;

import com.library.entity.Member;
import com.library.ui.view.LibrarianDashboardView;
import com.library.ui.view.LoginView;
import com.library.ui.view.StudentDashboardView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Central View Navigator for switching between Login, Librarian Portal, and Student Portal.
 */
public class ViewNavigator {

    private static Stage primaryStage;
    private static final String CSS_PATH = "/css/javafx-style.css";

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLogin() {
        LoginView view = new LoginView();
        switchScene(view.buildView(), "BBAU Library - Authentication Portal");
    }

    public static void showLibrarianDashboard(String username) {
        LibrarianDashboardView view = new LibrarianDashboardView(username);
        switchScene(view.buildView(), "BBAU Gautam Buddha Central Library - Librarian Portal");
    }

    public static void showStudentDashboard(Member student) {
        StudentDashboardView view = new StudentDashboardView(student);
        switchScene(view.buildView(), "BBAU Gautam Buddha Central Library - Student Portal (" + student.getName() + ")");
    }

    private static void switchScene(Parent root, String title) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1150, 750);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
            applyStyles(scene);
        }
        primaryStage.setTitle(title);
    }

    public static void applyStyles(Scene scene) {
        scene.getStylesheets().clear();
        try {
            var cssUrl = ViewNavigator.class.getResource(CSS_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
