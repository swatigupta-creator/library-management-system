package com.library.ui.view;

import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.entity.Wishlist;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.WishlistService;
import com.library.ui.LibraryFxApp;
import com.library.ui.ViewNavigator;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modern JavaFX Student Portal for browsing, borrowing, wishlisting, and tracking loans.
 */
public class StudentDashboardView {

    private final Member student;
    private final BookService bookService;
    private final LoanService loanService;
    private final WishlistService wishlistService;

    // Catalog Table & Lists
    private final ObservableList<Book> catalogMasterList = FXCollections.observableArrayList();
    private FilteredList<Book> catalogFilteredList;
    private TableView<Book> catalogTable;
    private TextField catalogSearchField;
    private ComboBox<String> catalogCategoryFilter;
    private final Set<Long> wishlistedBookIds = new HashSet<>();

    // Wishlist Table & Lists
    private final ObservableList<Wishlist> wishlistMasterList = FXCollections.observableArrayList();
    private TableView<Wishlist> wishlistTable;

    // My Loans Table & Lists
    private final ObservableList<Loan> myLoansMasterList = FXCollections.observableArrayList();
    private TableView<Loan> myLoansTable;

    public StudentDashboardView(Member student) {
        this.student = student;
        this.bookService = LibraryFxApp.getBean(BookService.class);
        this.loanService = LibraryFxApp.getBean(LoanService.class);
        this.wishlistService = LibraryFxApp.getBean(WishlistService.class);
    }

    public Parent buildView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // Top Navigation Header
        root.setTop(buildHeader());

        // Center TabPane
        VBox centerContent = new VBox(14);
        centerContent.setPadding(new Insets(16, 20, 16, 20));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab tabCatalog = new Tab("📚 Browse Catalog & Borrow", buildCatalogTab());
        Tab tabWishlist = new Tab("🤍 My Wishlist", buildWishlistTab());
        Tab tabMyLoans = new Tab("📋 My Borrowed Books", buildMyLoansTab());

        tabPane.getTabs().addAll(tabCatalog, tabWishlist, tabMyLoans);
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
        Label uni = new Label("Babasaheb Bhimrao Ambedkar University, Lucknow");
        uni.getStyleClass().add("header-title-main");
        Label sub = new Label("Gautam Buddha Central Library - Student Digital Portal");
        sub.getStyleClass().add("header-title-sub");
        titleBox.getChildren().addAll(uni, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userBadge = new Label("🎓 Student: " + student.getName() + " (" + student.getEmail() + ")");
        userBadge.getStyleClass().add("header-user-badge");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-header-logout");
        logoutBtn.setOnAction(e -> {
            if (ViewNavigator.showConfirmation("Logout Confirmation", "Log out of Student Portal?", "You will return to the login screen.")) {
                ViewNavigator.showLogin();
            }
        });

        header.getChildren().addAll(titleBox, spacer, userBadge, logoutBtn);
        return header;
    }

