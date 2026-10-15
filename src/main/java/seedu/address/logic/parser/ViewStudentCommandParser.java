package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ViewStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the single student-ID argument of a view-student command. */
public class ViewStudentCommandParser implements Parser<ViewStudentCommand> {

    // Match the entire argument string to reject extra text, unknown prefixes and repeated IDs.
    // Only the prefix is case-insensitive; generated student IDs use an uppercase S.
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "\\s*(?i:" + Pattern.quote(PREFIX_STUDENT_ID.getPrefix()) + ")\\s*(?<studentId>S-[0-9]{4,})\\s*");

    @Override
    public ViewStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = ARGUMENT_FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(ViewStudentCommand.MESSAGE_USAGE);
        }
        return new ViewStudentCommand(matcher.group("studentId"));
    }
}
