package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Immutable feedback and UI requests produced by a command.
 * Logic returns a mutating command's result to the UI only after its required save succeeds.
 */
public class CommandResult {
    /**
     * The workspace requested by a command, or no change to the current workspace.
     */
    public enum Workspace {
        UNCHANGED,
        GROUPS,
        STUDENTS
    }

    private final String feedbackToUser;
    private final boolean showHelp;
    private final boolean exit;
    private final String selectedStudentId;
    private final Workspace workspace;

    /**
     * Constructs feedback and help/exit requests without changing selection or workspace.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param showHelp Whether to show the help window.
     * @param exit Whether to close the application.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null);
    }

    /**
     * Constructs a result with optional student selection, leaving the workspace unchanged.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param showHelp Whether to show the help window.
     * @param exit Whether to close the application.
     * @param selectedStudentId The stable ID to select, or null to leave selection unchanged.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, Workspace.UNCHANGED);
    }

    /**
     * Constructs a workspace request without changing student selection.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param workspace The non-null workspace request.
     */
    public CommandResult(String feedbackToUser, Workspace workspace) {
        this(feedbackToUser, false, false, null, workspace);
    }

    /**
     * Requests a workspace and optional stable-ID student selection together.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param selectedStudentId The stable ID to select, or null to leave selection unchanged.
     * @param workspace The non-null workspace request.
     */
    public CommandResult(String feedbackToUser, String selectedStudentId, Workspace workspace) {
        this(feedbackToUser, false, false, selectedStudentId, workspace);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            Workspace workspace) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedStudentId = selectedStudentId;
        this.workspace = requireNonNull(workspace);
    }

    /** Retired Person-list selection constructor. The requested index is intentionally ignored. */
    @Deprecated
    public CommandResult(String feedbackToUser, Index ignoredPersonIndex) {
        this(feedbackToUser);
    }

    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns the requested student selection.
     *
     * @return The stable ID to select, or null when selection should remain unchanged.
     */
    public String getSelectedStudentId() {
        return selectedStudentId;
    }

    /**
     * Returns the requested workspace.
     *
     * @return The workspace to display, or {@link Workspace#UNCHANGED} to retain the current workspace.
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    /** Retired Person-list selection API; student selection uses stable IDs. */
    @Deprecated
    public Optional<Index> getSelectedIndex() {
        return Optional.empty();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CommandResult result
                && feedbackToUser.equals(result.feedbackToUser) && showHelp == result.showHelp
                && exit == result.exit && Objects.equals(selectedStudentId, result.selectedStudentId)
                && workspace == result.workspace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedStudentId, workspace);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp).add("exit", exit).add("selectedStudentId", selectedStudentId)
                .add("workspace", workspace).toString();
    }
}
