package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Restores and displays the complete student list. */
public class ListStudentsCommand extends Command {
    public static final String COMMAND_WORD = "list-students";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: list-students";
    public static final String MESSAGE_SUCCESS_FORMAT = "Listed %d student(s).";
    public static final String MESSAGE_EMPTY = "No students registered.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS);
        int studentCount = model.getStudentList().size();
        return new CommandResult(studentCount == 0
                ? MESSAGE_EMPTY
                : String.format(MESSAGE_SUCCESS_FORMAT, studentCount));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListStudentsCommand;
    }

    @Override
    public int hashCode() {
        return ListStudentsCommand.class.hashCode();
    }
}
