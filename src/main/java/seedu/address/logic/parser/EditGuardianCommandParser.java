package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.EditGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.GuardianPhone;

/** Parses edit-guardian arguments. */
public class EditGuardianCommandParser implements Parser<EditGuardianCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public EditGuardianCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("(?i)(^|\\s)gid/", "$1gid/")
                .replaceAll("(?i)(^|\\s)p/", "$1p/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("gid/") && !matcher.group(1).equalsIgnoreCase("p/")) {
                throw new ParseException(EditGuardianCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical,
                CliSyntax.PREFIX_GUARDIAN_ID, CliSyntax.PREFIX_PHONE);
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_GUARDIAN_ID, CliSyntax.PREFIX_PHONE);
        } catch (ParseException e) {
            throw new ParseException(EditGuardianCommand.MESSAGE_USAGE);
        }
        String id = arg.getValue(CliSyntax.PREFIX_GUARDIAN_ID).orElse("");
        String rawPhone = arg.getValue(CliSyntax.PREFIX_PHONE).orElse("");
        if (!arg.getPreamble().isEmpty() || !id.matches("G-[0-9]{4,}") || rawPhone.isBlank()) {
            throw new ParseException(EditGuardianCommand.MESSAGE_USAGE);
        }
        try {
            return new EditGuardianCommand(id, new GuardianPhone(rawPhone));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }
}
