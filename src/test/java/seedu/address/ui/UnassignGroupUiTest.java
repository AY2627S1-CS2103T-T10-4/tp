package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class UnassignGroupUiTest {
    @TempDir
    private Path temporaryFolder;

    private Stage stage;
    private MainWindow window;
    private ModelManager model;
    private TrackingStorage data;
    private Student targetStudent;
    private Student otherStudent;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @BeforeEach
    void setUp() {
        JavaFxTestUtils.runOnFxThread(() -> {
            AddressBook register = getUnlinkedAddressBook();
            targetStudent = register.findStudent("S-0007").orElseThrow();
            otherStudent = register.findStudent("S-0008").orElseThrow();
            register.linkGuardian("S-0007", "G-0003");
            register.linkGuardian("S-0008", "G-0004");
            register.addGroupAssignment(new GroupAssignment(targetStudent.getId(), new GroupCode("MAT")));
            register.addGroupAssignment(new GroupAssignment(targetStudent.getId(), new GroupCode("S2-MAT-A")));
            register.addGroupAssignment(new GroupAssignment(otherStudent.getId(), new GroupCode("MAT")));
            model = new ModelManager(register, new UserPrefs());
            data = new TrackingStorage(temporaryFolder.resolve("register.json"), model);
            new JsonAddressBookStorage(data.getAddressBookFilePath()).saveAddressBook(register);
            LogicManager logic = new LogicManager(model, new StorageManager(data,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            stage = new Stage();
            window = new MainWindow(stage, logic, data.getAddressBookFilePath());
            window.fillInnerParts();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            window.getStudentListPanel().selectStudentId("S-0007");
        });
    }

    @AfterEach
    void closeStage() {
        JavaFxTestUtils.runOnFxThread(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    @Test
    void submit_removalFromSelectedStudent_refreshesSameObjectProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            assertSame(targetStudent, selectedStudent());
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            AddressBook before = new AddressBook(model.getAddressBook());

            submit("unassign-group sid/S-0007 g/MAT");

            assertSame(targetStudent, selectedStudent());
            assertTargetProfile();
            assertEquals(List.of("S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            assertEquals("Removed group MAT from student S-0007.", feedback());
            assertWorkspace("studentsTab", "studentWorkspace");
            assertEquals(before.getStudentList(), model.getAddressBook().getStudentList());
            assertEquals(before.getGuardianList(), model.getAddressBook().getGuardianList());
            assertEquals(before.getGuardianLinks(), model.getAddressBook().getGuardianLinks());
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
        });
    }

    @Test
    void submit_finalRemoval_keepsSelectedProfileEmptyAcrossNavigation() {
        JavaFxTestUtils.runOnFxThread(() -> {
            submit("unassign-group sid/S-0007 g/MAT");

            submit("unassign-group sid/S-0007 g/S2-MAT-A");

            assertSame(targetStudent, selectedStudent());
            assertTargetProfile();
            assertEquals(List.of("No groups assigned."), assignments());
            assertEquals("Removed group S2-MAT-A from student S-0007.", feedback());
            assertEquals(2, data.saveAttempts);
            window.getStudentListPanel().selectStudentId("S-0008");
            assertEquals(List.of("MAT — Mathematics"), assignments());
            selectTab("guardiansTab");
            selectTab("groupsTab");
            selectTab("studentsTab");
            window.getStudentListPanel().selectStudentId("S-0007");
            assertSame(targetStudent, selectedStudent());
            assertTargetProfile();
            assertEquals(List.of("No groups assigned."), assignments());
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
        });
    }

    @Test
    void submit_fromGroupsAndGuardians_selectsTargetInStudents() {
        JavaFxTestUtils.runOnFxThread(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            window.getStudentListPanel().selectStudentId("S-0008");
            selectTab("groupsTab");

            submit("unassign-group sid/S-0007 g/MAT");

            assertWorkspace("studentsTab", "studentWorkspace");
            assertSame(targetStudent, selectedStudent());
            assertEquals(List.of("S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            window.getStudentListPanel().selectStudentId("S-0008");
            selectTab("guardiansTab");

            submit("unassign-group sid/S-0007 g/S2-MAT-A");

            assertWorkspace("studentsTab", "studentWorkspace");
            assertSame(targetStudent, selectedStudent());
            assertTargetProfile();
            assertEquals(List.of("No groups assigned."), assignments());
            assertEquals(before.getGuardianList(), model.getAddressBook().getGuardianList());
            assertEquals(before.getGuardianLinks(), model.getAddressBook().getGuardianLinks());
        });
    }

    @Test
    void submit_hiddenTarget_revealsAndRendersAfterSaving() {
        JavaFxTestUtils.runOnFxThread(() -> {
            selectOtherThroughSearch();
            selectTab("guardiansTab");

            submit("unassign-group sid/S-0007 g/MAT");

            assertEquals(List.of(otherStudent), data.visibleStudentsAtSave);
            assertEquals(List.of(targetStudent, otherStudent), model.getStudentList());
            assertSame(targetStudent, selectedStudent());
            assertWorkspace("studentsTab", "studentWorkspace");
            assertTargetProfile();
            assertEquals(List.of("S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            assertEquals(1, data.saveAttempts);
            assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
        });
    }

    @Test
    void submit_visibleTarget_keepsFilterAndRefreshesProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            model.updateFilteredStudentList(student -> student.equals(targetStudent));

            submit("unassign-group sid/S-0007 g/MAT");

            assertEquals(List.of(targetStudent), data.visibleStudentsAtSave);
            assertEquals(List.of(targetStudent), model.getStudentList());
            assertSame(targetStudent, selectedStudent());
            assertWorkspace("studentsTab", "studentWorkspace");
            assertTargetProfile();
            assertEquals(List.of("S2-MAT-A — Secondary 2 Mathematics A"), assignments());
        });
    }

    @Test
    void submit_validationFailures_preservesFilteredProfileAndWorkspace() {
        JavaFxTestUtils.runOnFxThread(() -> {
            selectOtherThroughSearch();
            selectTab("groupsTab");
            AddressBook before = new AddressBook(model.getAddressBook());
            byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());
            Map<String, String> invalidCommands = Map.of(
                    "unassign-group sid/S-0007 g/SCI", "Student S-0007 is not assigned to SCI.",
                    "unassign-group sid/S-9999 g/MAT", "Student S-9999 does not exist.",
                    "unassign-group sid/S-0007 g/unknown",
                    "Group UNKNOWN is not configured. Run list-groups to see available groups.",
                    "unassign-group sid/S-0007",
                    "Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE");

            for (Map.Entry<String, String> entry : invalidCommands.entrySet()) {
                submit(entry.getKey());

                assertEquals(entry.getValue(), feedback());
                assertWorkspace("groupsTab", "groupsWorkspacePlaceholder");
                assertSame(otherStudent, selectedStudent());
                assertEquals(List.of(otherStudent), model.getStudentList());
                assertEquals(List.of("MAT — Mathematics"), assignments());
                assertEquals("Raj Kumar\n+65 61234567", profileLabel("guardianDetails"));
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));
                assertTrue(commandInput().getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            }
            assertEquals(0, data.saveAttempts);
        });
    }

    @Test
    void submit_repeatedRemovalAndMalformedInput_preservesSelectedOrEmptyProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            submit("unassign-group sid/S-0007 g/MAT");
            AddressBook before = new AddressBook(model.getAddressBook());
            byte[] savedBytes = Files.readAllBytes(data.getAddressBookFilePath());

            submit("unassign-group sid/S-0007 g/MAT");

            assertEquals("Student S-0007 is not assigned to MAT.", feedback());
            assertSame(targetStudent, selectedStudent());
            assertTargetProfile();
            assertEquals(List.of("S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            window.getStudentListPanel().clearSelection();
            selectTab("groupsTab");

            submit("unassign-group sid/S-0007");

            assertEquals("Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE", feedback());
            assertNull(selectedStudent());
            assertTrue(profile().lookup("#emptyState").isVisible());
            assertFalse(profile().lookup("#detailsContent").isVisible());
            assertTrue(assignments().isEmpty());
            assertWorkspace("groupsTab", "groupsWorkspacePlaceholder");
            assertEquals(before, model.getAddressBook());
            assertEquals(1, data.saveAttempts);
            assertArrayEquals(savedBytes, Files.readAllBytes(data.getAddressBookFilePath()));
        });
    }

    @Test
    void submit_preWriteSaveFailure_restoresSelectedProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            model.updateFilteredStudentList(student -> student.equals(targetStudent));
            selectTab("groupsTab");
            AddressBook before = new AddressBook(model.getAddressBook());
            byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());
            data.failure = new IOException("Synthetic failure before writing");

            submit("unassign-group sid/S-0007 g/MAT");

            assertEquals("Could not save changes. The group assignment was not removed.", feedback());
            assertWorkspace("groupsTab", "groupsWorkspacePlaceholder");
            assertSame(targetStudent, selectedStudent());
            assertEquals(List.of(targetStudent), model.getStudentList());
            assertTargetProfile();
            assertEquals(List.of("MAT — Mathematics", "S2-MAT-A — Secondary 2 Mathematics A"), assignments());
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));
        });
    }

    @Test
    void submit_preWriteFailureForHiddenTarget_preservesOtherOrEmptySelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            selectOtherThroughSearch();
            selectTab("guardiansTab");
            AddressBook before = new AddressBook(model.getAddressBook());
            byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());
            Node guardianProfile = stage.getScene().lookup("#guardianDetailsPanelPlaceholder");
            String guardianName = ((Label) guardianProfile.lookup("#name")).getText();
            data.failure = new IOException("Synthetic failure before writing");

            submit("unassign-group sid/S-0007 g/MAT");

            assertEquals("Could not save changes. The group assignment was not removed.", feedback());
            assertWorkspace("guardiansTab", "guardianWorkspace");
            assertSame(otherStudent, selectedStudent());
            assertEquals(List.of(otherStudent), model.getStudentList());
            assertEquals(List.of("MAT — Mathematics"), assignments());
            assertEquals("Raj Kumar\n+65 61234567", profileLabel("guardianDetails"));
            assertEquals(guardianName, ((Label) guardianProfile.lookup("#name")).getText());
            window.getStudentListPanel().clearSelection();

            submit("unassign-group sid/S-0007 g/S2-MAT-A");

            assertEquals("Could not save changes. The group assignment was not removed.", feedback());
            assertNull(selectedStudent());
            assertTrue(profile().lookup("#emptyState").isVisible());
            assertTrue(assignments().isEmpty());
            assertWorkspace("guardiansTab", "guardianWorkspace");
            assertEquals(List.of(otherStudent), model.getStudentList());
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));
        });
    }

    @Test
    void submit_longUnknownGroupAtCompactSize_keepsResultHeadingReadable() {
        JavaFxTestUtils.runOnFxThread(() -> {
            stage.getScene().getRoot().resize(884, 563);
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            String groupCode = "UNKNOWN-GROUP-CODE-WITH-A-LONG-SYNTHETIC-NAME-FOR-COMPACT-LAYOUT";

            submit("unassign-group sid/S-0007 g/" + groupCode);
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();

            assertEquals("Group " + groupCode + " is not configured. Run list-groups to see available groups.",
                    feedback());
            Label heading = (Label) stage.getScene().lookup(".result-label");
            Text renderedHeading = (Text) heading.lookup(".text");
            assertEquals("RESULT", renderedHeading.getText());
        });
    }

    private void selectOtherThroughSearch() {
        submit("find-student n/Ling");
        window.getStudentListPanel().selectStudentId("S-0008");
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertSame(otherStudent, selectedStudent());
    }

    private void assertTargetProfile() {
        assertEquals("S-0007", profileLabel("id"));
        assertEquals("Mei Lin", profileLabel("name"));
        assertEquals("Sec 2", profileLabel("academicLevel"));
        assertEquals("Anita Lim\n+65 91234567", profileLabel("guardianDetails"));
        assertTrue(profile().lookup("#detailsContent").isVisible());
        assertFalse(profile().lookup("#emptyState").isVisible());
    }

    private void assertWorkspace(String tabId, String workspaceId) {
        assertTrue(((ToggleButton) stage.getScene().lookup("#" + tabId)).isSelected());
        Node workspace = stage.getScene().lookup("#" + workspaceId);
        assertTrue(workspace.isVisible());
        assertTrue(workspace.isManaged());
    }

    private void submit(String text) {
        TextField input = commandInput();
        input.setText(text);
        input.getOnAction().handle(new ActionEvent(input, input));
    }

    private TextField commandInput() {
        return (TextField) stage.getScene().lookup("#commandTextField");
    }

    private void selectTab(String id) {
        ToggleButton tab = (ToggleButton) stage.getScene().lookup("#" + id);
        tab.fire();
    }

    private Student selectedStudent() {
        return window.getStudentListPanel().selectedStudentProperty().get();
    }

    private Node profile() {
        return stage.getScene().lookup("#studentDetailsPanelPlaceholder");
    }

    private String profileLabel(String id) {
        return ((Label) profile().lookup("#" + id)).getText();
    }

    private String feedback() {
        return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private List<String> assignments() {
        VBox rows = (VBox) profile().lookup("#assignments");
        return rows.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }

    /** Uses real JSON writes unless an explicitly pre-write failure is injected. */
    private static class TrackingStorage extends JsonAddressBookStorage {
        private final ModelManager model;
        private int saveAttempts;
        private IOException failure;
        private List<Student> visibleStudentsAtSave;

        TrackingStorage(Path path, ModelManager model) {
            super(path);
            this.model = model;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            visibleStudentsAtSave = List.copyOf(model.getStudentList());
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
