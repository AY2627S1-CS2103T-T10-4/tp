package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses exactly one student ID and one guardian ID, in either prefix order. */
public class LinkGuardianCommandParser implements Parser<LinkGuardianCommand> {
    @Override
    public LinkGuardianCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ")
                .replaceAll("(?i)(^| )sid/", "$1sid/")
                .replaceAll("(?i)(^| )gid/", "$1gid/");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + canonical,
                PREFIX_STUDENT_ID, PREFIX_GUARDIAN_ID);
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_ID, PREFIX_GUARDIAN_ID);
        } catch (ParseException e) {
            throw new ParseException(LinkGuardianCommand.MESSAGE_USAGE);
        }
        String studentId = arguments.getValue(PREFIX_STUDENT_ID).orElse("");
        String guardianId = arguments.getValue(PREFIX_GUARDIAN_ID).orElse("");
        // Full-value matching also rejects unknown prefixes and stray text absorbed into either argument.
        if (!arguments.getPreamble().isEmpty() || !studentId.matches("S-[0-9]{4,}")
                || !guardianId.matches("G-[0-9]{4,}")) {
            throw new ParseException(LinkGuardianCommand.MESSAGE_USAGE);
        }
        return new LinkGuardianCommand(studentId, guardianId);
    }
}
