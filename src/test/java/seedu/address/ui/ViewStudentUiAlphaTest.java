package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
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
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Shown-window profile probes through real command input and temporary storage. */
class ViewStudentUiAlphaTest {
    @TempDir
    Path directory;

    @BeforeAll
    static void startToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void openingEveryStudentThroughEnterAndRunRendersExactProfileAndNeverSaves() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                AddressBook baseline = new AddressBook(f.model.getAddressBook());
                String bytes = Files.readString(f.path);
                int i = 0;
                for (var student : baseline.getStudentList()) {
                    f.submit("view-student sid/" + student.getId(), i++ % 2 == 0);
                    f.assertProfile(student.getId().toString());
                    assertEquals("Opened profile for " + student.getId() + ".", f.feedback());
                    assertEquals("", f.input().getText());
                    assertFalse(f.input().getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                }
                assertEquals(baseline, f.model.getAddressBook());
                assertEquals(bytes, Files.readString(f.path));
                assertEquals(0, f.storage.attempts);
            }
        });
    }

    @Test
    void hiddenProfileOpensOnOtherTabsAndRemainsSelectedOnReturningToStudents() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                for (String tab : List.of("groupsTab", "guardiansTab")) {
                    f.submit("find-student n/No Match", false);
                    assertTrue(f.model.getStudentList().isEmpty());
                    ((ToggleButton) f.stage.getScene().lookup("#" + tab)).fire();
                    f.submit("view-student sid/S-0003", true);
                    assertTrue(((ToggleButton) f.stage.getScene().lookup("#" + tab)).isSelected());
                    assertEquals(f.model.getAddressBook().getStudentList(), f.model.getStudentList());
                    ((ToggleButton) f.stage.getScene().lookup("#studentsTab")).fire();
                    f.assertProfile("S-0003");
                    assertTrue(f.window.getStudentDetailsPanel().getRoot().isVisible());
                }
                assertEquals(0, f.storage.attempts);
            }
        });
    }

    @Test
    void rejectedCommandsPreserveProfileAndEmptySelectionThenRecoverOnValidInput() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                f.submit("view-student sid/S-0004", false);
                for (String bad : List.of("view-student sid/S-9999", "view-student sid/S-0003 sid/S-0004")) {
                    f.submit(bad, true);
                    f.assertProfile("S-0004");
                    assertEquals(bad, f.input().getText());
                    assertTrue(f.input().getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                }
                f.submit("find-student n/No Match", false);
                f.submit("view-student sid/S-9999", false);
                VBox empty = (VBox) f.window.getStudentDetailsPanel().getRoot().lookup("#emptyState");
                assertTrue(empty.isVisible());
                assertTrue(empty.isManaged());
                assertEquals(null, f.window.getStudentListPanel().getSelectedStudentId());
                f.submit("view-student sid/S-0003", true);
                f.assertProfile("S-0003");
                assertEquals(0, f.storage.attempts);
            }
        });
    }

    @Test
    void selectedProfileRefreshesAfterGuardianEditsRollbackUnlinkAndDeletion() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                f.storage.allowWrites = true;
                f.submit("view-student sid/S-0003", false);
                f.submit("edit-student sid/S-0003 p/60001111", false);
                f.submit("edit-guardian gid/G-0001 p/90002222", false);
                f.assertProfile("S-0003");
                assertEquals("+65 60001111", f.label("studentPhone"));
                assertEquals("Anita Lim\n+65 90002222", f.label("guardianDetails"));
                String saved = Files.readString(f.path);
                f.storage.failure = new IOException("Injected save error");
                f.submit("edit-guardian gid/G-0001 p/90003333", false);
                assertEquals("Could not save changes. Guardian G-0001 was not updated.", f.feedback());
                assertEquals(saved, Files.readString(f.path));
                assertEquals("Anita Lim\n+65 90002222", f.label("guardianDetails"));
                f.assertProfile("S-0003");
                f.storage.failure = null;
                f.submit("edit-guardian gid/G-0001 p/90003333", false);
                assertEquals("Anita Lim\n+65 90003333", f.label("guardianDetails"));
                f.submit("unlink-guardian sid/S-0003", false);
                assertEquals("Guardian: none", f.label("guardianDetails"));
                f.submit("unassign-group sid/S-0003 g/MAT", false);
                f.submit("unassign-group sid/S-0003 g/S2-MAT-A", false);
                assertEquals(List.of("No groups assigned."), f.assignmentText());
                f.submit("delete-student sid/S-0003", false);
                assertTrue(f.window.getStudentDetailsPanel().getRoot().lookup("#emptyState").isVisible());
                f.submit("view-student sid/S-0004", false);
                f.assertProfile("S-0004");
                assertEquals("Phone: none", f.label("studentPhone"));
                assertEquals("Anita Lim\n+65 81234567", f.label("guardianDetails"));
                assertEquals(f.model.getAddressBook(), f.storage.readAddressBook().orElseThrow());
            }
        });
    }

    @Test
    void longNameAndManyAssignmentsHaveScrollableProfileAtMinimumSize() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                f.storage.allowWrites = true;
                f.submit("add-student n/" + "W".repeat(80) + " al/Sec 1", false);
                String sid = f.window.getStudentListPanel().getSelectedStudentId();
                assertTrue(sid.matches("S-[0-9]{4,}"));
                for (int i = 0; i < 12; i++) {
                    String code = "P" + i;
                    f.submit("add-group g/" + code + " l/" + "Long label ".repeat(6).trim() + " k/Subject", false);
                    f.submit("assign-group sid/" + sid + " g/" + code, false);
                }
                f.submit("view-student sid/" + sid, true);
                ((ToggleButton) f.stage.getScene().lookup("#studentsTab")).fire();
                f.stage.setWidth(820);
                f.stage.setHeight(560);
                f.layout();
                f.assertProfile(sid);
                ScrollPane scroll = (ScrollPane) f.window.getStudentDetailsPanel().getRoot().lookup("#profileScrollPane");
                assertTrue(scroll.getViewportBounds().getWidth() > 0);
                assertTrue(scroll.getViewportBounds().getHeight() > 0);
                assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
                assertEquals(12, f.assignmentText().size());
                scroll.setVvalue(1);
                f.layout();
                assertEquals(1, scroll.getVvalue());
                assertEquals(f.model.getAddressBook(), f.storage.readAddressBook().orElseThrow());
            }
        });
    }

    private class Fixture implements AutoCloseable {
        private final Path path;
        private final Storage storage;
        private final ModelManager model;
        private final Stage stage = new Stage();
        private final MainWindow window;

        Fixture() throws Exception {
            path = Files.createTempFile(directory, "ui-", ".json");
            Files.write(path, Files.readAllBytes(Path.of("test/data/addressbook.json")));
            storage = new Storage(path);
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            var logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            window = new MainWindow(stage, logic, path);
            window.fillInnerParts();
            stage.show();
            layout();
        }

        void layout() {
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        TextField input() {
            return (TextField) stage.getScene().lookup("#commandTextField");
        }

        void submit(String command, boolean runButton) {
            input().setText(command);
            if (runButton) {
                Button run = (Button) stage.getScene().getRoot().lookupAll(".button").stream()
                        .filter(n -> n instanceof Button && ((Button) n).getText().equals("Run"))
                        .findFirst().orElseThrow();
                run.fire();
            } else {
                input().fireEvent(new ActionEvent());
            }
            layout();
        }

        String label(String id) {
            return ((Label) window.getStudentDetailsPanel().getRoot().lookup("#" + id)).getText();
        }

        String feedback() {
            return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
        }

        List<String> assignmentText() {
            VBox box = (VBox) window.getStudentDetailsPanel().getRoot().lookup("#assignments");
            return box.getChildren().stream().map(n -> ((Label) n).getText()).toList();
        }

        void assertProfile(String sid) {
            var student = model.findStudent(sid).orElseThrow();
            assertEquals(sid, window.getStudentListPanel().getSelectedStudentId());
            assertEquals(sid, label("id"));
            assertEquals(student.getName().toString(), label("name"));
            assertEquals(student.getAcademicLevel().toString(), label("academicLevel"));
            assertEquals(student.getPhone().map(Object::toString).orElse("Phone: none"), label("studentPhone"));
            var guardian = model.getLinkedGuardian(sid);
            assertEquals(guardian.map(g -> g.getName() + "\n" + g.getPhone()).orElse("Guardian: none"),
                    label("guardianDetails"));
            var groups = model.getAssignedGroups(sid);
            assertEquals(groups.isEmpty() ? List.of("No groups assigned.")
                    : groups.stream().map(g -> g.getCode() + " — " + g.getLabel()).toList(), assignmentText());
            assertTrue(window.getStudentDetailsPanel().getRoot().lookup("#detailsContent").isVisible());
            assertFalse(window.getStudentDetailsPanel().getRoot().lookup("#emptyState").isVisible());
        }

        @Override
        public void close() {
            stage.close();
        }
    }

    private static class Storage extends JsonAddressBookStorage {
        private boolean allowWrites;
        private IOException failure;
        private int attempts;

        Storage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook register) throws IOException {
            attempts++;
            if (!allowWrites) {
                throw new AssertionError("A profile opening unexpectedly saved data");
            }
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(register);
        }
    }
}
