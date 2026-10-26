package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

class GroupListPanelTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void constructor_configuredGroups_rendersOrderedCodeLabelAndKind() {
        JavaFxTestUtils.runOnFxThread(() -> {
            GroupListPanel panel = new GroupListPanel(ConfiguredGroups.getCatalogue());
            Scene scene = new Scene(panel.getRoot(), 900, 500);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");

            List<String> rows = list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty())
                    .map(node -> (ListCell<?>) node)
                    .sorted(Comparator.comparingInt(ListCell::getIndex))
                    .map(cell -> displayedRow(cell.getGraphic()))
                    .toList();

            assertEquals(List.of("ENG | English | Subject", "MAT | Mathematics | Subject",
                    "SCI | Science | Subject", "CHI | Chinese | Subject",
                    "S2-MAT-A | Secondary 2 Mathematics A | Class"), rows);
            assertEquals("5 groups", ((Label) panel.getRoot().lookup("#groupCountLabel")).getText());
            assertThrows(UnsupportedOperationException.class, () -> list.getItems().clear());
            assertTrue(list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && cell.isEmpty())
                    .allMatch(node -> ((ListCell<?>) node).getGraphic() == null));
        });
    }

    @Test
    void constructor_emptyCatalogue_displaysZeroAndEmptyState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            GroupListPanel panel = new GroupListPanel(new GroupCatalogue(List.of()));
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");

            assertTrue(list.getItems().isEmpty());
            assertEquals("No configured groups.", ((Label) list.getPlaceholder()).getText());
            assertEquals("0 groups", ((Label) panel.getRoot().lookup("#groupCountLabel")).getText());
        });
    }

    @Test
    void refreshAndSelectGroupCode_updatesRenderedCatalogueAndSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            GroupListPanel panel = new GroupListPanel(ConfiguredGroups.getCatalogue());
            Group physics = new Group(new GroupCode("PHY"), "Physics", GroupKind.SUBJECT);
            GroupCatalogue updated = new GroupCatalogue(List.of(physics));

            panel.refresh(updated);
            panel.selectGroupCode("PHY");

            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");
            assertEquals(List.of(physics), list.getItems());
            assertEquals(physics, list.getSelectionModel().getSelectedItem());
            assertEquals("1 groups", ((Label) panel.getRoot().lookup("#groupCountLabel")).getText());
        });
    }

    private static String displayedRow(Node row) {
        return ((Label) row.lookup("#groupCode")).getText() + " | "
                + ((Label) row.lookup("#groupLabel")).getText() + " | "
                + ((Label) row.lookup("#groupKind")).getText();
    }
}
