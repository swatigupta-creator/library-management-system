package com.library.ui.dialog;

import com.library.dto.BookRequest;
import com.library.entity.Book;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Modern modal dialog for Adding a New Book or Editing an existing Book.
 */
public class BookDialog extends Dialog<BookRequest> {

    private final TextField titleField = new TextField();
    private final TextField authorField = new TextField();
    private final TextField isbnField = new TextField();
    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final Spinner<Integer> copiesSpinner = new Spinner<>(1, 1000, 1);

    public BookDialog(Book existingBook) {
        boolean isEdit = existingBook != null;
        setTitle(isEdit ? "Edit Book Details" : "Add New Book to Catalog");
        setHeaderText(isEdit ? "Update details for: " + existingBook.getTitle() : "Fill in the details to add a new book to the library catalog.");

        // Setup Form Fields
        titleField.setPromptText("e.g. Effective Java");
        authorField.setPromptText("e.g. Joshua Bloch");
        isbnField.setPromptText("e.g. 978-0134685991");

        categoryBox.setEditable(true);
        categoryBox.getItems().addAll("Programming", "Software Engineering", "Computer Science", "Architecture", "Education", "General Fiction", "Mathematics", "Science");
        categoryBox.setValue("Programming");

        if (isEdit) {
            titleField.setText(existingBook.getTitle());
            authorField.setText(existingBook.getAuthor());
            isbnField.setText(existingBook.getIsbn());
            categoryBox.setValue(existingBook.getCategory());
            copiesSpinner.getValueFactory().setValue(existingBook.getTotalCopies());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(new Label("Book Title *:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Author *:"), 0, 1);
        grid.add(authorField, 1, 1);

        grid.add(new Label("ISBN *:"), 0, 2);
        grid.add(isbnField, 1, 2);

        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        grid.add(new Label("Total Copies *:"), 0, 4);
        grid.add(copiesSpinner, 1, 4);

        titleField.setPrefWidth(280);

        VBox content = new VBox(10, grid);
        getDialogPane().setContent(content);

        // Buttons
        ButtonType saveButtonType = new ButtonType(isEdit ? "Save Changes" : "Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveBtn = (Button) getDialogPane().lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("btn-primary");

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();
                String category = categoryBox.getValue() != null ? categoryBox.getValue().trim() : "General";
                int copies = copiesSpinner.getValue();

                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Title, Author, and ISBN are required fields.");
                    alert.showAndWait();
                    return null;
                }

                BookRequest req = new BookRequest();
                req.setTitle(title);
                req.setAuthor(author);
                req.setIsbn(isbn);
                req.setCategory(category);
                req.setTotalCopies(copies);
                return req;
            }
            return null;
        });
    }
}
