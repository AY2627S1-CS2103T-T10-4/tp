package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollToEvent;
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
    void constructor_longValidLabel_keepsKindInsideListViewport() {
        JavaFxTestUtils.runOnFxThread(() -> {
            String longLabel = "W".repeat(20) + " " + "W".repeat(19) + " "
                    + "W".repeat(19) + " " + "W".repeat(19);
            assertEquals(80, longLabel.length());
            Group group = new Group(new GroupCode("LONG-TEST"), longLabel, GroupKind.SUBJECT);
            GroupListPanel panel = new GroupListPanel(new GroupCatalogue(List.of(group)));
            Scene scene = new Scene(panel.getRoot(), 804, 523);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");
            ListCell<?> cell = list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> candidate && !candidate.isEmpty())
                    .map(node -> (ListCell<?>) node)
                    .findFirst()
                    .orElseThrow();
            Label displayedLabel = (Label) cell.getGraphic().lookup("#groupLabel");
            Label displayedKind = (Label) cell.getGraphic().lookup("#groupKind");
            Bounds listBounds = list.localToScene(list.getBoundsInLocal());
            Bounds kindBounds = displayedKind.localToScene(displayedKind.getBoundsInLocal());

            assertEquals(longLabel, displayedLabel.getText());
            assertTrue(kindBounds.getMaxX() <= listBounds.getMaxX());
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

    @Test
    void selectGroupCode_existingGroup_selectsAndRequestsScroll() {
        JavaFxTestUtils.runOnFxThread(() -> {
            List<Group> groups = new ArrayList<>(ConfiguredGroups.getCatalogue().getGroups());
            groups.add(new Group(new GroupCode("RP-AA"), "Repro Alpha", GroupKind.SUBJECT));
            groups.add(new Group(new GroupCode("RP-BB"), "Repro Beta", GroupKind.SUBJECT));
            Group target = new Group(new GroupCode("RP-CC"), "Repro Gamma", GroupKind.SUBJECT);
            groups.add(target);
            GroupListPanel panel = new GroupListPanel(new GroupCatalogue(groups));
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();
            int[] scrollTargets = {-1};
            panel.getRoot().addEventFilter(ScrollToEvent.scrollToTopIndex(), event ->
                    scrollTargets[0] = event.getScrollTarget());

            panel.selectGroupCode("RP-CC");

            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");
            assertEquals(target, list.getSelectionModel().getSelectedItem());
            assertEquals(groups.indexOf(target), scrollTargets[0]);
        });
    }

    private static String displayedRow(Node row) {
        return ((Label) row.lookup("#groupCode")).getText() + " | "
                + ((Label) row.lookup("#groupLabel")).getText() + " | "
                + ((Label) row.lookup("#groupKind")).getText();
    }
}
