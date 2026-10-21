package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Verifies that editing a shared guardian record immediately refreshes open student profiles. */
class EditGuardianPhoneUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_editLinkedGuardianPhone_refreshesSelectedAndOtherStudentProfiles() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
            model.linkGuardian("S-0007", "G-0003");
            model.linkGuardian("S-0008", "G-0003");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Anita Lim\n+65 91234567", guardianDetails(stage));

                submit(stage, "edit-guardian gid/G-0003 p/98765432");

                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Anita Lim\n+65 98765432", guardianDetails(stage));
                submit(stage, "view-student sid/S-0008");
                assertEquals("Anita Lim\n+65 98765432", guardianDetails(stage));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_saveFailureAfterSuccessfulEdit_restoresSelectedGuardian() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "list-guardians");
                submit(stage, "edit-guardian gid/G-0003 p/98765432");
                Guardian previousGuardian = model.findGuardian("G-0003").orElseThrow();
                assertSame(previousGuardian, window.getGuardianListPanel().selectedGuardianProperty().get());
                Student previousStudent = window.getStudentListPanel().selectedStudentProperty().get();
                AddressBook before = new AddressBook(model.getAddressBook());
                byte[] savedBytes = Files.readAllBytes(storage.getAddressBookFilePath());
                storage.failure = new IOException("Injected failure before writing");

                try (CommandLogRecorder logs = new CommandLogRecorder(model)) {
                    submit(stage, "edit-guardian gid/G-0003 p/81234567");

                    assertEquals(1, logs.warnings().size());
                    LogRecord warning = logs.warnings().getFirst();
                    assertEquals(Level.WARNING, warning.getLevel());
                    assertEquals("ab3.LogicManager", warning.getLoggerName());
                    assertSame(storage.failure, warning.getThrown());
                    assertTrue(warning.getMessage().contains("EditGuardianCommand"));
                    assertTrue(warning.getMessage().contains("in-memory rollback completed"));
                    assertNoContactPayload(warning);
                    assertEquals(List.of(before), logs.registersAtWarnings);
                    assertEquals(1, logs.uiRecords().size());
                    assertEquals(Level.FINE, logs.uiRecords().getFirst().getLevel());
                    assertNoContactPayload(logs.uiRecords().getFirst());
                }

                assertEquals("Could not save changes. Guardian G-0003 was not updated.", feedback(stage));
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(savedBytes, Files.readAllBytes(storage.getAddressBookFilePath()));
                assertEquals(2, storage.saveAttempts);
                assertSame(model.findGuardian("G-0003").orElseThrow(),
                        window.getGuardianListPanel().selectedGuardianProperty().get());
                assertNotSame(storage.failedGuardian, window.getGuardianListPanel().selectedGuardianProperty().get());
                assertSame(previousStudent, window.getStudentListPanel().selectedStudentProperty().get());
                assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                assertGuardianProfile(stage, "+65 98765432");
                assertEquals("Anita Lim\n+65 98765432", guardianDetails(stage));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_saveFailureWithoutGuardianSelection_keepsProfileUnselected() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "find-guardian n/Anita");
                guardianList(stage).getSelectionModel().clearSelection();
                Node placeholder = guardianList(stage).getPlaceholder();
                Student student = window.getStudentListPanel().selectedStudentProperty().get();
                AddressBook before = new AddressBook(model.getAddressBook());
                storage.failure = new IOException("Injected failure before writing");

                submit(stage, "edit-guardian gid/G-0003 p/98765432");

                assertNull(window.getGuardianListPanel().selectedGuardianProperty().get());
                Node details = stage.getScene().lookup("#guardianDetailsRoot");
                assertTrue(details.lookup("#emptyState").isVisible());
                assertFalse(details.lookup("#detailsContent").isVisible());
                assertSame(student, window.getStudentListPanel().selectedStudentProperty().get());
                assertSame(placeholder, guardianList(stage).getPlaceholder());
                assertEquals(List.of(model.findGuardian("G-0003").orElseThrow()), model.getGuardianList());
                assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                assertEquals(before, model.getAddressBook());
                assertEquals(1, storage.saveAttempts);
                assertEquals("Could not save changes. Guardian G-0003 was not updated.", feedback(stage));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_failedEditWithVisibleFilteredGuardian_preservesSearchAndStudentState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "edit-guardian gid/G-0003 p/98765432");
                submit(stage, "FiNd-GuArDiAn n/AnItA   lIm");
                assertEquals("Found 1 guardian(s) matching \"AnItA lIm\".", feedback(stage));
                guardianList(stage).getSelectionModel().selectFirst();
                model.updateFilteredStudentList(student -> student.getId().value.equals("S-0007"));
                window.getStudentListPanel().selectStudentId("S-0007");
                submit(stage, "list-groups");
                Predicate<?> studentFilter = ((FilteredList<?>) model.getStudentList()).getPredicate();
                Predicate<?> guardianFilter = ((FilteredList<?>) model.getGuardianList()).getPredicate();
                Node placeholder = guardianList(stage).getPlaceholder();
                Student student = window.getStudentListPanel().selectedStudentProperty().get();
                AddressBook before = new AddressBook(model.getAddressBook());
                storage.failure = new IOException("Injected failure before writing");

                submit(stage, "edit-guardian gid/G-0003 p/81234567");

                assertSame(model.findGuardian("G-0003").orElseThrow(),
                        window.getGuardianListPanel().selectedGuardianProperty().get());
                assertGuardianProfile(stage, "+65 98765432");
                assertSame(student, window.getStudentListPanel().selectedStudentProperty().get());
                assertSame(studentFilter, ((FilteredList<?>) model.getStudentList()).getPredicate());
                assertSame(guardianFilter, ((FilteredList<?>) model.getGuardianList()).getPredicate());
                assertSame(placeholder, guardianList(stage).getPlaceholder());
                assertEquals(List.of(model.findGuardian("G-0003").orElseThrow()), model.getGuardianList());
                assertEquals(List.of(student), model.getStudentList());
                assertTrue(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
                assertEquals("Could not save changes. Guardian G-0003 was not updated.", feedback(stage));
                assertEquals(before, model.getAddressBook());
                assertEquals(2, storage.saveAttempts);

                submit(stage, "list-guardians");

                assertEquals(before.getGuardianList(), model.getGuardianList());
                assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                assertEquals(2, storage.saveAttempts);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_failedEditOfHiddenGuardian_preservesEmptySearchFeedback() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "find-guardian n/Nobody");
                window.getStudentListPanel().clearSelection();
                Node placeholder = guardianList(stage).getPlaceholder();
                assertEquals("No guardians match \"Nobody\"", ((Label) placeholder).getText());
                Predicate<?> guardianFilter = ((FilteredList<?>) model.getGuardianList()).getPredicate();
                AddressBook before = new AddressBook(model.getAddressBook());
                storage.failure = new IOException("Injected failure before writing");

                submit(stage, "edit-guardian gid/G-0003 p/98765432");

                assertSame(placeholder, guardianList(stage).getPlaceholder());
                assertEquals("No guardians match \"Nobody\"", ((Label) placeholder).getText());
                assertSame(guardianFilter, ((FilteredList<?>) model.getGuardianList()).getPredicate());
                assertTrue(model.getGuardianList().isEmpty());
                assertNull(window.getGuardianListPanel().selectedGuardianProperty().get());
                assertNull(window.getStudentListPanel().selectedStudentProperty().get());
                assertTrue(stage.getScene().lookup("#guardianDetailsRoot").lookup("#emptyState").isVisible());
                assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                assertEquals("Could not save changes. Guardian G-0003 was not updated.", feedback(stage));
                assertEquals(before, model.getAddressBook());
                assertEquals(1, storage.saveAttempts);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_validationFailures_preserveSearchSelectionWithoutSaveWarnings() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "find-guardian n/Anita");
                guardianList(stage).getSelectionModel().selectFirst();
                Guardian guardian = window.getGuardianListPanel().selectedGuardianProperty().get();
                Student student = window.getStudentListPanel().selectedStudentProperty().get();
                Predicate<?> guardianFilter = ((FilteredList<?>) model.getGuardianList()).getPredicate();
                Node placeholder = guardianList(stage).getPlaceholder();
                AddressBook before = new AddressBook(model.getAddressBook());
                storage.failure = new IOException("Validation must not reach storage");
                Map<String, String> invalidCommands = Map.of(
                        "edit-guardian gid/G-0003",
                        "Invalid command format. Use: edit-guardian gid/GUARDIAN_ID p/PHONE",
                        "edit-guardian gid/G-9999 p/98765432", "Guardian G-9999 does not exist.",
                        "edit-guardian gid/G-0003 p/61234567", "Phone +65 61234567 is already used by guardian G-0004.",
                        "find-guardian n/Raj n/Anita", "Invalid command format. Use: find-guardian n/QUERY");

                try (CommandLogRecorder logs = new CommandLogRecorder(model)) {
                    for (Map.Entry<String, String> entry : invalidCommands.entrySet()) {
                        submit(stage, entry.getKey());

                        assertEquals(entry.getValue(), feedback(stage));
                        assertSame(guardian, window.getGuardianListPanel().selectedGuardianProperty().get());
                        assertSame(student, window.getStudentListPanel().selectedStudentProperty().get());
                        assertSame(guardianFilter, ((FilteredList<?>) model.getGuardianList()).getPredicate());
                        assertSame(placeholder, guardianList(stage).getPlaceholder());
                        assertEquals(before, model.getAddressBook());
                        assertGuardianProfile(stage, "+65 91234567");
                        assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                    }
                    assertTrue(logs.warnings().isEmpty());
                    assertEquals(invalidCommands.size(), logs.uiRecords().size());
                    for (LogRecord record : logs.uiRecords()) {
                        assertEquals(Level.FINE, record.getLevel());
                        assertTrue(record.getMessage().startsWith("Command rejected: "));
                        assertNoContactPayload(record);
                    }
                }
                assertEquals(0, storage.saveAttempts);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void selectGuardianId_absentHiddenOrNull_clearsWithoutChangingSearch() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            RecordingStorage storage = new RecordingStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "find-guardian n/Raj");
                Guardian visibleGuardian = model.findGuardian("G-0004").orElseThrow();
                Predicate<?> guardianFilter = ((FilteredList<?>) model.getGuardianList()).getPredicate();
                Node placeholder = guardianList(stage).getPlaceholder();
                AddressBook before = new AddressBook(model.getAddressBook());

                for (String unavailableId : new String[] {null, "G-0003", "G-9999"}) {
                    window.getGuardianListPanel().selectGuardianId("G-0004");
                    assertSame(visibleGuardian, window.getGuardianListPanel().selectedGuardianProperty().get());

                    window.getGuardianListPanel().selectGuardianId(unavailableId);

                    assertNull(window.getGuardianListPanel().selectedGuardianProperty().get());
                    assertSame(guardianFilter, ((FilteredList<?>) model.getGuardianList()).getPredicate());
                    assertSame(placeholder, guardianList(stage).getPlaceholder());
                    assertEquals(List.of(visibleGuardian), model.getGuardianList());
                }
                assertEquals(before, model.getAddressBook());
                assertEquals(0, storage.saveAttempts);
            } finally {
                stage.close();
            }
        });
    }

    private ModelManager linkedModel() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.linkGuardian("S-0007", "G-0003");
        model.linkGuardian("S-0008", "G-0003");
        model.addGroupAssignment(new GroupAssignment(model.findStudent("S-0007").orElseThrow().getId(),
                new GroupCode("MAT")));
        model.addGroupAssignment(new GroupAssignment(model.findStudent("S-0008").orElseThrow().getId(),
                new GroupCode("S2-MAT-A")));
        return model;
    }

    private MainWindow openWindow(Stage stage, ModelManager model, JsonAddressBookStorage storage) {
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
        window.fillInnerParts();
        stage.getScene().getRoot().resize(1000, 700);
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return window;
    }

    private static String guardianDetails(Stage stage) {
        return ((Label) stage.getScene().lookup("#guardianDetails")).getText();
    }

    private static ListView<?> guardianList(Stage stage) {
        return (ListView<?>) stage.getScene().lookup("#guardianListView");
    }

    private static String feedback(Stage stage) {
        return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private static void assertGuardianProfile(Stage stage, String phone) {
        Node details = stage.getScene().lookup("#guardianDetailsRoot");
        assertTrue(details.lookup("#detailsContent").isVisible());
        assertFalse(details.lookup("#emptyState").isVisible());
        assertEquals("G-0003", ((Label) details.lookup("#id")).getText());
        assertEquals("Anita Lim", ((Label) details.lookup("#name")).getText());
        assertEquals(phone, ((Label) details.lookup("#phone")).getText());
        VBox linkedStudents = (VBox) details.lookup("#linkedStudents");
        assertEquals(List.of("S-0007 - Mei Lin", "S-0008 - Mei Ling"), linkedStudents.getChildren().stream()
                .map(node -> ((Label) node).getText()).toList());
        Node card = guardianList(stage).lookupAll("#cardPane").stream()
                .filter(node -> node.lookup("#id") instanceof Label id && id.getText().equals("G-0003"))
                .findFirst().orElseThrow();
        assertEquals("2", ((Label) card.lookup("#linkedStudentCount")).getText());
    }

    private static void assertNoContactPayload(LogRecord record) {
        for (String contactValue : List.of("98765432", "81234567", "61234567", "Anita", "Mei",
                "G-0003", "G-0004", "G-9999", "S-0007", "gid/", "p/", "n/")) {
            assertFalse(record.getMessage().contains(contactValue), record.getMessage());
        }
        assertNull(record.getParameters());
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    /** Counts real saves and can reject a write before any bytes are changed. */
    private static class RecordingStorage extends JsonAddressBookStorage {
        private int saveAttempts;
        private IOException failure;
        private Guardian failedGuardian;

        RecordingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (failure != null) {
                failedGuardian = addressBook.findGuardian("G-0003").orElseThrow();
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }

    /** Observes existing loggers and restores their levels and handlers when the assertion scope ends. */
    private static class CommandLogRecorder extends Handler implements AutoCloseable {
        private final Logger logicLogger = Logger.getLogger("ab3.LogicManager");
        private final Logger uiLogger = Logger.getLogger("ab3.MainWindow");
        private final Level previousLogicLevel = logicLogger.getLevel();
        private final Level previousUiLevel = uiLogger.getLevel();
        private final List<LogRecord> records = new ArrayList<>();
        private final List<AddressBook> registersAtWarnings = new ArrayList<>();
        private final ModelManager model;

        CommandLogRecorder(ModelManager model) {
            this.model = model;
            setLevel(Level.ALL);
            logicLogger.addHandler(this);
            uiLogger.addHandler(this);
            logicLogger.setLevel(Level.ALL);
            uiLogger.setLevel(Level.ALL);
        }

        @Override
        public void publish(LogRecord record) {
            records.add(record);
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                registersAtWarnings.add(new AddressBook(model.getAddressBook()));
            }
        }

        List<LogRecord> warnings() {
            return records.stream().filter(record -> record.getLevel().intValue() >= Level.WARNING.intValue()).toList();
        }

        List<LogRecord> uiRecords() {
            return records.stream().filter(record -> record.getLoggerName().equals(uiLogger.getName())).toList();
        }

        @Override
        public void flush() { }

        @Override
        public void close() {
            logicLogger.removeHandler(this);
            uiLogger.removeHandler(this);
            logicLogger.setLevel(previousLogicLevel);
            uiLogger.setLevel(previousUiLevel);
        }
    }
}
