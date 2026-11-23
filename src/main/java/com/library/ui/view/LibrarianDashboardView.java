package com.library.ui.view;

import com.library.constant.LibraryConstants;
import com.library.dto.BookRequest;
import com.library.dto.DashboardStatsResponse;
import com.library.dto.MemberRequest;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.ui.LibraryFxApp;
import com.library.ui.ViewNavigator;
import com.library.ui.dialog.BookDialog;
import com.library.ui.dialog.MemberDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Modern JavaFX Administration Dashboard for Librarians.
 */
public class LibrarianDashboardView {

    private final String adminUsername;
    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;

    // Stat Labels
    private final Label statTotalBooks = new Label("-");
    private final Label statAvailable = new Label("-");
    private final Label statIssued = new Label("-");
    private final Label statMembers = new Label("-");

    // Books Table & Lists
    private final ObservableList<Book> bookMasterList = FXCollections.observableArrayList();
    private FilteredList<Book> bookFilteredList;
    private TableView<Book> booksTable;
    private TextField bookSearchField;
    private ComboBox<String> categoryFilter;

    // Members Table & Lists
    private final ObservableList<Member> memberMasterList = FXCollections.observableArrayList();
    private FilteredList<Member> memberFilteredList;
    private TableView<Member> membersTable;
    private TextField memberSearchField;

    // Loans Table & Lists
    private final ObservableList<Loan> loanMasterList = FXCollections.observableArrayList();
    private FilteredList<Loan> loanFilteredList;
    private TableView<Loan> loansTable;
    private String currentLoanFilter = "ALL";

    // Issue controls
    private ComboBox<Member> issueMemberBox;
    private ComboBox<Book> issueBookBox;

    public LibrarianDashboardView(String adminUsername) {
        this.adminUsername = adminUsername;
        this.bookService = LibraryFxApp.getBean(BookService.class);
        this.memberService = LibraryFxApp.getBean(MemberService.class);
        this.loanService = LibraryFxApp.getBean(LoanService.class);
    }

    public Parent buildView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // Top Navigation Header
        root.setTop(buildHeader());

        // Center Content
        VBox centerContent = new VBox(16);
        centerContent.setPadding(new Insets(16, 20, 16, 20));

        // 1. Live Stats Bar
        centerContent.getChildren().add(buildStatsBar());

        // 2. TabPane for Management Modules
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab tabBooks = new Tab("📖 Books Management", buildBooksTab());
        Tab tabMembers = new Tab("👥 Member Directory", buildMembersTab());
        Tab tabLoans = new Tab("🔄 Issue & Return Loans", buildLoansTab());

        tabPane.getTabs().addAll(tabBooks, tabMembers, tabLoans);
        centerContent.getChildren().add(tabPane);

        root.setCenter(centerContent);

        // Load Initial Data
        refreshAllData();

        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label uni = new Label("Babasaheb Bhimrao Ambedkar University (BBAU Lucknow)");
        uni.getStyleClass().add("header-title-main");
        Label sub = new Label("Gautam Buddha Central Library - Librarian Administration Portal");
        sub.getStyleClass().add("header-title-sub");
        titleBox.getChildren().addAll(uni, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userBadge = new Label("🛡️ Administrator: " + adminUsername);
        userBadge.getStyleClass().add("header-user-badge");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-header-logout");
        logoutBtn.setOnAction(e -> {
            if (ViewNavigator.showConfirmation("Logout Confirmation", "Are you sure you want to log out of the admin portal?", "You will be returned to the login screen.")) {
                ViewNavigator.showLogin();
            }
        });

        header.getChildren().addAll(titleBox, spacer, userBadge, logoutBtn);
        return header;
    }

