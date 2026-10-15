package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.EditStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;

/** Parses edit-student arguments. */
public class EditStudentCommandParser implements Parser<EditStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    /**
     * Parses one nonempty {@code sid/} and {@code al/} value in either order, ignoring prefix case.
     * Student IDs are checked as text without advancing the ID generator.
     *
     * @param args The argument text after the command word.
     * @return An edit command with the supplied ID text and canonical academic level.
     * @throws ParseException If input has invalid syntax or an unsupported academic level.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public EditStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ")
                .replaceAll("(?i)(^|\\s)sid/", "$1sid/").replaceAll("(?i)(^|\\s)al/", "$1al/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("sid/") && !matcher.group(1).equalsIgnoreCase("al/")) {
                throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical,
                CliSyntax.PREFIX_STUDENT_ID, CliSyntax.PREFIX_ACADEMIC_LEVEL);
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_STUDENT_ID, CliSyntax.PREFIX_ACADEMIC_LEVEL);
        } catch (ParseException e) {
            throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
        }
        String rawId = arg.getValue(CliSyntax.PREFIX_STUDENT_ID).orElse("");
        String rawLevel = arg.getValue(CliSyntax.PREFIX_ACADEMIC_LEVEL).orElse("");
        if (!arg.getPreamble().isEmpty() || !rawId.matches("S-[0-9]{4,}") || rawLevel.isBlank()) {
            throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
        }
        try {
            return new EditStudentCommand(rawId, new AcademicLevel(rawLevel));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }
}
