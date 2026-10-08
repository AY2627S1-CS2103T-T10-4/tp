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
    @FXML private ListView<Student> studentListView;
    public StudentListPanel(ObservableList<Student> students) {
        super(FXML);
        studentListView.setItems(students);
        studentListView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setText(empty || student == null ? null : student.getId() + "   " + student.getName()
                        + "   " + student.getAcademicLevel());
            }
        });
    }
    public ReadOnlyObjectProperty<Student> selectedStudentProperty() {
        return studentListView.getSelectionModel().selectedItemProperty();
    }
    public void selectFirstStudent() { studentListView.getSelectionModel().selectFirst(); }
    public void selectStudentId(String id) {
        for (int i = 0; i < studentListView.getItems().size(); i++) {
            if (studentListView.getItems().get(i).getId().value.equals(id)) {
                studentListView.getSelectionModel().select(i);
                studentListView.scrollTo(i);
                return;
            }
        }
    }
}
