package com.library.ui.dialog;

import com.library.dto.MemberRequest;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Modern modal dialog for Registering a New Library Member / Student.
 */
public class MemberDialog extends Dialog<MemberRequest> {

    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    public MemberDialog() {
        setTitle("Register New Library Member");
        setHeaderText("Register a student or faculty member into Gautam Buddha Central Library.");

        nameField.setPromptText("e.g. Swati Soni");
        emailField.setPromptText("e.g. swati@bbau.ac.in");
        phoneField.setPromptText("e.g. 9876543210");
        passwordField.setPromptText("e.g. student123");
        passwordField.setText("student123");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(new Label("Full Name *:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Email Address *:"), 0, 1);
        grid.add(emailField, 1, 1);

        grid.add(new Label("Phone Number *:"), 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(new Label("Login Password:"), 0, 3);
        grid.add(passwordField, 1, 3);

        nameField.setPrefWidth(280);

        VBox content = new VBox(10, grid);
        getDialogPane().setContent(content);

        ButtonType registerBtnType = new ButtonType("Register Member", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);

        Button regBtn = (Button) getDialogPane().lookupButton(registerBtnType);
        regBtn.getStyleClass().add("btn-primary");

        setResultConverter(dialogButton -> {
            if (dialogButton == registerBtnType) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim().toLowerCase();
                String phone = phoneField.getText().trim();
                String pass = passwordField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Name, Email, and Phone are required fields.");
                    alert.showAndWait();
                    return null;
                }

                MemberRequest req = new MemberRequest();
                req.setName(name);
                req.setEmail(email);
                req.setPhone(phone);
                return req;
            }
            return null;
        });
    }
}
