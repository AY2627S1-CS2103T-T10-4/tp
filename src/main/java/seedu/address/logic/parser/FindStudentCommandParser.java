package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentName;

/** Parses the name query of a find-student command. */
public class FindStudentCommandParser implements Parser<FindStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public FindStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.replaceAll("(?i)n/", "n/")
                .replaceAll("\\s+n/", " n/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase(PREFIX_NAME.getPrefix())) {
                throw new ParseException(FindStudentCommand.MESSAGE_USAGE);
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + canonical, PREFIX_NAME);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(FindStudentCommand.MESSAGE_USAGE);
        }

        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME);
        } catch (ParseException e) {
            throw new ParseException(FindStudentCommand.MESSAGE_USAGE);
        }
        String rawQuery = arguments.getValue(PREFIX_NAME)
                .orElseThrow(() -> new ParseException(FindStudentCommand.MESSAGE_USAGE));
        if (rawQuery.isBlank()) {
            throw new ParseException(FindStudentCommand.MESSAGE_USAGE);
        }

        try {
            return new FindStudentCommand(new StudentName(rawQuery));
        } catch (IllegalArgumentException e) {
            throw new ParseException(FindStudentCommand.MESSAGE_QUERY_CONSTRAINTS);
        }
    }
}
