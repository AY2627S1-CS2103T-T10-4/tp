package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;
    private final String selectedStudentId;

    /** The visible row to select, or null when selection should be preserved. */
    private final Index selectedIndex;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit,
            Index selectedIndex, String selectedStudentId) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedIndex = selectedIndex;
        this.selectedStudentId = selectedStudentId;
    }

    /**
     * Constructs a result requesting selection of a row in the current filtered list.
     * The command must ensure the index is valid in the list returned after execution.
     */
    public CommandResult(String feedbackToUser, Index selectedIndex) {
        this(feedbackToUser, false, false, requireNonNull(selectedIndex), null);
    }

    /**
     * Constructs a result and optionally identifies the student to select in the UI.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String selectedStudentId) {
        this(feedbackToUser, showHelp, exit, null, selectedStudentId);
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
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
     * Returns the visible index to select, or an empty optional when no selection is requested.
     */
    public Optional<Index> getSelectedIndex() {
        return Optional.ofNullable(selectedIndex);
    }

    public String getSelectedStudentId() {
        return selectedStudentId;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(selectedIndex, otherCommandResult.selectedIndex)
                && Objects.equals(selectedStudentId, otherCommandResult.selectedStudentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit,
                selectedIndex == null ? null : selectedIndex.getZeroBased(), selectedStudentId);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("selectedIndex", selectedIndex);
        if (selectedStudentId != null) {
            builder.add("selectedStudentId", selectedStudentId);
        }
        return builder.toString();
    }

}
