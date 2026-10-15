package seedu.address.logic.commands;

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

    /** Creates a command for the supplied student ID. */
    public DeleteStudentCommand(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Student student = model.getStudentList().stream()
                .filter(candidate -> candidate.getId().value.equals(studentId))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentId)));
        model.deleteStudent(student.getId());
        String feedback = String.format(MESSAGE_SUCCESS, studentId, student.getName());
        return new CommandResult(feedback, false, false, null, true, studentId);
    }

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
