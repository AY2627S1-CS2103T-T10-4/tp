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
        GUARDIANS,
        GROUPS,
        STUDENTS
    }

    private final String feedbackToUser;
    private final boolean showHelp;
    private final boolean exit;
    private final String selectedStudentId;
    private final Workspace workspace;
    private final boolean clearStudentSelection;
    private final String studentIdToClearSelection;
    private final String helpCommand;
    private final String selectedGroupCode;

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
        this(feedbackToUser, showHelp, exit, selectedStudentId, Workspace.UNCHANGED, false, null);
    }

    /**
     * Constructs a result with optional unconditional selection clearing, retaining the workspace.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param showHelp Whether to show the help window.
     * @param exit Whether to close the application.
     * @param selectedStudentId The stable ID to select, or null to retain the previous selection.
     * @param clearStudentSelection Whether to clear selection, taking precedence over requested selection.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            boolean clearStudentSelection) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, clearStudentSelection, null);
    }

    /**
     * Constructs a result with conditional selection clearing, retaining the workspace.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param showHelp Whether to show the help window.
     * @param exit Whether to close the application.
     * @param selectedStudentId The stable ID to select, or null to retain the previous selection.
     * @param clearStudentSelection Whether to request selection clearing.
     * @param studentIdToClearSelection The ID that must match the pre-command selection, or null for any selection.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            boolean clearStudentSelection, String studentIdToClearSelection) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, Workspace.UNCHANGED,
                clearStudentSelection, studentIdToClearSelection);
    }

    /**
     * Constructs a workspace request without changing student selection.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param workspace The non-null workspace request.
     */
    public CommandResult(String feedbackToUser, Workspace workspace) {
        this(feedbackToUser, false, false, null, workspace, false, null);
    }

    /**
     * Requests a workspace and optional stable-ID student selection together.
     *
     * @param feedbackToUser The non-null feedback to display.
     * @param selectedStudentId The stable ID to select, or null to leave selection unchanged.
     * @param workspace The non-null workspace request.
     */
    public CommandResult(String feedbackToUser, String selectedStudentId, Workspace workspace) {
        this(feedbackToUser, false, false, selectedStudentId, workspace, false, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            Workspace workspace, boolean clearStudentSelection, String studentIdToClearSelection) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, workspace, clearStudentSelection,
                studentIdToClearSelection, null, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            Workspace workspace, boolean clearStudentSelection, String studentIdToClearSelection, String helpCommand,
            String selectedGroupCode) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedStudentId = selectedStudentId;
        this.workspace = requireNonNull(workspace);
        this.clearStudentSelection = clearStudentSelection;
        this.studentIdToClearSelection = studentIdToClearSelection;
        this.helpCommand = helpCommand;
        this.selectedGroupCode = selectedGroupCode;
    }

    /** Retired Person-list selection constructor. The requested index is intentionally ignored. */
    @Deprecated
    public CommandResult(String feedbackToUser, Index ignoredPersonIndex) {
        this(feedbackToUser);
    }

    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Requests help for a command, or the overview when the command is null. */
    public static CommandResult forHelp(String feedback, String command) {
        return new CommandResult(feedback, true, false, null, Workspace.UNCHANGED, false, null, command, null);
    }

    /** Requests the Groups workspace and selects one canonical group code. */
    public static CommandResult forGroup(String feedback, String groupCode) {
        return new CommandResult(feedback, false, false, null, Workspace.GROUPS, false, null, null,
                requireNonNull(groupCode));
    }

    /** Returns the requested help command, or null for the overview. */
    public String getHelpCommand() {
        return helpCommand;
    }

    /** Returns the group code to select, or null when no group selection was requested. */
    public String getSelectedGroupCode() {
        return selectedGroupCode;
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
     * @return The stable ID to select, or null for no selection request; clearing metadata is applied separately.
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

    /**
     * Returns whether selection clearing is requested after successful execution and persistence.
     *
     * @return Whether to apply the optional ID condition from {@link #getStudentIdToClearSelection()}.
     */
    public boolean shouldClearStudentSelection() {
        return clearStudentSelection;
    }

    /**
     * Returns the stable ID whose pre-command selection should be cleared.
     *
     * @return The conditional ID, or null to clear any selection when clearing is requested.
     */
    public String getStudentIdToClearSelection() {
        return studentIdToClearSelection;
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
                && workspace == result.workspace
                && clearStudentSelection == result.clearStudentSelection
                && Objects.equals(studentIdToClearSelection, result.studentIdToClearSelection)
                && Objects.equals(helpCommand, result.helpCommand)
                && Objects.equals(selectedGroupCode, result.selectedGroupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedStudentId, workspace, clearStudentSelection,
                studentIdToClearSelection, helpCommand, selectedGroupCode);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp).add("exit", exit).add("selectedStudentId", selectedStudentId)
                .add("workspace", workspace)
                .add("clearStudentSelection", clearStudentSelection)
                .add("studentIdToClearSelection", studentIdToClearSelection).add("helpCommand", helpCommand)
                .add("selectedGroupCode", selectedGroupCode).toString();
    }
}
