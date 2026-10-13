package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.EditStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentId;

/** Parses edit-student arguments. */
public class EditStudentCommandParser implements Parser<EditStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public EditStudentCommand parse(String args) throws ParseException {
        String canonical = args.replaceAll("(?i)sid/", "sid/").replaceAll("(?i)al/", "al/");
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
            String rawId = arg.getValue(CliSyntax.PREFIX_STUDENT_ID)
                    .orElseThrow(() -> new IllegalArgumentException(EditStudentCommand.MESSAGE_USAGE));
            String rawLevel = arg.getValue(CliSyntax.PREFIX_ACADEMIC_LEVEL)
                    .orElseThrow(() -> new IllegalArgumentException(EditStudentCommand.MESSAGE_USAGE));
            if (!arg.getPreamble().isEmpty()) {
                throw new IllegalArgumentException(EditStudentCommand.MESSAGE_USAGE);
            }
            return new EditStudentCommand(new StudentId(rawId), new AcademicLevel(rawLevel));
        } catch (ParseException e) {
            throw new ParseException(EditStudentCommand.MESSAGE_USAGE);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }
}
