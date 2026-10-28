package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;

/** Displays the configured group codes, labels and kinds in catalogue order. */
public class GroupListPanel extends UiPart<Region> {
    private static final String FXML = "GroupListPanel.fxml";

    @FXML
    private Label groupCountLabel;
    @FXML
    private ListView<Group> groupListView;

    /**
     * Creates a read-only view of the supplied immutable catalogue in its configured order.
     *
     * @param catalogue Validated catalogue to render.
     * @throws NullPointerException If catalogue is null.
     */
    public GroupListPanel(GroupCatalogue catalogue) {
        super(FXML);
        requireNonNull(catalogue);
        groupListView.setPlaceholder(new Label("No configured groups."));
        groupListView.setCellFactory(unused -> new GroupListViewCell());
        refresh(catalogue);
    }

    /** Replaces the rendered catalogue after a successful group mutation or rollback. */
    public void refresh(GroupCatalogue catalogue) {
        requireNonNull(catalogue);
        groupCountLabel.setText(catalogue.size() + " groups");
        groupListView.setItems(FXCollections.unmodifiableObservableList(
                FXCollections.observableArrayList(catalogue.getGroups())));
    }

    /** Selects and scrolls to a group by canonical code when present. */
    public void selectGroupCode(String code) {
        if (code == null) {
            return;
        }
        for (int index = 0; index < groupListView.getItems().size(); index++) {
            if (groupListView.getItems().get(index).getCode().getValue().equals(code)) {
                groupListView.getSelectionModel().select(index);
                groupListView.scrollTo(index);
                return;
            }
        }
    }

    /** Renders each immutable group using the existing theme's text styles. */
    private static class GroupListViewCell extends ListCell<Group> {
        @Override
        protected void updateItem(Group group, boolean empty) {
            super.updateItem(group, empty);
            setText(null);
            if (empty || group == null) {
                setGraphic(null);
                return;
            }

            Label code = new Label(group.getCode().getValue());
            code.setId("groupCode");
            code.getStyleClass().add("contact-phone");
            code.setMinWidth(120);
            code.setPrefWidth(120);
            Label label = new Label(group.getLabel());
            label.setId("groupLabel");
            label.getStyleClass().add("detail-value");
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            String kindText = group.getKind().toDisplayString();
            Label kind = new Label(kindText);
            kind.setId("groupKind");
            kind.getStyleClass().add("helper-label");
            kind.setMinWidth(94);
            HBox row = new HBox(code, label, kind);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 18, 12, 18));
            setGraphic(row);
        }
    }
}
