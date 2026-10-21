package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.guardian.GuardianId;

/** Removes a student's guardian relationship without deleting either record. */
public class UnlinkGuardianCommand extends Command {
    public static final String COMMAND_WORD = "unlink-guardian";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: unlink-guardian sid/STUDENT_ID";
    public static final String MESSAGE_SUCCESS =
            "Removed guardian %s from student %s. Guardian record was retained.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The guardian link was not removed.";

    private final String studentId;

    /** Retains ID text so parsing and lookup do not advance the student ID generator. */
    public UnlinkGuardianCommand(String studentId) {
        this.studentId = requireNonNull(studentId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        GuardianId guardianId;
        try {
            guardianId = model.unlinkGuardian(studentId);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, guardianId, studentId),
                studentId, CommandResult.Workspace.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnlinkGuardianCommand otherCommand
                && studentId.equals(otherCommand.studentId);
    }

    @Override
    public int hashCode() {
        return studentId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentId", studentId).toString();
    }
}
