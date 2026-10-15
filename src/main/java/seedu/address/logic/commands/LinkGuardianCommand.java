package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_GUARDIAN;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_STUDENT;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;

/** Links a student to one existing guardian, without replacing an existing relationship. */
public class LinkGuardianCommand extends Command {
    public static final String COMMAND_WORD = "link-guardian";
    public static final String MESSAGE_USAGE =
            "Invalid command format. Use: link-guardian sid/STUDENT_ID gid/GUARDIAN_ID";
    public static final String MESSAGE_SUCCESS = "Linked student %s (%s) to guardian %s (%s, %s).";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The guardian link was not created.";

    private final String studentId;
    private final String guardianId;

    /** Retains validated ID text so parsing and lookup do not advance either ID generator. */
    public LinkGuardianCommand(String studentId, String guardianId) {
        requireAllNonNull(studentId, guardianId);
        this.studentId = studentId;
        this.guardianId = guardianId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Student student = model.findStudent(studentId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_UNKNOWN_STUDENT, studentId)));
        Guardian guardian = model.findGuardian(guardianId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_UNKNOWN_GUARDIAN, guardianId)));
        try {
            model.linkGuardian(studentId, guardianId);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentId, student.getName(),
                guardianId, guardian.getName(), guardian.getPhone()), studentId, CommandResult.Workspace.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LinkGuardianCommand otherCommand
                && studentId.equals(otherCommand.studentId) && guardianId.equals(otherCommand.guardianId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, guardianId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentId", studentId).add("guardianId", guardianId).toString();
    }
}
