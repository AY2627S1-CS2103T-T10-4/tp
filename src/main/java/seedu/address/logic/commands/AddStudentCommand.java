package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;

/** Adds a student record. */
public class AddStudentCommand extends Command {
    public static final String COMMAND_WORD = "add-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: add-student n/NAME al/ACADEMIC_LEVEL "
            + "[p/PHONE]";
    public static final String DUPLICATE_FORMAT = "A student named %s at %s already exists.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Added student %s: %s (%s).";
    private final StudentName name;
    private final AcademicLevel level;
    private final StudentPhone phone;

    /** Creates an add command for the supplied student name and academic level. */
    public AddStudentCommand(StudentName name, AcademicLevel level) {
        this(name, level, null);
    }

    /** Creates an add command with an optional student phone. */
    public AddStudentCommand(StudentName name, AcademicLevel level, StudentPhone phone) {
        this.name = name;
        this.level = level;
        this.phone = phone;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Student candidate = new Student(name, level, phone);
        if (model.hasStudent(candidate)) {
            throw new CommandException(String.format(DUPLICATE_FORMAT, name, level));
        }
        model.addStudent(candidate);
        String output = String.format(MESSAGE_SUCCESS_FORMAT, candidate.getId(), name, level);
        if (phone != null) {
            output = output.substring(0, output.length() - 1) + " with phone " + phone + ".";
        }
        return new CommandResult(output,
                false, false, candidate.getId().value);
    }
}