    // ==========================================================
    // TAB 1: BROWSE CATALOG & BORROW
    // ==========================================================
    private VBox buildCatalogTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 4, 4, 4));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        catalogSearchField = new TextField();
        catalogSearchField.setPromptText("🔍 Search books by title, author, or category...");
        catalogSearchField.setPrefWidth(320);

        catalogCategoryFilter = new ComboBox<>();
        catalogCategoryFilter.getItems().add("All Categories");
        catalogCategoryFilter.setValue("All Categories");

        Button borrowBtn = new Button("📖 Borrow Selected Book");
        borrowBtn.getStyleClass().add("btn-primary");
        borrowBtn.setOnAction(e -> handleBorrowBookFromCatalog());

        Button wishlistToggleBtn = new Button("🤍 Toggle Wishlist");
        wishlistToggleBtn.getStyleClass().add("btn-outline");
        wishlistToggleBtn.setOnAction(e -> handleToggleWishlistFromCatalog());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> refreshAllData());

        toolbar.getChildren().addAll(catalogSearchField, catalogCategoryFilter, borrowBtn, wishlistToggleBtn, refreshBtn);

        // Catalog Table
        catalogTable = new TableView<>();
        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        TableColumn<Book, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(260);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(180);

        TableColumn<Book, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(140);

        TableColumn<Book, Integer> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        availCol.setPrefWidth(80);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> {
            Book b = c.getValue();
            boolean avail = b.getAvailableCopies() != null && b.getAvailableCopies() > 0;
            return new SimpleStringProperty(avail ? "AVAILABLE" : "OUT OF STOCK");
        });
        statusCol.setPrefWidth(120);
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

        TableColumn<Book, String> wishCol = new TableColumn<>("Wishlisted");
        wishCol.setCellValueFactory(c -> {
            boolean isW = wishlistedBookIds.contains(c.getValue().getId());
            return new SimpleStringProperty(isW ? "✓ Saved" : "-");
        });
        wishCol.setPrefWidth(90);

        catalogTable.getColumns().addAll(idCol, titleCol, authorCol, catCol, availCol, statusCol, wishCol);

        catalogFilteredList = new FilteredList<>(catalogMasterList, p -> true);
        catalogSearchField.textProperty().addListener((obs, o, n) -> applyCatalogFilter());
        catalogCategoryFilter.valueProperty().addListener((obs, o, n) -> applyCatalogFilter());
        catalogTable.setItems(catalogFilteredList);

        box.getChildren().addAll(toolbar, catalogTable);
        return box;
    }

    private void applyCatalogFilter() {
        String search = catalogSearchField.getText() == null ? "" : catalogSearchField.getText().trim().toLowerCase();
        String cat = catalogCategoryFilter.getValue();

        catalogFilteredList.setPredicate(b -> {
            if (b == null) return false;
            boolean matchesSearch = search.isEmpty()
                    || (b.getTitle() != null && b.getTitle().toLowerCase().contains(search))
                    || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(search))
                    || (b.getCategory() != null && b.getCategory().toLowerCase().contains(search));

            boolean matchesCat = cat == null || "All Categories".equals(cat)
                    || (b.getCategory() != null && b.getCategory().equalsIgnoreCase(cat));

            return matchesSearch && matchesCat;
        });
    }

    private void handleBorrowBookFromCatalog() {
        Book selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Click on a book from the table to borrow it.");
            return;
        }

        if (selected.getAvailableCopies() == null || selected.getAvailableCopies() <= 0) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Out of Stock", "No copies available",
                    "All copies of '" + selected.getTitle() + "' are currently issued. You can add it to your Wishlist to borrow when returned.");
            return;
        }

        if (ViewNavigator.showConfirmation("Confirm Borrowing", "Borrow '" + selected.getTitle() + "'?",
                "Duration: 14 Days (Due date: " + LocalDate.now().plusDays(14) + ").\nDo you want to proceed?")) {
            try {
                loanService.issueBook(selected.getId(), student.getId());
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Borrow Successful", "Book Issued!",
                        "You have successfully borrowed '" + selected.getTitle() + "'. Please return it on or before " + LocalDate.now().plusDays(14) + ".");
                refreshAllData();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Borrow Failed", "Could not borrow book", ex.getMessage());
            }
        }
    }

    private void handleToggleWishlistFromCatalog() {
        Book selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Click on a book to add or remove it from your wishlist.");
            return;
        }

        try {
            boolean isNowWishlisted = wishlistService.toggleWishlist(selected.getId(), student.getId());
            String action = isNowWishlisted ? "added to" : "removed from";
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Wishlist Updated", "Success",
                    "'" + selected.getTitle() + "' was " + action + " your personal wishlist.");
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Wishlist Error", "Could not update wishlist", ex.getMessage());
        }
    }

    // ==========================================================
    // TAB 2: MY WISHLIST
    // ==========================================================
    private VBox buildWishlistTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 4, 4, 4));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label headerLbl = new Label("🤍 Your Saved Wishlist Books");
        headerLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e3a8a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button borrowWishBtn = new Button("📖 Borrow Now");
        borrowWishBtn.getStyleClass().add("btn-primary");
        borrowWishBtn.setOnAction(e -> handleBorrowFromWishlist());

        Button removeWishBtn = new Button("🗑️ Remove from Wishlist");
        removeWishBtn.getStyleClass().add("btn-danger");
        removeWishBtn.setOnAction(e -> handleRemoveFromWishlist());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> refreshWishlist());

        toolbar.getChildren().addAll(headerLbl, spacer, borrowWishBtn, removeWishBtn, refreshBtn);

        wishlistTable = new TableView<>();
        VBox.setVgrow(wishlistTable, Priority.ALWAYS);

        TableColumn<Wishlist, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getTitle() : ""));
        titleCol.setPrefWidth(260);

        TableColumn<Wishlist, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getAuthor() : ""));
        authorCol.setPrefWidth(180);

        TableColumn<Wishlist, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getCategory() : ""));
        catCol.setPrefWidth(140);

        TableColumn<Wishlist, String> availCol = new TableColumn<>("Copies Available");
        availCol.setCellValueFactory(c -> {
            Book b = c.getValue().getBook();
            int copies = b != null && b.getAvailableCopies() != null ? b.getAvailableCopies() : 0;
            return new SimpleStringProperty(String.valueOf(copies));
        });
        availCol.setPrefWidth(110);

        TableColumn<Wishlist, String> statusCol = new TableColumn<>("Stock Status");
        statusCol.setCellValueFactory(c -> {
            Book b = c.getValue().getBook();
            boolean avail = b != null && b.getAvailableCopies() != null && b.getAvailableCopies() > 0;
            return new SimpleStringProperty(avail ? "AVAILABLE" : "OUT OF STOCK");
        });
        statusCol.setPrefWidth(120);

        wishlistTable.getColumns().addAll(titleCol, authorCol, catCol, availCol, statusCol);
        wishlistTable.setItems(wishlistMasterList);

        box.getChildren().addAll(toolbar, wishlistTable);
        return box;
    }

    private void handleBorrowFromWishlist() {
        Wishlist selected = wishlistTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getBook() == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Choose a book from your wishlist to borrow.");
            return;
        }

        Book book = selected.getBook();
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Out of Stock", "No copies available", "All copies are currently issued. Please check back later.");
            return;
        }

        try {
            loanService.issueBook(book.getId(), student.getId());
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Borrow Successful", "Book Borrowed",
                    "'" + book.getTitle() + "' borrowed successfully for 14 days.");
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Borrow Failed", "Error", ex.getMessage());
        }
    }

    private void handleRemoveFromWishlist() {
        Wishlist selected = wishlistTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getBook() == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book", "Choose a book from your wishlist to remove.");
            return;
        }

        try {
            wishlistService.removeFromWishlist(selected.getBook().getId(), student.getId());
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Wishlist Updated", "Removed", "Book removed from your wishlist.");
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not remove from wishlist", ex.getMessage());
        }
    }

    // ==========================================================
    // TAB 3: MY BORROWED BOOKS
    // ==========================================================
    private VBox buildMyLoansTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 4, 4, 4));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label headerLbl = new Label("📖 Your Borrowing Records & Active Loans");
        headerLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e3a8a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button returnBtn = new Button("🔄 Return Selected Book");
        returnBtn.getStyleClass().add("btn-success");
        returnBtn.setOnAction(e -> handleReturnMyBook());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> refreshMyLoans());

        toolbar.getChildren().addAll(headerLbl, spacer, returnBtn, refreshBtn);

        myLoansTable = new TableView<>();
        VBox.setVgrow(myLoansTable, Priority.ALWAYS);

        TableColumn<Loan, Long> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<Loan, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getTitle() : ""));
        titleCol.setPrefWidth(240);

        TableColumn<Loan, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook() != null ? c.getValue().getBook().getAuthor() : ""));
        authorCol.setPrefWidth(180);

        TableColumn<Loan, LocalDate> issueCol = new TableColumn<>("Issue Date");
        issueCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        issueCol.setPrefWidth(100);

        TableColumn<Loan, LocalDate> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueCol.setPrefWidth(100);

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

        TableColumn<Loan, String> fineCol = new TableColumn<>("Days Remaining / Fine");
        fineCol.setCellValueFactory(c -> {
            Loan loan = c.getValue();
            if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
                return new SimpleStringProperty("Returned");
            }
            if (loan.getDueDate() != null && LocalDate.now().isAfter(loan.getDueDate())) {
                long late = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                long fine = late * 5;
                return new SimpleStringProperty("⚠️ Overdue by " + late + "d (₹" + fine + ")");
            }
            long remaining = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
            return new SimpleStringProperty(remaining + " days left");
        });
        fineCol.setPrefWidth(180);

        myLoansTable.getColumns().addAll(idCol, titleCol, authorCol, issueCol, dueCol, statusCol, fineCol);
        myLoansTable.setItems(myLoansMasterList);

        box.getChildren().addAll(toolbar, myLoansTable);
        return box;
    }

    private void handleReturnMyBook() {
        Loan selected = myLoansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a loan", "Click on an active loan to return the book.");
            return;
        }

        if ("RETURNED".equalsIgnoreCase(selected.getStatus())) {
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Already Returned", "Loan is closed", "This book was already returned.");
            return;
        }

        try {
            loanService.returnBook(selected.getId());
            ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Book Returned", "Return Successful",
                    "You have successfully returned the book to the library.");
            refreshAllData();
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Return Failed", "Error", ex.getMessage());
        }
    }

    // ==========================================================
    // REFRESH DATA
    // ==========================================================
    public void refreshAllData() {
        refreshCatalog();
        refreshWishlist();
        refreshMyLoans();
    }

    private void refreshCatalog() {
        try {
            List<Book> books = bookService.getAllBooks();
            catalogMasterList.setAll(books);

            catalogCategoryFilter.getItems().clear();
            catalogCategoryFilter.getItems().add("All Categories");
            books.stream()
                    .map(Book::getCategory)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .forEach(catalogCategoryFilter.getItems()::add);
            catalogCategoryFilter.setValue("All Categories");

            List<Long> wIds = wishlistService.getWishlistBookIds(student.getId());
            wishlistedBookIds.clear();
            wishlistedBookIds.addAll(wIds);

            catalogTable.refresh();
        } catch (Exception ignored) {
        }
    }

    private void refreshWishlist() {
        try {
            List<Wishlist> list = wishlistService.getMemberWishlist(student.getId());
            wishlistMasterList.setAll(list);
        } catch (Exception ignored) {
        }
    }

    private void refreshMyLoans() {
        try {
            List<Loan> loans = loanService.getLoansByMemberId(student.getId());
            myLoansMasterList.setAll(loans);
        } catch (Exception ignored) {
        }
    }
}
