package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ListStudentsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a list-students command, which accepts no arguments. */
public class ListStudentsCommandParser implements Parser<ListStudentsCommand> {
    @Override
    public ListStudentsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(ListStudentsCommand.MESSAGE_USAGE);
        }
        return new ListStudentsCommand();
    }
}
