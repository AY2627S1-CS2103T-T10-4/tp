package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the argument-free {@code exit} command. */
public class ExitCommandParser implements Parser<ExitCommand> {
    @Override
    public ExitCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(ExitCommand.MESSAGE_USAGE);
        }
        return new ExitCommand();
    }
}
