package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.UnlinkGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses exactly one student ID without reserving or allocating an ID. */
public class UnlinkGuardianCommandParser implements Parser<UnlinkGuardianCommand> {
    // Match the whole input: reject extra text and repeated prefixes, but ignore prefix casing.
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "\\s*(?i:" + Pattern.quote(PREFIX_STUDENT_ID.getPrefix()) + ")\\s*(?<studentId>S-[0-9]{4,})\\s*");

    @Override
    public UnlinkGuardianCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = ARGUMENT_FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(UnlinkGuardianCommand.MESSAGE_USAGE);
        }
        return new UnlinkGuardianCommand(matcher.group("studentId"));
    }
}
