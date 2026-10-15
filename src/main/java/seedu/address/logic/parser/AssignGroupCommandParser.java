package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AssignGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.GroupCode;

/**
 * Parses the two required, single-value assignment parameters without allocating student IDs.
 */
public class AssignGroupCommandParser implements Parser<AssignGroupCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    /**
     * Parses exactly one {@code sid/} and one {@code g/} prefix, in either order and ignoring prefix case.
     * ID syntax is validated without reserving an ID; reference existence is checked by the command.
     *
     * @param args The argument text following the command word.
     * @return The command with ID text and a canonical group code.
     * @throws ParseException If required values are missing or malformed, or unexpected input is present.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public AssignGroupCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ")
                .replaceAll("(?i)(^|\\s)sid/", "$1sid/")
                .replaceAll("(?i)(^|\\s)g/", "$1g/");
        Matcher prefixes = PREFIX.matcher(canonical);
        while (prefixes.find()) {
            if (!prefixes.group(1).equals("sid/") && !prefixes.group(1).equals("g/")) {
                throw new ParseException(AssignGroupCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + canonical, PREFIX_STUDENT_ID, PREFIX_GROUP);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(AssignGroupCommand.MESSAGE_USAGE);
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_ID, PREFIX_GROUP);
        } catch (ParseException e) {
            throw new ParseException(AssignGroupCommand.MESSAGE_USAGE);
        }
        String studentId = arguments.getValue(PREFIX_STUDENT_ID).orElse("").strip();
        String groupCode = arguments.getValue(PREFIX_GROUP).orElse("").strip();
        boolean hasExtraText = groupCode.codePoints()
                .anyMatch(value -> Character.isWhitespace(value) || Character.isSpaceChar(value));
        if (!studentId.matches("S-[0-9]{4,}") || groupCode.isBlank() || hasExtraText) {
            throw new ParseException(AssignGroupCommand.MESSAGE_USAGE);
        }
        return new AssignGroupCommand(studentId, new GroupCode(groupCode));
    }
}
