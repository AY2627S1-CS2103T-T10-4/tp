package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class AssignGroupUiTest {
    @TempDir
    private Path testFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void assignGroup_updatesSameStudentProfileAndSupportsSelectionAndTabs() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Student amy = new Student(new StudentId("S-0701"), new StudentName("Amy Tan"), new AcademicLevel("Sec 1"));
            Student mei = new Student(new StudentId("S-0702"), new StudentName("Mei Lin"), new AcademicLevel("Sec 3"));
            model.addStudent(amy);
            model.addStudent(mei);
            Stage stage = new Stage();
            MainWindow window = createWindow(model, new JsonAddressBookStorage(testFolder.resolve("students.json")),
                    stage);
            submit(stage, "list-groups");

            submit(stage, "assign-group SID/S-0702 G/mat");

            assertTrue(((ToggleButton) stage.getScene().lookup("#studentsTab")).isSelected());
            assertSame(mei, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("MAT — Mathematics"), assignments(stage));
            submit(stage, "assign-group g/s2-mat-a sid/S-0702");
            assertSame(mei, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments(stage));
            Node profile = stage.getScene().lookup("#studentDetailsPanelPlaceholder");
            assertEquals("Sec 3", ((Label) profile.lookup("#academicLevel")).getText());
            window.getStudentListPanel().selectStudentId("S-0701");
            assertEquals(List.of("No groups assigned."), assignments(stage));
            submit(stage, "assign-group sid/S-0701 g/ENG");
            assertEquals(List.of("ENG — English"), assignments(stage));
            selectTab(stage, "groupsTab");
            selectTab(stage, "studentsTab");
            assertEquals(List.of("ENG — English"), assignments(stage));
            window.getStudentListPanel().selectStudentId("S-0702");
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments(stage));
            stage.close();
        });
    }

    @Test
    void filteredCommands_revealTargetsAndRetainNavigationContracts() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("filtered.json"));
            data.saveAddressBook(model.getAddressBook());
            byte[] beforeQueries = Files.readAllBytes(data.getAddressBookFilePath());
            Stage stage = new Stage();
            MainWindow window = createWindow(model, data, stage);
            submit(stage, "list-groups");
            submit(stage, "find-student n/Mei");
            submit(stage, "view-student sid/S-9402");

            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
            Student amy = model.findStudent("S-9402").orElseThrow();
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            Node profile = stage.getScene().lookup("#studentDetailsPanelPlaceholder");
            assertEquals("Amy Tan", ((Label) profile.lookup("#name")).getText());
            assertEquals(List.of("No groups assigned."), assignments(stage));
            assertArrayEquals(beforeQueries, Files.readAllBytes(data.getAddressBookFilePath()));

            submit(stage, "find-student n/Mei");
            submit(stage, "assign-group sid/S-9402 g/SCI");
            assertTrue(((ToggleButton) stage.getScene().lookup("#studentsTab")).isSelected());
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("SCI — Science"), assignments(stage));
            submit(stage, "assign-group sid/S-9402 g/CHI");
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("SCI — Science", "CHI — Chinese"), assignments(stage));
            window.getStudentListPanel().selectStudentId("S-9401");
            assertEquals(List.of("MAT — Mathematics"), assignments(stage));
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
            stage.close();
        });
    }

    @Test
    void addStudentOutsideFilter_selectsNewProfileAfterSaving() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("filtered-add.json"));
            Stage stage = new Stage();
            MainWindow window = createWindow(model, data, stage);
            submit(stage, "find-student n/Mei");
            submit(stage, "add-student n/Zara Lim al/Sec 4");

            Student selected = window.getStudentListPanel().selectedStudentProperty().get();
            assertEquals("Zara Lim", selected.getName().toString());
            assertEquals(3, model.getStudentList().size());
            assertEquals(List.of("No groups assigned."), assignments(stage));
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
            stage.close();
        });
    }

    @Test
    void editHiddenStudent_refreshesReplacedProfileAndPreservesAssignmentsAndTabs() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            Student mei = model.findStudent("S-9401").orElseThrow();
            model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("S2-MAT-A")));
            JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("edited.json"));
            Stage stage = new Stage();
            MainWindow window = createWindow(model, data, stage);
            submit(stage, "find-student n/Amy");
            submit(stage, "list-groups");

            submit(stage, "edit-student sid/S-9401 al/Sec 4");

            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
            Student edited = model.findStudent("S-9401").orElseThrow();
            assertSame(edited, window.getStudentListPanel().selectedStudentProperty().get());
            assertSame(mei.getId(), edited.getId());
            assertEquals(2, model.getStudentList().size());
            selectTab(stage, "studentsTab");
            Node profile = stage.getScene().lookup("#studentDetailsPanelPlaceholder");
            assertEquals("Mei Lin", ((Label) profile.lookup("#name")).getText());
            assertEquals("Sec 4", ((Label) profile.lookup("#academicLevel")).getText());
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments(stage));

            submit(stage, "edit-student sid/S-9401 al/IP 5");
            assertSame(model.findStudent("S-9401").orElseThrow(),
                    window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals("IP 5", ((Label) profile.lookup("#academicLevel")).getText());
            window.getStudentListPanel().selectStudentId("S-9402");
            assertEquals("Sec 3", ((Label) profile.lookup("#academicLevel")).getText());
            assertEquals(List.of("No groups assigned."), assignments(stage));
            window.getStudentListPanel().selectStudentId("S-9401");
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments(stage));
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
            stage.close();
        });
    }

    @Test
    void failedEdit_restoresProfileFilterSelectionWorkspaceAndCompleteRegister() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            AddressBook before = new AddressBook(model.getAddressBook());
            Path path = testFolder.resolve("failed-edit.json");
            new JsonAddressBookStorage(path).saveAddressBook(before);
            byte[] original = Files.readAllBytes(path);
            JsonAddressBookStorage failed = new JsonAddressBookStorage(path) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook records) throws IOException {
                    throw new IOException("synthetic failure before writing");
                }
            };
            Stage stage = new Stage();
            MainWindow window = createWindow(model, failed, stage);
            submit(stage, "find-student n/Mei");
            window.getStudentListPanel().selectStudentId("S-9401");
            Student mei = window.getStudentListPanel().selectedStudentProperty().get();
            submit(stage, "list-groups");

            for (String target : List.of("S-9401", "S-9402")) {
                submit(stage, "edit-student sid/" + target + " al/Sec 4");
                assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
                assertEquals(List.of(mei), model.getStudentList());
                assertSame(mei, window.getStudentListPanel().selectedStudentProperty().get());
                Node profile = stage.getScene().lookup("#studentDetailsPanelPlaceholder");
                assertEquals("Sec 2", ((Label) profile.lookup("#academicLevel")).getText());
                assertEquals(List.of("MAT — Mathematics"), assignments(stage));
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(original, Files.readAllBytes(path));
                Label feedback = (Label) stage.getScene().lookup("#resultDisplay");
                assertEquals("Could not save changes. Student " + target + " was not updated.", feedback.getText());
            }
            window.getStudentListPanel().clearSelection();
            submit(stage, "edit-student sid/S-9401 al/Sec 4");
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            assertTrue(assignments(stage).isEmpty());
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(original, Files.readAllBytes(path));
            stage.close();
        });
    }

    @Test
    void deleteStudents_preservesOtherProfilesAndClearsOnlyTheDeletedSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            Student mei = model.findStudent("S-9401").orElseThrow();
            Student amy = model.findStudent("S-9402").orElseThrow();
            Student zara = new Student(new StudentId("S-9403"), new StudentName("Zara Lim"),
                    new AcademicLevel("Sec 4"));
            model.addStudent(zara);
            model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("S2-MAT-A")));
            model.addGroupAssignment(new GroupAssignment(amy.getId(), new GroupCode("ENG")));
            List<Guardian> guardians = List.copyOf(model.getGuardianList());
            JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("deleted.json"));
            Stage stage = new Stage();
            MainWindow window = createWindow(model, data, stage);
            submit(stage, "find-student n/Amy");
            window.getStudentListPanel().selectStudentId("S-9402");
            submit(stage, "list-groups");

            submit(stage, "delete-student sid/S-9401");

            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of(amy), model.getStudentList());
            assertEquals(List.of(amy, zara), model.getAddressBook().getStudentList());
            assertTrue(model.getAssignedGroups("S-9401").isEmpty());
            assertEquals(List.of("ENG — English"), assignments(stage));
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());

            submit(stage, "list-students");
            submit(stage, "delete-student sid/S-9403");
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("ENG — English"), assignments(stage));
            submit(stage, "delete-student sid/S-9402");
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            assertTrue(assignments(stage).isEmpty());
            Node profile = stage.getScene().lookup("#studentDetailsPanelPlaceholder");
            assertTrue(profile.lookup("#emptyState").isVisible());
            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
            assertTrue(model.getAddressBook().getStudentList().isEmpty());
            assertTrue(model.getAddressBook().getGroupAssignments().isEmpty());
            assertEquals(guardians, model.getGuardianList());
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
            stage.close();
        });
    }

    @Test
    void failedDelete_restoresAssignmentsFilterSelectionAndWorkspace() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            Student mei = model.findStudent("S-9401").orElseThrow();
            model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("S2-MAT-A")));
            AddressBook before = new AddressBook(model.getAddressBook());
            Path path = testFolder.resolve("failed-delete.json");
            new JsonAddressBookStorage(path).saveAddressBook(before);
            byte[] original = Files.readAllBytes(path);
            JsonAddressBookStorage failed = new JsonAddressBookStorage(path) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook records) throws IOException {
                    throw new IOException("synthetic failure before writing");
                }
            };
            Stage stage = new Stage();
            MainWindow window = createWindow(model, failed, stage);
            submit(stage, "find-student n/Mei");
            window.getStudentListPanel().selectStudentId("S-9401");
            submit(stage, "list-groups");

            for (String target : List.of("S-9401", "S-9402")) {
                submit(stage, "delete-student sid/" + target);
                assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
                assertEquals(List.of(mei), model.getStudentList());
                assertSame(mei, window.getStudentListPanel().selectedStudentProperty().get());
                assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments(stage));
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(original, Files.readAllBytes(path));
                Label feedback = (Label) stage.getScene().lookup("#resultDisplay");
                assertEquals("Could not save changes. Student " + target + " was not deleted.", feedback.getText());
            }
            window.getStudentListPanel().clearSelection();
            submit(stage, "delete-student sid/S-9401");
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            assertTrue(assignments(stage).isEmpty());
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(original, Files.readAllBytes(path));
            stage.close();
        });
    }

    @Test
    void failedAssignmentToHiddenStudent_preservesFilterSelectionWorkspaceAndMixedState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = mixedModel();
            AddressBook before = new AddressBook(model.getAddressBook());
            Path path = testFolder.resolve("filtered-failure.json");
            new JsonAddressBookStorage(path).saveAddressBook(before);
            byte[] original = Files.readAllBytes(path);
            JsonAddressBookStorage failed = new JsonAddressBookStorage(path) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook records) throws IOException {
                    throw new IOException("synthetic failure before writing");
                }
            };
            Stage stage = new Stage();
            MainWindow window = createWindow(model, failed, stage);
            submit(stage, "find-student n/Mei");
            window.getStudentListPanel().selectStudentId("S-9401");
            Student mei = window.getStudentListPanel().selectedStudentProperty().get();
            submit(stage, "list-groups");
            submit(stage, "assign-group sid/S-9402 g/SCI");

            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
            assertEquals(List.of(mei), model.getStudentList());
            assertSame(mei, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("MAT — Mathematics"), assignments(stage));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(original, Files.readAllBytes(path));
            Label feedback = (Label) stage.getScene().lookup("#resultDisplay");
            assertEquals("Could not save changes. The group was not assigned.", feedback.getText());
            stage.close();
        });
    }

    @Test
    void assignGroup_failedSave_restoresAssignmentsSelectionAndWorkspace() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Student amy = new Student(new StudentId("S-0701"), new StudentName("Amy Tan"), new AcademicLevel("Sec 1"));
            Student mei = new Student(new StudentId("S-0702"), new StudentName("Mei Lin"), new AcademicLevel("Sec 3"));
            model.addStudent(amy);
            model.addStudent(mei);
            model.addGroupAssignment(new GroupAssignment(amy.getId(), new GroupCode("ENG")));
            model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
            AddressBook before = new AddressBook(model.getAddressBook());
            Path path = testFolder.resolve("failed-save.json");
            new JsonAddressBookStorage(path).saveAddressBook(before);
            byte[] original = Files.readAllBytes(path);
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(path) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook ignored) throws IOException {
                    throw new IOException("synthetic save failure before writing");
                }
            };
            Stage stage = new Stage();
            MainWindow window = createWindow(model, failingStorage, stage);

            submit(stage, "assign-group sid/S-0701 g/SCI");
            assertEquals(List.of("ENG — English"), assignments(stage));
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            Label feedback = (Label) stage.getScene().lookup("#resultDisplay");
            assertEquals("Could not save changes. The group was not assigned.", feedback.getText());

            selectTab(stage, "groupsTab");
            submit(stage, "assign-group sid/S-0702 g/S2-MAT-A");
            assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(List.of("ENG — English"), assignments(stage));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(original, Files.readAllBytes(path));

            selectTab(stage, "studentsTab");
            window.getStudentListPanel().clearSelection();
            submit(stage, "assign-group sid/S-0702 g/CHI");
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            assertTrue(assignments(stage).isEmpty());
            stage.close();
        });
    }

    @Test
    void compactLayout_allAssignmentsAreScrollableAndFooterRemainsVisible() {
        assertCompactLayout("Mei Lin", 884, 563);
    }

    @Test
    void minimumLayout_longStudentName_keepsLastAssignmentInsideViewport() {
        assertCompactLayout("W".repeat(80), 804, 523);
    }

    private void assertCompactLayout(String name, double width, double height) {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Student student = new Student(new StudentId("S-0701"), new StudentName(name),
                    new AcademicLevel("Sec 2"));
            model.addStudent(student);
            ConfiguredGroups.getCatalogue().getGroups().forEach(group ->
                    model.addGroupAssignment(new GroupAssignment(student.getId(), group.getCode())));
            Stage stage = new Stage();
            createWindow(model, new JsonAddressBookStorage(testFolder.resolve("layout.json")), stage);
            stage.getScene().getRoot().resize(width, height);
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            Node footer = stage.getScene().lookup("#statusbarPlaceholder");
            ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#profileScrollPane");

            assertTrue(footer.localToScene(footer.getBoundsInLocal()).getMaxY() <= height + 0.5);
            assertTrue(scroll.getViewportBounds().getHeight() > 0);
            scroll.setVvalue(scroll.getVmax());
            stage.getScene().getRoot().layout();
            VBox assignmentRows = (VBox) scroll.getContent().lookup("#assignments");
            Node lastGroup = assignmentRows.getChildren().getLast();
            Node viewport = scroll.lookup(".viewport");
            Bounds visible = viewport.localToScene(viewport.getBoundsInLocal());
            Bounds last = lastGroup.localToScene(lastGroup.getBoundsInLocal());
            assertTrue(last.getMinY() >= visible.getMinY() - 0.5);
            assertTrue(last.getMaxY() <= visible.getMaxY() + 0.5);
            assertTrue(visible.getMaxY() <= footer.localToScene(footer.getBoundsInLocal()).getMinY());
            assertEquals(5, assignments(stage).size());
            stage.close();
        });
    }

    private static ModelManager mixedModel() {
        ModelManager model = new ModelManager();
        Student mei = new Student(new StudentId("S-9401"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        model.addStudent(mei);
        model.addStudent(new Student(new StudentId("S-9402"), new StudentName("Amy Tan"),
                new AcademicLevel("Sec 3")));
        model.addGuardian(new Guardian(new GuardianId("G-9401"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567")));
        model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
        return model;
    }

    private MainWindow createWindow(ModelManager model, JsonAddressBookStorage data, Stage stage) {
        LogicManager logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, data.getAddressBookFilePath());
        window.fillInnerParts();
        // SplitPane creates its scene-graph children when its CSS skin is initialized.
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return window;
    }

    private static void submit(Stage stage, String text) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(text);
        input.getOnAction().handle(new ActionEvent(input, input));
    }

    private static void selectTab(Stage stage, String id) {
        ToggleButton tab = (ToggleButton) stage.getScene().lookup("#" + id);
        tab.fire();
    }

    private static List<String> assignments(Stage stage) {
        VBox assignments = (VBox) stage.getScene().lookup("#assignments");
        return assignments.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }
}
