package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Baseline-backed shown-window group-filter tests. */
class FilterGroupUiAlphaTest {
    @TempDir
    Path directory;

    @BeforeAll
    static void toolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void filteredRowsSummarySelectionEmptyStateAndRejectedTabAreConsistent() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                AddressBook before = new AddressBook(f.model.getAddressBook());
                String saved = Files.readString(f.path);
                f.submit("view-student sid/S-0003");
                f.tab("groupsTab").fire();
                f.submit("filter-students g/mat");
                assertTrue(f.tab("studentsTab").isSelected());
                assertEquals(List.of("S-0003", "S-0005"), f.ids());
                assertEquals("S-0003", f.window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Filtered by group: MAT — Mathematics", f.summary().getText());
                assertTrue(f.summary().isVisible());
                assertTrue(f.summary().isManaged());
                assertEquals("Found 2 student(s) assigned to MAT.", f.feedback());
                f.submit("filter-students g/CHI");
                assertEquals(List.of(), f.ids());
                assertEquals(null, f.window.getStudentListPanel().getSelectedStudentId());
                assertTrue(f.window.getStudentDetailsPanel().getRoot().lookup("#emptyState").isVisible());
                assertEquals("No students are assigned to CHI.", f.feedback());
                assertEquals("Filtered by group: CHI — Chinese", f.summary().getText());
                f.tab("groupsTab").fire();
                f.submit("filter-students g/S2-MAT-A");
                assertTrue(f.tab("groupsTab").isSelected());
                assertEquals(List.of(), f.ids());
                assertEquals("Filtered by group: CHI — Chinese", f.summary().getText());
                assertEquals("Group S2-MAT-A is a Class group; use the academic-level filter.", f.feedback());
                f.submit("list-students");
                assertEquals(before.getStudentList(), f.model.getStudentList());
                assertFalse(f.summary().isVisible());
                assertFalse(f.summary().isManaged());
                assertEquals(before, f.model.getAddressBook());
                assertEquals(saved, Files.readString(f.path));
            }
        });
    }

    @Test
    void recreateFilteredGroupMustNotDisplayPreviousLabel() {
        JavaFxTestUtils.runOnFxThread(() -> {
            try (Fixture f = new Fixture()) {
                f.submit("filter-students g/CHI");
                f.submit("delete-group g/CHI");
                f.submit("add-group g/CHI l/Mandarin k/Subject");
                assertEquals("Mandarin", f.model.getGroupCatalogue().findByCode("CHI").orElseThrow().getLabel());
                assertEquals(f.model.getAddressBook(), f.storage.readAddressBook().orElseThrow());
                f.tab("studentsTab").fire();
                assertTrue(f.summary().isVisible());
                assertEquals("Filtered by group: CHI — Mandarin", f.summary().getText());
            }
        });
    }

    private class Fixture implements AutoCloseable {
        private final Path path;
        private final JsonAddressBookStorage storage;
        private final ModelManager model;
        private final Stage stage = new Stage();
        private final MainWindow window;

        Fixture() throws Exception {
            path = Files.createTempFile(directory, "group-ui-", ".json");
            Files.write(path, Files.readAllBytes(Path.of("test/data/addressbook.json")));
            storage = new JsonAddressBookStorage(path);
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            var logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            window = new MainWindow(stage, logic, path);
            window.fillInnerParts();
            stage.show();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        void submit(String command) {
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText(command);
            input.fireEvent(new ActionEvent());
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        List<String> ids() {
            return model.getStudentList().stream().map(s -> s.getId().toString()).toList();
        }

        ToggleButton tab(String id) {
            return (ToggleButton) stage.getScene().lookup("#" + id);
        }

        Label summary() {
            return window.getStudentFilterSummaryLabel();
        }

        String feedback() {
            return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
        }

        @Override
        public void close() {
            stage.close();
        }
    }
}
