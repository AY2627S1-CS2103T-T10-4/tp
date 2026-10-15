package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ListGroupsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the argument-free list-groups command.
 */
public class ListGroupsCommandParser implements Parser<ListGroupsCommand> {
    /**
     * Accepts only empty or whitespace-only argument text.
     *
     * @param args The argument text following the command word.
     * @return A read-only catalogue command.
     * @throws ParseException If any non-whitespace input is present.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public ListGroupsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(ListGroupsCommand.MESSAGE_USAGE);
        }
        return new ListGroupsCommand();
    }
}
