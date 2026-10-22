package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.logic.HelpContent;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an optional single command name without executing that command. */
public class HelpCommandParser implements Parser<HelpCommand> {
    @Override
    public HelpCommand parse(String args) throws ParseException {
        String argument = requireNonNull(args).strip();
        if (argument.isEmpty()) {
            return new HelpCommand();
        }
        if (argument.split("\\s+").length != 1) {
            throw new ParseException(HelpCommand.MESSAGE_USAGE);
        }
        String command = argument.toLowerCase(Locale.ROOT);
        if (!HelpContent.contains(command)) {
            throw new ParseException(String.format(HelpCommand.MESSAGE_UNKNOWN_COMMAND, argument));
        }
        return new HelpCommand(command);
    }
}
