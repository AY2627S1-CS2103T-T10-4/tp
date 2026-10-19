package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ListGuardiansCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the argument-free {@code list-guardians} command. */
public class ListGuardiansCommandParser implements Parser<ListGuardiansCommand> {
    @Override
    public ListGuardiansCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(ListGuardiansCommand.MESSAGE_USAGE);
        }
        return new ListGuardiansCommand();
    }
}