    private HBox buildStatsBar() {
        HBox statsBox = new HBox(14);
        statsBox.setAlignment(Pos.CENTER);

        statsBox.getChildren().addAll(
                createStatCard("TOTAL TITLES", statTotalBooks, "#2563eb"),
                createStatCard("AVAILABLE COPIES", statAvailable, "#059669"),
                createStatCard("CURRENTLY ISSUED", statIssued, "#d97706"),
                createStatCard("TOTAL MEMBERS", statMembers, "#7c3aed")
        );

        return statsBox;
    }

    private VBox createStatCard(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-weight: bold; -fx-font-size: 22px;");

        card.getChildren().addAll(titleLbl, valueLabel);
        return card;
    }

    // ==========================================================
    // TAB 1: BOOKS MANAGEMENT
    // ==========================================================
    private VBox buildBooksTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 4, 4, 4));

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        bookSearchField = new TextField();
        bookSearchField.setPromptText("🔍 Search books by title, author, isbn, or category...");
        bookSearchField.setPrefWidth(320);

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.setValue("All Categories");

        Button addBtn = new Button("➕ Add New Book");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> handleAddBook());

        Button editBtn = new Button("✏️ Edit Book");
        editBtn.getStyleClass().add("btn-outline");
        editBtn.setOnAction(e -> handleEditBook());

        Button deleteBtn = new Button("🗑️ Delete Book");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteBook());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> refreshBooks());

        toolbar.getChildren().addAll(bookSearchField, categoryFilter, addBtn, editBtn, deleteBtn, refreshBtn);

        // Books TableView
        booksTable = new TableView<>();
        VBox.setVgrow(booksTable, Priority.ALWAYS);

        TableColumn<Book, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(240);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(180);

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(130);

        TableColumn<Book, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(140);

        TableColumn<Book, Integer> totalCopiesCol = new TableColumn<>("Total");
        totalCopiesCol.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        totalCopiesCol.setPrefWidth(70);

        TableColumn<Book, Integer> availCopiesCol = new TableColumn<>("Available");
        availCopiesCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        availCopiesCol.setPrefWidth(80);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Book b = cellData.getValue();
            boolean isAvail = b.getAvailableCopies() != null && b.getAvailableCopies() > 0;
            return new SimpleStringProperty(isAvail ? "AVAILABLE" : "OUT OF STOCK");
        });
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Book, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("AVAILABLE".equals(item)) {
                        badge.getStyleClass().add("badge-available");
                    } else {
                        badge.getStyleClass().add("badge-out");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        booksTable.getColumns().addAll(idCol, titleCol, authorCol, isbnCol, catCol, totalCopiesCol, availCopiesCol, statusCol);

        // Filter setup
        bookFilteredList = new FilteredList<>(bookMasterList, p -> true);
        bookSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyBookFilter());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyBookFilter());
        booksTable.setItems(bookFilteredList);

        box.getChildren().addAll(toolbar, booksTable);
        return box;
    }

    private void applyBookFilter() {
        String search = bookSearchField.getText() == null ? "" : bookSearchField.getText().trim().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        bookFilteredList.setPredicate(book -> {
            if (book == null) return false;

            boolean matchesSearch = search.isEmpty()
                    || (book.getTitle() != null && book.getTitle().toLowerCase().contains(search))
                    || (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(search))
                    || (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(search))
                    || (book.getCategory() != null && book.getCategory().toLowerCase().contains(search));

            boolean matchesCategory = selectedCategory == null || "All Categories".equals(selectedCategory)
                    || (book.getCategory() != null && book.getCategory().equalsIgnoreCase(selectedCategory));

            return matchesSearch && matchesCategory;
        });
    }

    private void handleAddBook() {
        BookDialog dialog = new BookDialog(null);
        Optional<BookRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                bookService.addBook(req);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Added", "Success", "Book '" + req.getTitle() + "' added to catalog successfully.");
                refreshAllData();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Add Book Failed", "Error", ex.getMessage());
            }
        });
    }

    private void handleEditBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Click on a book in the table to edit it.");
            return;
        }

        BookDialog dialog = new BookDialog(selected);
        Optional<BookRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                bookService.updateBook(selected.getId(), req);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Updated", "Success", "Book details updated successfully.");
                refreshAllData();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Update Failed", "Error", ex.getMessage());
            }
        });
    }

    private void handleDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Click on a book in the table to delete it.");
            return;
        }

        if (ViewNavigator.showConfirmation("Delete Book", "Are you sure you want to delete this book?",
                "Title: " + selected.getTitle() + "\nISBN: " + selected.getIsbn() + "\nThis action cannot be undone.")) {
            try {
                bookService.deleteBook(selected.getId());
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Deleted", "Success", "Book deleted successfully.");
                refreshAllData();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Delete Failed", "Cannot delete book", ex.getMessage());
            }
        }
    }

    // ==========================================================
    // TAB 2: MEMBERS DIRECTORY
    // ==========================================================
    private VBox buildMembersTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 4, 4, 4));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        memberSearchField = new TextField();
        memberSearchField.setPromptText("🔍 Search members by name, email, or phone...");
        memberSearchField.setPrefWidth(320);

        Button registerBtn = new Button("➕ Register New Member");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setOnAction(e -> handleRegisterMember());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> refreshMembers());

        toolbar.getChildren().addAll(memberSearchField, registerBtn, refreshBtn);

        membersTable = new TableView<>();
        VBox.setVgrow(membersTable, Priority.ALWAYS);

        TableColumn<Member, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Member, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(220);

        TableColumn<Member, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);

        TableColumn<Member, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(140);

        TableColumn<Member, LocalDate> dateCol = new TableColumn<>("Joined Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("membershipDate"));
        dateCol.setPrefWidth(120);

        TableColumn<Member, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        membersTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, dateCol, statusCol);

        memberFilteredList = new FilteredList<>(memberMasterList, p -> true);
        memberSearchField.textProperty().addListener((obs, o, n) -> {
            String query = n == null ? "" : n.trim().toLowerCase();
            memberFilteredList.setPredicate(m -> {
                if (m == null) return false;
                return query.isEmpty()
                        || (m.getName() != null && m.getName().toLowerCase().contains(query))
                        || (m.getEmail() != null && m.getEmail().toLowerCase().contains(query))
                        || (m.getPhone() != null && m.getPhone().contains(query));
            });
        });
        membersTable.setItems(memberFilteredList);

        box.getChildren().addAll(toolbar, membersTable);
        return box;
    }

    private void handleRegisterMember() {
        MemberDialog dialog = new MemberDialog();
        Optional<MemberRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                memberService.addMember(req);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Member Registered", "Success", "Member '" + req.getName() + "' registered successfully.");
                refreshAllData();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Registration Failed", "Error", ex.getMessage());
            }
        });
    }

    // ==========================================================
    // TAB 3: ISSUE & RETURN LOANS COUNTER
    // ==========================================================
    private VBox buildLoansTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(14, 4, 4, 4));

        // Top Issue Card
        VBox issueCard = new VBox(10);
        issueCard.getStyleClass().add("card-panel");

        Label issueHeader = new Label("⚡ Quick Issue Counter");
        issueHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e3a8a;");

        HBox issueControls = new HBox(12);
        issueControls.setAlignment(Pos.CENTER_LEFT);

        issueMemberBox = new ComboBox<>();
        issueMemberBox.setPromptText("Select Member...");
        issueMemberBox.setPrefWidth(280);
        issueMemberBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Member m) {
                return m == null ? "" : m.getName() + " (" + m.getEmail() + ")";
            }
            @Override
            public Member fromString(String string) {
                return null;
            }
        });

        issueBookBox = new ComboBox<>();
        issueBookBox.setPromptText("Select Book (Available only)...");
        issueBookBox.setPrefWidth(320);
        issueBookBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book b) {
                return b == null ? "" : b.getTitle() + " [Avail: " + b.getAvailableCopies() + "]";
            }
            @Override
            public Book fromString(String string) {
                return null;
            }
        });

        Button issueBtn = new Button("Issue Book (14 Days)");
        issueBtn.getStyleClass().add("btn-primary");
        issueBtn.setOnAction(e -> handleIssueBook());

        issueControls.getChildren().addAll(
                new Label("Member:"), issueMemberBox,
                new Label("Book:"), issueBookBox,
                issueBtn
        );

        issueCard.getChildren().addAll(issueHeader, issueControls);

        // Loan Records Section
        VBox recordsCard = new VBox(10);
        VBox.setVgrow(recordsCard, Priority.ALWAYS);

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label recordsTitle = new Label("Circulation History & Active Loans");
        recordsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e3a8a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleGroup filterGroup = new ToggleGroup();
        RadioButton rbAll = new RadioButton("All Loans");
        rbAll.setToggleGroup(filterGroup);
        rbAll.setSelected(true);

        RadioButton rbActive = new RadioButton("Active (Issued)");
        rbActive.setToggleGroup(filterGroup);

        RadioButton rbReturned = new RadioButton("Returned");
        rbReturned.setToggleGroup(filterGroup);

        RadioButton rbOverdue = new RadioButton("Overdue");
        rbOverdue.setToggleGroup(filterGroup);

        filterGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (rbActive.isSelected()) currentLoanFilter = "ISSUED";
            else if (rbReturned.isSelected()) currentLoanFilter = "RETURNED";
            else if (rbOverdue.isSelected()) currentLoanFilter = "OVERDUE";
            else currentLoanFilter = "ALL";
            applyLoanFilter();
        });

        Button returnBtn = new Button("🔄 Return Selected Book");
        returnBtn.getStyleClass().add("btn-success");
        returnBtn.setOnAction(e -> handleReturnBook());

        filterBar.getChildren().addAll(recordsTitle, spacer, rbAll, rbActive, rbReturned, rbOverdue, returnBtn);

        // Loans Table
        loansTable = new TableView<>();
        VBox.setVgrow(loansTable, Priority.ALWAYS);

        TableColumn<Loan, Long> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<Loan, String> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getTitle() : "N/A"));
        bookCol.setPrefWidth(220);

        TableColumn<Loan, String> memberCol = new TableColumn<>("Borrower (Member)");
        memberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMember() != null ? c.getValue().getMember().getName() : "N/A"));
        memberCol.setPrefWidth(180);

        TableColumn<Loan, LocalDate> issueDateCol = new TableColumn<>("Issue Date");
        issueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        issueDateCol.setPrefWidth(100);

        TableColumn<Loan, LocalDate> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setPrefWidth(100);

        TableColumn<Loan, String> retDateCol = new TableColumn<>("Return Date");
        retDateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReturnDate() != null ? c.getValue().getReturnDate().toString() : "Not Returned"));
        retDateCol.setPrefWidth(110);

        TableColumn<Loan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("RETURNED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-returned");
                    } else if ("OVERDUE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-out");
                    } else {
                        badge.getStyleClass().add("badge-issued");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Loan, String> fineCol = new TableColumn<>("Overdue Status / Fine");
        fineCol.setCellValueFactory(c -> {
            Loan loan = c.getValue();
            if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
                return new SimpleStringProperty("Returned Clean");
            }
            if (loan.getDueDate() != null && LocalDate.now().isAfter(loan.getDueDate())) {
                long days = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                long fine = days * 5; // Rs 5 per day
                return new SimpleStringProperty("Late by " + days + "d (₹" + fine + ")");
            }
            long remaining = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
            return new SimpleStringProperty(remaining + " days left");
        });
        fineCol.setPrefWidth(160);

        loansTable.getColumns().addAll(idCol, bookCol, memberCol, issueDateCol, dueDateCol, retDateCol, statusCol, fineCol);

        loanFilteredList = new FilteredList<>(loanMasterList, p -> true);
        loansTable.setItems(loanFilteredList);

        recordsCard.getChildren().addAll(filterBar, loansTable);
        box.getChildren().addAll(issueCard, recordsCard);
        return box;
    }

    private void applyLoanFilter() {
        loanFilteredList.setPredicate(loan -> {
            if (loan == null) return false;
            if ("ALL".equals(currentLoanFilter)) return true;
            if ("OVERDUE".equals(currentLoanFilter)) {
                return "ISSUED".equalsIgnoreCase(loan.getStatus())
                        && loan.getDueDate() != null
                        && LocalDate.now().isAfter(loan.getDueDate());
            }
            return currentLoanFilter.equalsIgnoreCase(loan.getStatus());
        });
    }

    private void handleIssueBook() {
        Member member = issueMemberBox.getValue();
        Book book = issueBookBox.getValue();

        if (member == null || book == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select both a Member and a Book",
                    "Choose a member from the dropdown and an available book to issue.");
            return;
        }

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Out of Stock", "No copies available",
                    "All copies of '" + book.getTitle() + "' are currently issued.");
            return;
        }

        try {
            loanService.issueBook(book.getId(), member.getId());
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Issued", "Issue Successful",
                    "Book '" + book.getTitle() + "' has been issued to " + member.getName() + " for 14 days.");
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Issue Failed", "Could not issue book", ex.getMessage());
        }
    }

    private void handleReturnBook() {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a loan", "Click on an active loan in the table to return it.");
            return;
        }

        if ("RETURNED".equalsIgnoreCase(selected.getStatus())) {
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Already Returned", "Loan is already closed", "This book was already returned on " + selected.getReturnDate());
            return;
        }

        try {
            loanService.returnBook(selected.getId());

            String extraMsg = "";
            if (selected.getDueDate() != null && LocalDate.now().isAfter(selected.getDueDate())) {
                long lateDays = ChronoUnit.DAYS.between(selected.getDueDate(), LocalDate.now());
                long fine = lateDays * 5;
                extraMsg = "\n⚠️ Book was returned late by " + lateDays + " day(s). Fine due: ₹" + fine;
            }

            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Returned", "Return Processed Successfully",
                    "Book has been returned to catalog and available copies updated." + extraMsg);
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Return Failed", "Error processing return", ex.getMessage());
        }
    }

    // ==========================================================
    // REFRESH DATA
    // ==========================================================
    public void refreshAllData() {
        refreshStats();
        refreshBooks();
        refreshMembers();
        refreshLoans();
    }

    private void refreshStats() {
        try {
            DashboardStatsResponse stats = loanService.getDashboardStats();
            statTotalBooks.setText(String.valueOf(stats.getTotalBooks()));
            statAvailable.setText(String.valueOf(stats.getAvailableCopies()));
            statIssued.setText(String.valueOf(stats.getIssuedCopies()));
            statMembers.setText(String.valueOf(stats.getTotalMembers()));
        } catch (Exception ignored) {
        }
    }

    private void refreshBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookMasterList.setAll(books);

            // Populate category filter and issue dropdown
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            books.stream()
                    .map(Book::getCategory)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .forEach(categoryFilter.getItems()::add);
            categoryFilter.setValue("All Categories");

            if (issueBookBox != null) {
                issueBookBox.getItems().clear();
                books.stream()
                        .filter(b -> b.getAvailableCopies() != null && b.getAvailableCopies() > 0)
                        .forEach(issueBookBox.getItems()::add);
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshMembers() {
        try {
            List<Member> members = memberService.getAllMembers();
            memberMasterList.setAll(members);

            if (issueMemberBox != null) {
                issueMemberBox.getItems().clear();
                issueMemberBox.getItems().addAll(members);
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshLoans() {
        try {
            List<Loan> loans = loanService.getAllLoans();
            loanMasterList.setAll(loans);
            applyLoanFilter();
        } catch (Exception ignored) {
        }
    }
}
