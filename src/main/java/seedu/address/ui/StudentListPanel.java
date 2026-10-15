package seedu.address.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;

/** Displays students and supports stable-ID selection. */
public class StudentListPanel extends UiPart<Region> {
    private static final String FXML = "StudentListPanel.fxml";

    @FXML
    private ListView<Student> studentListView;

    /** Creates the panel backed by the supplied observable student list. */
    public StudentListPanel(ObservableList<Student> students) {
        super(FXML);
        studentListView.setItems(students);
        studentListView.setCellFactory(unused -> new StudentListViewCell());
    }

    /** Returns the student currently selected in the list. */
    public ReadOnlyObjectProperty<Student> selectedStudentProperty() {
        return studentListView.getSelectionModel().selectedItemProperty();
    }

    /** Returns the ID of the currently selected student, if any. */
    public String getSelectedStudentId() {
        Student selected = studentListView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getId().value;
    }

    /** Returns whether the ID is currently visible in the list. */
    public boolean containsStudentId(String id) {
        return studentListView.getItems().stream().anyMatch(student -> student.getId().value.equals(id));
    }

    /** Clears the current student selection. */
    public void clearSelection() {
        studentListView.getSelectionModel().clearSelection();
    }

    /** Selects the first visible student, if one exists. */
    public void selectFirstStudent() {
        studentListView.getSelectionModel().selectFirst();
    }

    /** Selects and scrolls to the student with the supplied generated ID, if it is visible. */
    public void selectStudentId(String id) {
        for (int i = 0; i < studentListView.getItems().size(); i++) {
            if (studentListView.getItems().get(i).getId().value.equals(id)) {
                studentListView.getSelectionModel().select(i);
                studentListView.scrollTo(i);
                return;
            }
        }
    }

    /** Custom {@code ListCell} that displays a student using the shared Mentora card layout. */
    private static class StudentListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student student, boolean empty) {
            super.updateItem(student, empty);
            if (empty || student == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            setGraphic(new StudentCard(student).getRoot());
            setText(null);
        }
    }
}
