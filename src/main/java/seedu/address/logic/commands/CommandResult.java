package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/** Result returned after executing a command. */
public class CommandResult {
    private final String feedbackToUser;
    private final boolean showHelp;
    private final boolean exit;
    private final String selectedStudentId;
    private final boolean clearStudentSelection;
    private final String studentIdToClearSelection;

    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null);
    }

    /** Constructs a result that requests selection of the student with this stable ID. */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, false);
    }

    /** Constructs a result that asks the UI to clear the current student selection. */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            boolean clearStudentSelection) {
        this(feedbackToUser, showHelp, exit, selectedStudentId, clearStudentSelection, null);
    }

    /** Constructs a result that clears selection only when it belongs to the specified student. */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId,
            boolean clearStudentSelection, String studentIdToClearSelection) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedStudentId = selectedStudentId;
        this.clearStudentSelection = clearStudentSelection;
        this.studentIdToClearSelection = studentIdToClearSelection;
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

    public String getSelectedStudentId() {
        return selectedStudentId;
    }

    public boolean shouldClearStudentSelection() {
        return clearStudentSelection;
    }

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
                && clearStudentSelection == result.clearStudentSelection
                && Objects.equals(studentIdToClearSelection, result.studentIdToClearSelection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedStudentId, clearStudentSelection,
                studentIdToClearSelection);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp).add("exit", exit).add("selectedStudentId", selectedStudentId)
                .add("clearStudentSelection", clearStudentSelection)
                .add("studentIdToClearSelection", studentIdToClearSelection).toString();
    }
}
