package seedu.address.logic.commands;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** Deletes a student record by its permanent ID. */
public class DeleteStudentCommand extends Command {
    public static final String COMMAND_WORD = "delete-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: delete-student sid/STUDENT_ID";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student %s was not found.";
    public static final String MESSAGE_SUCCESS = "Deleted student %s: %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Student %s was not deleted.";

    private final StudentId studentId;

    /** Creates a command for the supplied student ID. */
    public DeleteStudentCommand(StudentId studentId) {
        this.studentId = studentId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Student student = model.getStudentList().stream()
                .filter(candidate -> candidate.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentId)));
        model.deleteStudent(studentId);
        String feedback = String.format(MESSAGE_SUCCESS, studentId, student.getName());
        return new CommandResult(feedback, false, false, null, true);
    }

    public StudentId getStudentId() {
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
