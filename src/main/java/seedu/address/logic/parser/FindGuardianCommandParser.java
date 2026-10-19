package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.GuardianName;

/** Validates a single guardian-name query before any UI or model state changes. */
public class FindGuardianCommandParser implements Parser<FindGuardianCommand> {
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "\\s*(?i:" + Pattern.quote(PREFIX_NAME.getPrefix()) + ")(?<query>.*)", Pattern.DOTALL);
    private static final Pattern EXTRA_PREFIX = Pattern.compile("(?:^|\\s)\\S*/");

    @Override
    public FindGuardianCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = ARGUMENT_FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(FindGuardianCommand.MESSAGE_USAGE);
        }
        String query = matcher.group("query");
        if (query.isBlank() || EXTRA_PREFIX.matcher(query).find()) {
            throw new ParseException(FindGuardianCommand.MESSAGE_USAGE);
        }
        try {
            return new FindGuardianCommand(new GuardianName(query));
        } catch (IllegalArgumentException e) {
            throw new ParseException(FindGuardianCommand.MESSAGE_QUERY_CONSTRAINTS);
        }
    }
}
