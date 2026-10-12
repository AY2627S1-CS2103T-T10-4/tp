package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;

/** Parses add-student arguments. */
public class AddStudentCommandParser implements Parser<AddStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public AddStudentCommand parse(String args) throws ParseException {
        String canonical = args.replaceAll("(?i)n/", "n/").replaceAll("(?i)al/", "al/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("n/") && !matcher.group(1).equalsIgnoreCase("al/")) {
                throw new ParseException(AddStudentCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical,
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ACADEMIC_LEVEL);
        if (!arg.getPreamble().isEmpty()) {
            throw new ParseException(AddStudentCommand.MESSAGE_USAGE);
        }
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ACADEMIC_LEVEL);
            String rawName = arg.getValue(CliSyntax.PREFIX_NAME)
                    .orElseThrow(() -> new IllegalArgumentException(AddStudentCommand.MESSAGE_USAGE));
            String rawLevel = arg.getValue(CliSyntax.PREFIX_ACADEMIC_LEVEL)
                    .orElseThrow(() -> new IllegalArgumentException(AddStudentCommand.MESSAGE_USAGE));
            return new AddStudentCommand(new StudentName(rawName), new AcademicLevel(rawLevel));
        } catch (ParseException e) {
            throw new ParseException(AddStudentCommand.MESSAGE_USAGE);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(AddStudentCommand.MESSAGE_USAGE)) {
                throw new ParseException(e.getMessage());
            }
            throw new ParseException(e.getMessage());
        }
    }
}
