package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianNameContainsQueryPredicate;

/** Displays guardians whose normalized names contain the supplied query. */
public class FindGuardianCommand extends Command {
    public static final String COMMAND_WORD = "find-guardian";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: find-guardian n/QUERY";
    public static final String MESSAGE_QUERY_CONSTRAINTS =
            "Search query must be 1-80 characters and contain name characters only.";
    public static final String MESSAGE_SINGLE_MATCH_FORMAT = "Found 1 guardian matching \"%s\".";
    public static final String MESSAGE_SUCCESS_FORMAT = "Found %d guardians matching \"%s\".";
    public static final String MESSAGE_NO_MATCHES_FORMAT = "No guardians match \"%s\"";

    private static final Logger logger = LogsCenter.getLogger(FindGuardianCommand.class);

    private final GuardianName query;
    private final GuardianNameContainsQueryPredicate predicate;

    /** Creates a search using a validated and whitespace-normalized query. */
    public FindGuardianCommand(GuardianName query) {
        this.query = requireNonNull(query);
        predicate = new GuardianNameContainsQueryPredicate(query);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredGuardianList(predicate);
        int count = model.getGuardianList().size();
        logger.fine(() -> "Guardian name search returned " + count + " record(s).");
        String feedback;
        if (count == 0) {
            feedback = String.format(Locale.ROOT, MESSAGE_NO_MATCHES_FORMAT, query);
        } else if (count == 1) {
            feedback = String.format(Locale.ROOT, MESSAGE_SINGLE_MATCH_FORMAT, query);
        } else {
            feedback = String.format(Locale.ROOT, MESSAGE_SUCCESS_FORMAT, count, query);
        }
        return new CommandResult(feedback, CommandResult.Workspace.GUARDIANS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FindGuardianCommand command && query.equals(command.query);
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
