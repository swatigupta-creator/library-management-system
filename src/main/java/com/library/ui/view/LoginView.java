package com.library.ui.view;

import com.library.constant.LibraryConstants;
import com.library.entity.Member;
import com.library.repository.MemberRepository;
import com.library.ui.LibraryFxApp;
import com.library.ui.ViewNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Modern Login and Registration View for both Librarians and Students.
 */
public class LoginView {

    public Parent buildView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        // Top University Header
        VBox topHeader = new VBox(4);
        topHeader.getStyleClass().add("header-bar");
        topHeader.setAlignment(Pos.CENTER);
        topHeader.setPadding(new Insets(18, 24, 18, 24));

        Label uniTitle = new Label("Babasaheb Bhimrao Ambedkar University (A Central University), Lucknow");
        uniTitle.getStyleClass().add("header-title-main");
        Label libTitle = new Label("Gautam Buddha Central Library - Desktop Management Portal");
        libTitle.getStyleClass().add("header-title-sub");

        topHeader.getChildren().addAll(uniTitle, libTitle);
        root.setTop(topHeader);

        // Center Login Card
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(30, 20, 30, 20));

        VBox card = new VBox(15);
        card.getStyleClass().add("card-panel");
        card.setMaxWidth(480);
        card.setPadding(new Insets(24, 28, 24, 28));

        Label cardHeader = new Label("Welcome to Library Portal");
        cardHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label cardSub = new Label("Please sign in with your role to access your dashboard.");
        cardSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Librarian Login
        Tab librarianTab = new Tab("🛡️ Librarian Admin", buildLibrarianLoginForm());

        // Tab 2: Student Portal
        Tab studentTab = new Tab("🎓 Student Portal", buildStudentPortalForm());

        tabPane.getTabs().addAll(librarianTab, studentTab);

        card.getChildren().addAll(cardHeader, cardSub, tabPane);
        centerBox.getChildren().add(card);
        root.setCenter(centerBox);

        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 16, 12, 16));
        Label footerLabel = new Label("Gautam Buddha Central Library © 2026. Vidya Vihar, Raebareli Road, Lucknow - 226025.");
        footerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        footer.getChildren().add(footerLabel);
        root.setBottom(footer);

        return root;
    }

    private VBox buildLibrarianLoginForm() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18, 4, 8, 4));

        Label userLbl = new Label("Username:");
        userLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField usernameField = new TextField("librarian");

        Label passLbl = new Label("Password:");
        passLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("admin123");

        Button loginBtn = new Button("Login as Administrator");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(38);

        Label tipLabel = new Label("🔑 Default: username: librarian | password: admin123");
        tipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e40af; -fx-background-color: #dbeafe; -fx-padding: 8px 12px; -fx-background-radius: 6px;");

        loginBtn.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();

            if ("librarian".equals(u) && "admin123".equals(p)) {
                ViewNavigator.showLibrarianDashboard(u);
            } else {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Librarian Credentials",
                        "The username or password entered is incorrect. Default is librarian / admin123");
            }
        });

        box.getChildren().addAll(userLbl, usernameField, passLbl, passwordField, loginBtn, tipLabel);
        return box;
    }

    private VBox buildStudentPortalForm() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18, 4, 8, 4));

        // Sub-tabs or Toggle for Student Login vs Register
        ToggleGroup group = new ToggleGroup();
        RadioButton loginRadio = new RadioButton("Student Login");
        loginRadio.setToggleGroup(group);
        loginRadio.setSelected(true);
        loginRadio.setStyle("-fx-font-weight: bold;");

        RadioButton registerRadio = new RadioButton("New Student Registration");
        registerRadio.setToggleGroup(group);
        registerRadio.setStyle("-fx-font-weight: bold;");

        HBox toggleBox = new HBox(16, loginRadio, registerRadio);
        toggleBox.setAlignment(Pos.CENTER_LEFT);

        StackPane dynamicFormPane = new StackPane();

        // 1. Student Login Form
        VBox studentLoginBox = new VBox(12);
        Label emailLbl = new Label("Student Email / Roll ID:");
        emailLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField studentEmailField = new TextField("aarav.sharma@example.com");

        Label passLbl = new Label("Password:");
        passLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField studentPassField = new PasswordField();
        studentPassField.setText("student123");

        Button studentLoginBtn = new Button("Sign In to Student Portal");
        studentLoginBtn.getStyleClass().add("btn-primary");
        studentLoginBtn.setMaxWidth(Double.MAX_VALUE);
        studentLoginBtn.setPrefHeight(38);

        Label sampleTip = new Label("💡 Sample Student: aarav.sharma@example.com | pass: student123");
        sampleTip.setStyle("-fx-font-size: 11px; -fx-text-fill: #065f46; -fx-background-color: #d1fae5; -fx-padding: 8px 12px; -fx-background-radius: 6px;");

        studentLoginBtn.setOnAction(e -> {
            String email = studentEmailField.getText().trim().toLowerCase();
            String pass = studentPassField.getText();

            MemberRepository memberRepo = LibraryFxApp.getBean(MemberRepository.class);
            Optional<Member> memberOpt = memberRepo.findByEmailIgnoreCase(email);

            if (memberOpt.isEmpty()) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Login Failed", "Student Not Found",
                        "No student account found for email: " + email + ". Please register first.");
                return;
            }

            Member member = memberOpt.get();
            if (!member.getPassword().equals(pass)) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect Password",
                        "The password entered does not match our records.");
                return;
            }

            ViewNavigator.showStudentDashboard(member);
        });

        studentLoginBox.getChildren().addAll(emailLbl, studentEmailField, passLbl, studentPassField, studentLoginBtn, sampleTip);

        // 2. Student Register Form
        VBox studentRegisterBox = new VBox(10);
        TextField regNameField = new TextField();
        regNameField.setPromptText("Full Name (e.g. Swati Soni)");
        TextField regEmailField = new TextField();
        regEmailField.setPromptText("Email Address (e.g. swati@bbau.ac.in)");
        TextField regPhoneField = new TextField();
        regPhoneField.setPromptText("Phone Number (e.g. 9876543210)");
        PasswordField regPassField = new PasswordField();
        regPassField.setPromptText("Create Password");
        regPassField.setText("student123");

        Button createAccountBtn = new Button("Register & Enter Portal");
        createAccountBtn.getStyleClass().add("btn-success");
        createAccountBtn.setMaxWidth(Double.MAX_VALUE);
        createAccountBtn.setPrefHeight(38);

        createAccountBtn.setOnAction(e -> {
            String name = regNameField.getText().trim();
            String email = regEmailField.getText().trim().toLowerCase();
            String phone = regPhoneField.getText().trim();
            String pass = regPassField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Incomplete Form", "Required Fields Missing",
                        "Please fill in your name, email, phone, and password.");
                return;
            }

            MemberRepository memberRepo = LibraryFxApp.getBean(MemberRepository.class);
            if (memberRepo.existsByEmailIgnoreCase(email)) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Account Exists", "Email already registered",
                        "An account with email " + email + " already exists. Please login instead.");
                return;
            }

            Member newStudent = new Member(name, email, phone, LocalDate.now(), LibraryConstants.STATUS_ACTIVE, pass);
            Member saved = memberRepo.save(newStudent);

            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Welcome to BBAU Library!",
                    "Account created successfully for " + name + ". Entering Student Portal now.");
            ViewNavigator.showStudentDashboard(saved);
        });

        studentRegisterBox.getChildren().addAll(
                new Label("Full Name:"), regNameField,
                new Label("Email:"), regEmailField,
                new Label("Phone:"), regPhoneField,
                new Label("Password:"), regPassField,
                createAccountBtn
        );

        dynamicFormPane.getChildren().addAll(studentLoginBox, studentRegisterBox);
        studentRegisterBox.setVisible(false);

        loginRadio.setOnAction(e -> {
            studentLoginBox.setVisible(true);
            studentRegisterBox.setVisible(false);
        });
        registerRadio.setOnAction(e -> {
            studentLoginBox.setVisible(false);
            studentRegisterBox.setVisible(true);
        });

        box.getChildren().addAll(toggleBox, dynamicFormPane);
        return box;
    }
}
