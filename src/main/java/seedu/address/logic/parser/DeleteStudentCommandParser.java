package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentId;

/** Parses delete-student arguments. */
public class DeleteStudentCommandParser implements Parser<DeleteStudentCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public DeleteStudentCommand parse(String args) throws ParseException {
        String canonical = args.replaceAll("(?i)sid/", "sid/");
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
            return new DeleteStudentCommand(new StudentId(rawId));
        } catch (ParseException | IllegalArgumentException e) {
            throw new ParseException(DeleteStudentCommand.MESSAGE_USAGE);
        }
    }
}
