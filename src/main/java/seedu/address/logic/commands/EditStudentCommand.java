package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentPhone;

/** Updates a registered student's academic level and/or independent phone while retaining its stable ID. */
public class EditStudentCommand extends Command {
    public static final String COMMAND_WORD = "edit-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: edit-student sid/STUDENT_ID "
            + "al/ACADEMIC_LEVEL or edit-student sid/STUDENT_ID p/PHONE";
    public static final String MESSAGE_NOT_FOUND_FORMAT = "Student %s does not exist.";
    public static final String MESSAGE_DUPLICATE_FORMAT = "Cannot update %s: a student named %s at %s "
            + "already exists.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Updated student %s: academic level is now %s.";

    private final String id;
    private final AcademicLevel level;
    private final StudentPhone phone;
    private final boolean editingPhone;

    /**
     * Retains the parsed ID text without allocating or reserving a StudentId.
     *
     * @param id The student ID text validated by the parser.
     * @param level The requested academic level.
     * @throws NullPointerException If either argument is null.
     */
    public EditStudentCommand(String id, AcademicLevel level) {
        this(id, requireNonNull(level), null, false);
    }

    /** Creates an edit command that updates only the student's phone. */
    public EditStudentCommand(String id, StudentPhone phone) {
        this(id, null, phone, true);
    }

    /** Creates an edit command updating the supplied fields. */
    public EditStudentCommand(String id, AcademicLevel level, StudentPhone phone, boolean editingPhone) {
        requireNonNull(id);
        this.id = id;
        this.level = level;
        this.phone = phone;
        this.editingPhone = editingPhone;
    }

    /**
     * Creates an edit command from an existing ID without changing the ID generator.
     *
     * @param id The existing student ID.
     * @param level The requested academic level.
     * @throws NullPointerException If either argument is null.
     */
    public EditStudentCommand(StudentId id, AcademicLevel level) {
        this(requireNonNull(id).value, level);
    }

    /**
     * Returns the ID used in a persistence failure message.
     *
     * @return The supplied stable student ID text.
     */
    public String getStudentId() {
        return id;
    }

    /**
     * Resolves the full register and delegates duplicate validation and replacement to the model.
     * Persistence and revealing a hidden target are handled by LogicManager after successful execution.
     *
     * @param model The authoritative register, independent of its filtered Student view.
     * @return Success feedback and stable-ID selection, keeping the current workspace.
     * @throws CommandException If the ID is unknown or the new identity duplicates another student.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Student current = model.findStudent(id)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND_FORMAT, id)));
        StudentId matchedId = current.getId();
        AcademicLevel updatedLevel = level == null ? current.getAcademicLevel() : level;
        StudentPhone updatedPhone = editingPhone ? phone : current.getPhone().orElse(null);
        Student updated = new Student(matchedId, current.getName(), updatedLevel, updatedPhone);
        try {
            model.updateStudent(updated);
        } catch (IllegalArgumentException e) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_FORMAT, id, updated.getName(), updatedLevel),
                    e);
        }
        String feedback = level != null && editingPhone
                ? "Updated student " + id + ": academic level is now " + updatedLevel
                        + " and phone is now " + phone + "."
                : editingPhone
                        ? "Updated student " + id + " phone to " + phone + "."
                        : String.format(MESSAGE_SUCCESS_FORMAT, id, updatedLevel);
        return new CommandResult(feedback, false, false, id);
    }
}
