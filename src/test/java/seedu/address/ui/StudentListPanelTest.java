package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.stage.Stage;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class StudentListPanelTest {
    private static final double LAYOUT_TOLERANCE = 0.5;
    private static final double LIST_HEIGHT = 350;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void selectionMethods_selectByStableIdAndFirstStudent() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Student first = student("S-0092", "Amy Tan");
            Student second = student("S-0093", "Mei Lin");
            ObservableList<Student> students = FXCollections.observableArrayList(first, second);
            StudentListPanel panel = new StudentListPanel(students);

            panel.selectStudentId("S-0093");
            assertEquals(second, panel.selectedStudentProperty().get());
            panel.selectFirstStudent();
            assertEquals(first, panel.selectedStudentProperty().get());
            panel.selectStudentId("S-9999");
            assertEquals(first, panel.selectedStudentProperty().get());
        });
    }

    @Test
    void selectFirstStudent_emptyListLeavesSelectionEmpty() {
        JavaFxTestUtils.runOnFxThread(() -> {
            StudentListPanel panel = new StudentListPanel(FXCollections.observableArrayList());
            panel.selectFirstStudent();
            panel.selectStudentId("S-0001");
            assertNull(panel.selectedStudentProperty().get());
        });
    }

    @Test
    void layout_longNameKeepsLevelVisibleAtDifferentWidths() {
        JavaFxTestUtils.runOnFxThread(() -> {
            String longName = "W".repeat(80);
            Student shortStudent = student("S-0092", "Amy Tan", "Sec 1");
            Student longStudent = student("S-0093", longName, "Sec 2");
            Student multiwordStudent = student("S-0094", "Alexandra Marie Tan", "IP 4");
            StudentListPanel panel = new StudentListPanel(
                    FXCollections.observableArrayList(shortStudent, longStudent, multiwordStudent));
            Stage stage = showPanel(panel);
            try {
                ListView<?> list = (ListView<?>) panel.getRoot().lookup("#studentListView");
                for (double width : new double[]{320, 600, 320}) {
                    resizePanel(panel, list, width);
                    for (Student item : new Student[]{shortStudent, longStudent, multiwordStudent}) {
                        panel.selectStudentId(item.getId().toString());
                        layoutPanel(panel);
                        assertLevelVisible(list, item);
                    }
                    panel.selectStudentId(shortStudent.getId().toString());
                    layoutPanel(panel);
                    assertLevelVisible(list, longStudent);
                }
                StudentDetailsPanel details = new StudentDetailsPanel();
                new Scene(details.getRoot());
                details.getRoot().applyCss();
                details.setStudent(longStudent);
                assertEquals("S-0093 - " + longName, ((Label) details.getRoot().lookup("#name")).getText());
                assertEquals("Sec 2", ((Label) details.getRoot().lookup("#academicLevel")).getText());
                assertEquals(longName, longStudent.getName().toString());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void layout_scrollingReusesCellsWithoutStaleStudentDetails() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            for (int index = 0; index < 40; index++) {
                String name = index == 0 || index == 39 ? "W".repeat(80)
                        : "Student " + (char) ('A' + index / 26) + (char) ('A' + index % 26);
                int levelIndex = index % 11;
                String level = levelIndex < 5 ? "Sec " + (levelIndex + 1) : "IP " + (levelIndex - 4);
                students.add(student("S-" + (1000 + index), name, level));
            }
            StudentListPanel panel = new StudentListPanel(students);
            Stage stage = showPanel(panel);
            try {
                ListView<?> list = (ListView<?>) panel.getRoot().lookup("#studentListView");
                resizePanel(panel, list, 320);
                VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                assertTrue(flow.getLastVisibleCell().getIndex() < students.size() - 1,
                        "The fixture must require scrolling");
                Map<ListCell<?>, Student> previousItems = displayedStudents(list);
                boolean reusedForDifferentLevel = false;
                for (double width : new double[]{320, 600, 320}) {
                    resizePanel(panel, list, width);
                    for (int index : new int[]{39, 0, 20, 0}) {
                        Student target = students.get(index);
                        panel.selectStudentId(target.getId().toString());
                        layoutPanel(panel);
                        assertLevelVisible(list, target);
                        Map<ListCell<?>, Student> currentItems = displayedStudents(list);
                        for (Map.Entry<ListCell<?>, Student> entry : currentItems.entrySet()) {
                            Label level = (Label) entry.getKey().getGraphic().lookup("#academicLevel");
                            if (viewportBounds(list).contains(level.localToScene(level.getBoundsInLocal()))) {
                                assertLevelVisible(list, entry.getValue());
                                Student previous = previousItems.get(entry.getKey());
                                if (previous != null && !previous.getAcademicLevel()
                                        .equals(entry.getValue().getAcademicLevel())) {
                                    reusedForDifferentLevel = true;
                                }
                            }
                        }
                        previousItems.putAll(currentItems);
                    }
                    panel.clearSelection();
                    layoutPanel(panel);
                    assertLevelVisible(list, students.getFirst());
                }
                assertTrue(reusedForDifferentLevel,
                        "At least one displayed cell must be reused for a student with a different level");
            } finally {
                stage.close();
            }
        });
    }

    private static Map<ListCell<?>, Student> displayedStudents(ListView<?> list) {
        Map<ListCell<?>, Student> result = new HashMap<>();
        VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
        Bounds viewport = viewportBounds(list);
        for (int index = 0; index < list.getItems().size(); index++) {
            ListCell<?> cell = (ListCell<?>) flow.getVisibleCell(index);
            if (cell != null && cell.isVisible()
                    && viewport.intersects(cell.localToScene(cell.getBoundsInLocal()))) {
                result.put(cell, (Student) list.getItems().get(index));
            }
        }
        return result;
    }


    private static Stage showPanel(StudentListPanel panel) {
        // Size the content directly: native Stage resizing is deferred until a later pulse.
        Group host = new Group(panel.getRoot());
        host.setAutoSizeChildren(false);
        Scene scene = new Scene(host, 600, LIST_HEIGHT);
        scene.getStylesheets().add(StudentListPanelTest.class
                .getResource("/view/DarkTheme.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        return stage;
    }

    private static void resizePanel(StudentListPanel panel, ListView<?> list, double width) {
        panel.getRoot().resize(width, LIST_HEIGHT);
        layoutPanel(panel);
        assertEquals(width, panel.getRoot().getWidth(), LAYOUT_TOLERANCE, "Panel did not resize");
        assertEquals(width, list.getWidth(), LAYOUT_TOLERANCE, "List did not resize");
        assertTrue(viewportBounds(list).getWidth() > 0, "Viewport must be laid out");
    }

    private static void layoutPanel(StudentListPanel panel) {
        panel.getRoot().applyCss();
        panel.getRoot().layout();
    }

    private static Bounds viewportBounds(ListView<?> list) {
        Node viewport = list.lookup(".clipped-container");
        assertNotNull(viewport, "Expected the ListView skin's content viewport");
        assertNotNull(viewport.getClip(), "Expected a viewport clip");
        return viewport.localToScene(viewport.getClip().getBoundsInParent());
    }

    private static ListCell<?> visibleCell(ListView<?> list, Student item) {
        VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
        assertNotNull(flow);
        ListCell<?> cell = (ListCell<?>) flow.getVisibleCell(list.getItems().indexOf(item));
        assertNotNull(cell, "Expected a displayed cell for " + item.getId());
        assertTrue(cell.isVisible());
        assertEquals(item, cell.getItem());
        return cell;
    }

    private static void assertLevelVisible(ListView<?> list, Student item) {
        ListCell<?> cell = visibleCell(list, item);
        Label name = (Label) cell.getGraphic().lookup("#name");
        Label level = (Label) cell.getGraphic().lookup("#academicLevel");
        Bounds visible = viewportBounds(list);
        Bounds nameBounds = name.localToScene(name.getBoundsInLocal());
        Bounds levelBounds = level.localToScene(level.getBoundsInLocal());
        assertEquals(item.equals(list.getSelectionModel().getSelectedItem()), cell.isSelected());
        assertEquals(item.getName().toString(), name.getText());
        assertEquals(item.getAcademicLevel().toString(), level.getText());
        assertTrue(nameBounds.getWidth() > 0, "Name has no display space");
        assertTrue(levelBounds.getMinX() >= visible.getMinX() - LAYOUT_TOLERANCE);
        assertTrue(levelBounds.getMaxX() <= visible.getMaxX() + LAYOUT_TOLERANCE,
                "Level extends outside the content viewport");
        assertTrue(levelBounds.getMinY() >= visible.getMinY() - LAYOUT_TOLERANCE);
        assertTrue(levelBounds.getMaxY() <= visible.getMaxY() + LAYOUT_TOLERANCE);
        assertTrue(nameBounds.getMaxX() <= levelBounds.getMinX() + LAYOUT_TOLERANCE, "Name overlaps level");
        assertTrue(level.getWidth() >= level.prefWidth(-1) - LAYOUT_TOLERANCE, "Level text is clipped");
    }

    private static Student student(String id, String name) {
        return student(id, name, "Sec 2");
    }

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
