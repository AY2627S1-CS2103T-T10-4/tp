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
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
    void resize_longValidCode_keepsFullTextAndKindInsideRow() {
        JavaFxTestUtils.runOnFxThread(() -> {
            String code = "W".repeat(20);
            Group group = new Group(new GroupCode(code), "Wide Code", GroupKind.SUBJECT);
            GroupListPanel panel = new GroupListPanel(new GroupCatalogue(List.of(group)));
            // Resize the content directly because native window resizing waits for a later pulse.
            javafx.scene.Group host = new javafx.scene.Group(panel.getRoot());
            host.setAutoSizeChildren(false);
            Scene scene = new Scene(host, 900, 523);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            try {
                stage.show();
                for (double width : new double[] {900, 804, 900}) {
                    panel.getRoot().resize(width, 523);
                    panel.getRoot().applyCss();
                    panel.getRoot().layout();
                    ListView<?> list = (ListView<?>) panel.getRoot().lookup("#groupListView");
                    assertEquals(width, list.getWidth(), 1.0, "List must respond to resizing");
                    ListCell<?> cell = list.lookupAll(".list-cell").stream()
                            .filter(node -> node instanceof ListCell<?> candidate && !candidate.isEmpty())
                            .map(node -> (ListCell<?>) node)
                            .findFirst()
                            .orElseThrow();
                    Node row = cell.getGraphic();
                    Label codeLabel = (Label) row.lookup("#groupCode");
                    Bounds codeBounds = assertRenderedTextFits(codeLabel, code);
                    Bounds labelBounds = assertRenderedTextFits((Label) row.lookup("#groupLabel"), "Wide Code");
                    Bounds kindBounds = assertRenderedTextFits((Label) row.lookup("#groupKind"), "Subject");

                    assertEquals(120, codeLabel.getWidth(), 1.0, "Code column must not widen");
                    assertContains(row.localToScene(row.getLayoutBounds()), codeBounds);
                    assertContains(cell.localToScene(cell.getLayoutBounds()), codeBounds);
                    assertTrue(codeBounds.getMaxX() <= labelBounds.getMinX(), "Code overlaps label");
                    assertTrue(labelBounds.getMaxX() <= kindBounds.getMinX(), "Label overlaps kind");
                    Node viewport = list.lookup(".clipped-container");
                    Bounds viewportBounds = viewport.localToScene(viewport.getClip().getBoundsInParent());
                    assertContains(viewportBounds, codeBounds);
                    assertContains(viewportBounds, kindBounds);
                }
            } finally {
                stage.close();
            }
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

    private static Bounds assertRenderedTextFits(Label label, String expected) {
        Text renderedText = (Text) label.lookup(".text");
        assertEquals(expected, renderedText.getText(), "Rendered text must not be ellipsized");
        // Layout bounds retain the full text extent even when a clip hides part of it.
        Bounds textBounds = renderedText.localToScene(renderedText.getLayoutBounds());
        assertTrue(textBounds.getWidth() > 0 && textBounds.getHeight() > 0);
        assertContains(label.localToScene(label.getLayoutBounds()), textBounds);
        for (Node node = renderedText; node != null; node = node.getParent()) {
            if (node.getClip() != null) {
                assertContains(node.localToScene(node.getClip().getBoundsInParent()), textBounds);
            }
        }
        return textBounds;
    }

    private static void assertContains(Bounds outer, Bounds inner) {
        // Allow subpixel layout rounding without depending on platform font metrics.
        assertTrue(inner.getMinX() >= outer.getMinX() - 1.0 && inner.getMaxX() <= outer.getMaxX() + 1.0
                && inner.getMinY() >= outer.getMinY() - 1.0 && inner.getMaxY() <= outer.getMaxY() + 1.0,
                "Rendered text " + inner + " extends outside " + outer);
    }

    private static String displayedRow(Node row) {
        return ((Label) row.lookup("#groupCode")).getText() + " | "
                + ((Label) row.lookup("#groupLabel")).getText() + " | "
                + ((Label) row.lookup("#groupKind")).getText();
    }
}
