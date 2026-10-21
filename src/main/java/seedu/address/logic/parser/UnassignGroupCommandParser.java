package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.UnassignGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.GroupCode;

/**
 * Parses the two required unassignment parameters while retaining the student ID as text.
 * Reference existence and assignment membership are validated when the returned command executes.
 */
public class UnassignGroupCommandParser implements Parser<UnassignGroupCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    /** Creates a parser for one student ID and one group code. */
    public UnassignGroupCommandParser() { }

    /**
     * Parses exactly one {@code sid/} and one {@code g/}, in either order and ignoring prefix case.
     * Student IDs must match {@code S-[0-9]{4,}}; syntax is checked without numeric parsing or ID allocation.
     * Group codes are canonicalized after rejecting empty values and extra whitespace or text.
     *
     * @param args The argument text following the command word, excluding the command word itself.
     * @return The command with validated ID text and a canonical group code.
     * @throws ParseException If a required value is missing or malformed, or unexpected input is present.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public UnassignGroupCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ")
                .replaceAll("(?i)(^|\\s)sid/", "$1sid/")
                .replaceAll("(?i)(^|\\s)g/", "$1g/");
        Matcher prefixes = PREFIX.matcher(canonical);
        while (prefixes.find()) {
            if (!prefixes.group(1).equals("sid/") && !prefixes.group(1).equals("g/")) {
                throw new ParseException(UnassignGroupCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + canonical, PREFIX_STUDENT_ID, PREFIX_GROUP);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(UnassignGroupCommand.MESSAGE_USAGE);
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_ID, PREFIX_GROUP);
        } catch (ParseException e) {
            throw new ParseException(UnassignGroupCommand.MESSAGE_USAGE);
        }
        String studentId = arguments.getValue(PREFIX_STUDENT_ID).orElse("").strip();
        String groupCode = arguments.getValue(PREFIX_GROUP).orElse("").strip();
        boolean hasExtraText = groupCode.codePoints()
                .anyMatch(value -> Character.isWhitespace(value) || Character.isSpaceChar(value));
        if (!studentId.matches("S-[0-9]{4,}") || groupCode.isBlank() || hasExtraText) {
            throw new ParseException(UnassignGroupCommand.MESSAGE_USAGE);
        }
        return new UnassignGroupCommand(studentId, new GroupCode(groupCode));
    }
}
