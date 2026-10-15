package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** Updates a student's academic level. */
public class EditStudentCommand extends Command {
    public static final String COMMAND_WORD = "edit-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: edit-student sid/STUDENT_ID "
            + "al/ACADEMIC_LEVEL";
    public static final String MESSAGE_NOT_FOUND_FORMAT = "Student %s does not exist.";
    public static final String MESSAGE_DUPLICATE_FORMAT = "Cannot update %s: a student named %s at %s "
            + "already exists.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Updated student %s: academic level is now %s.";

    private final String id;
    private final AcademicLevel level;

    /** Creates an edit command for the supplied student ID and new academic level. */
    public EditStudentCommand(String id, AcademicLevel level) {
        this.id = id;
        this.level = level;
    }

    /** Creates an edit command from a previously validated student ID. */
    public EditStudentCommand(StudentId id, AcademicLevel level) {
        this(id.value, level);
    }

    /** Returns the ID used in a persistence failure message. */
    public String getStudentId() {
        return id;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Student current = model.getStudentList().stream()
                .filter(student -> student.getId().value.equals(id))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND_FORMAT, id)));
        StudentId matchedId = current.getId();
        Student updated = new Student(matchedId, current.getName(), level);
        if (model.getStudentList().stream().anyMatch(student -> !student.getId().value.equals(id)
                && student.sameIdentity(updated))) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_FORMAT, id, updated.getName(), level));
        }
        model.updateStudent(updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, id, level), false, false, id);
    }
}
