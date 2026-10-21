package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.EditStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentPhone;

/** Parses edit-student arguments. */
public class EditStudentCommandParser implements Parser<EditStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    /**
     * Parses a student ID and academic level and/or phone in either order, ignoring prefix case.
     * Student IDs are checked as text without advancing the ID generator.
     *
     * @param args The argument text after the command word.
     * @return An edit command with the supplied ID text and updated values.
     * @throws ParseException If input has invalid syntax or an unsupported academic level.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public EditStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("[\\t\\n\\x0B\\f\\r]", " ")
                .replaceAll("(?i)(^|\\s)sid/", "$1sid/").replaceAll("(?i)(^|\\s)al/", "$1al/")
                .replaceAll("(?i)(^|\\s)p/", "$1p/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("sid/") && !matcher.group(1).equalsIgnoreCase("al/")
                    && !matcher.group(1).equalsIgnoreCase("p/")) {
                throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical,
                CliSyntax.PREFIX_STUDENT_ID, CliSyntax.PREFIX_ACADEMIC_LEVEL, CliSyntax.PREFIX_PHONE);
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_STUDENT_ID, CliSyntax.PREFIX_ACADEMIC_LEVEL,
                    CliSyntax.PREFIX_PHONE);
        } catch (ParseException e) {
            throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
        }
        String rawId = arg.getValue(CliSyntax.PREFIX_STUDENT_ID).orElse("");
        String rawLevel = arg.getValue(CliSyntax.PREFIX_ACADEMIC_LEVEL).orElse("");
        String rawPhone = arg.getValue(CliSyntax.PREFIX_PHONE).orElse("");
        boolean hasLevel = !arg.getAllValues(CliSyntax.PREFIX_ACADEMIC_LEVEL).isEmpty();
        boolean hasPhone = !arg.getAllValues(CliSyntax.PREFIX_PHONE).isEmpty();
        if (!arg.getPreamble().isEmpty() || !rawId.matches("S-[0-9]{4,}")
                || (hasLevel && rawLevel.isBlank()) || (hasPhone && rawPhone.isBlank())
                || (rawLevel.isBlank() && rawPhone.isBlank())) {
            throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
        }
        try {
            AcademicLevel level = rawLevel.isBlank() ? null : new AcademicLevel(rawLevel);
            StudentPhone phone = rawPhone.isBlank() ? null : new StudentPhone(rawPhone);
            return new EditStudentCommand(rawId, level, phone, phone != null);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }
}
