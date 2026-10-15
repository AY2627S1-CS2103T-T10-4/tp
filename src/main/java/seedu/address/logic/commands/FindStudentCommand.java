package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentNameContainsQueryPredicate;

/** Filters the displayed student list to names containing a query. */
public class FindStudentCommand extends Command {
    public static final String COMMAND_WORD = "find-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: find-student n/QUERY";
    public static final String MESSAGE_SUCCESS_FORMAT = "Found %d student(s) matching \"%s\".";
    public static final String MESSAGE_NO_MATCHES_FORMAT = "No students match \"%s\".";

    private final StudentName query;
    private final StudentNameContainsQueryPredicate predicate;

    /** Creates a command that searches for the supplied cleaned name query. */
    public FindStudentCommand(StudentName query) {
        this.query = requireNonNull(query);
        predicate = new StudentNameContainsQueryPredicate(query);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredStudentList(predicate);
        int matchCount = model.getStudentList().size();
        if (matchCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES_FORMAT, query));
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, matchCount, query));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FindStudentCommand command && query.equals(command.query);
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("query", query).toString();
    }
}
