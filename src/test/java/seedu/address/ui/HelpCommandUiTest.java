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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class HelpCommandUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_specificGeneralAndInvalidHelp_updatesExistingWindowWithoutSaving() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            Path data = temporaryFolder.resolve("students.json");
            LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                    new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            MainWindow main = new MainWindow(stage, logic, data);
            try {
                main.fillInnerParts();
                submit(stage, "help ADD-STUDENT");
                Stage help = (Stage) Window.getWindows().stream()
                        .filter(window -> window instanceof Stage other
                                && other.getTitle().equals("Mentora Help: add-student"))
                        .findFirst().orElseThrow();
                TextArea content = (TextArea) help.getScene().lookup("#helpContent");
                assertTrue(content.getText().contains("Syntax: add-student"));
                assertFalse(content.isEditable());
                assertTrue(content.isWrapText());
                assertEquals("Showing help for add-student.", feedback(stage));
                submit(stage, "help exit");
                assertEquals("Mentora Help: exit", help.getTitle());
                assertTrue(content.getText().startsWith("Syntax: exit"));
                submit(stage, "help unknown");
                assertEquals("No help is available for \"unknown\". Run help to see all commands.", feedback(stage));
                assertEquals("Mentora Help: exit", help.getTitle());
                submit(stage, "help");
                assertTrue(content.getText().contains("Syntax: filter-students"));
                assertEquals("Mentora Help", help.getTitle());
                Label guide = (Label) help.getScene().lookup("#helpMessage");
                assertTrue(guide.getText().contains("ay2627s1-cs2103t-t10-4.github.io/tp/UserGuide.html"));
                submit(stage, "help unlink-guardian");
                main.handleHelp();
                assertEquals("Mentora Help", help.getTitle());
                assertFalse(Files.exists(data));
            } finally {
                List.copyOf(Window.getWindows()).stream()
                        .filter(window -> window instanceof Stage other && other.getTitle().startsWith("Mentora Help"))
                        .forEach(Window::hide);
                stage.close();
            }
        });
    }

    private static String feedback(Stage stage) {
        return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private static void submit(Stage stage, String text) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(text);
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
