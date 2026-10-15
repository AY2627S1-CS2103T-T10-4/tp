package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses delete-student arguments. */
public class DeleteStudentCommandParser implements Parser<DeleteStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    /**
     * Parses exactly one nonempty {@code sid/} value, ignoring prefix case without reserving an ID.
     *
     * @param args The argument text after the command word.
     * @return A delete command holding the validated ID text.
     * @throws ParseException If the prefix or ID is missing, malformed, repeated, or accompanied by extra input.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public DeleteStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ").replaceAll("(?i)(^|\\s)sid/", "$1sid/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("sid/")) {
                throw new ParseException(DeleteStudentCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical, CliSyntax.PREFIX_STUDENT_ID);
        if (!arg.getPreamble().isEmpty()) {
            throw new ParseException(DeleteStudentCommand.MESSAGE_USAGE);
        }
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_STUDENT_ID);
            String rawId = arg.getValue(CliSyntax.PREFIX_STUDENT_ID)
                    .orElseThrow(() -> new IllegalArgumentException(DeleteStudentCommand.MESSAGE_USAGE));
            if (!rawId.matches("S-[0-9]{4,}")) {
                throw new IllegalArgumentException(DeleteStudentCommand.MESSAGE_USAGE);
            }
            return new DeleteStudentCommand(rawId);
        } catch (ParseException | IllegalArgumentException e) {
            throw new ParseException(DeleteStudentCommand.MESSAGE_USAGE);
        }
    }
}
