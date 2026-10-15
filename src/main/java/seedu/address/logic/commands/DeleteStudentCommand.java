package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.Student;

/** Deletes a student record by its permanent ID. */
public class DeleteStudentCommand extends Command {
    public static final String COMMAND_WORD = "delete-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: delete-student sid/STUDENT_ID";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student %s was not found.";
    public static final String MESSAGE_SUCCESS = "Deleted student %s: %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Student %s was not deleted.";

    private final String studentId;

    /**
     * Retains the parsed ID text without allocating or reserving a StudentId.
     *
     * @param studentId The stable ID text validated by the parser.
     * @throws NullPointerException If {@code studentId} is null.
     */
    public DeleteStudentCommand(String studentId) {
        this.studentId = requireNonNull(studentId);
    }

    /**
     * Resolves the complete register and removes the student and their assignments by the existing ID.
     * Other students, assignments and guardian records are preserved by the model.
     *
     * @param model The authoritative register, independent of its filtered Student view.
     * @return Success feedback and a request to clear selection only if the deleted student was selected.
     * @throws CommandException If the student does not exist.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Student student = model.findStudent(studentId)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentId)));
        model.deleteStudent(student.getId());
        String feedback = String.format(MESSAGE_SUCCESS, studentId, student.getName());
        return new CommandResult(feedback, false, false, null, true, studentId);
    }

    /**
     * Returns the ID used in the delete-specific save-failure message.
     *
     * @return The supplied stable student ID text.
     */
    public String getStudentId() {
        return studentId;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteStudentCommand command
                && studentId.equals(command.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
