package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;

/** Adds a student record. */
public class AddStudentCommand extends Command {
    public static final String COMMAND_WORD = "add-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: add-student n/NAME al/ACADEMIC_LEVEL";
    public static final String DUPLICATE_FORMAT = "A student named %s at %s already exists.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Added student %s: %s (%s).";
    private final StudentName name;
    private final AcademicLevel level;

    public AddStudentCommand(StudentName name, AcademicLevel level) { this.name = name; this.level = level; }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Student candidate = new Student(name, level);
        if (model.hasStudent(candidate)) {
            throw new CommandException(String.format(DUPLICATE_FORMAT, name, level));
        }
        model.addStudent(candidate);
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, candidate.getId(), name, level),
                false, false, candidate.getId().value);
    }
}
