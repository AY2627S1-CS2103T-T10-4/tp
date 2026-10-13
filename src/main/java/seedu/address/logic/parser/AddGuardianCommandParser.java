package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

/** Parses add-guardian arguments. */
public class AddGuardianCommandParser implements Parser<AddGuardianCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public AddGuardianCommand parse(String args) throws ParseException {
        String canonical = args.replaceAll("(?i)n/", "n/").replaceAll("(?i)p/", "p/");
        Matcher matcher = UNKNOWN_PREFIX.matcher(canonical);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("n/") && !matcher.group(1).equalsIgnoreCase("p/")) {
                throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
            }
        }
        ArgumentMultimap arg = ArgumentTokenizer.tokenize(" " + canonical,
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_PHONE);
        if (!arg.getPreamble().isEmpty()) {
            throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
        }
        try {
            arg.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_PHONE);
            String rawName = arg.getValue(CliSyntax.PREFIX_NAME)
                    .orElseThrow(() -> new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE));
            String rawPhone = arg.getValue(CliSyntax.PREFIX_PHONE)
                    .orElseThrow(() -> new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE));
            if (rawName.isEmpty() || rawPhone.isEmpty()) {
                throw new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE);
            }
            return new AddGuardianCommand(new GuardianName(rawName), new GuardianPhone(rawPhone));
        } catch (ParseException e) {
            throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(AddGuardianCommand.MESSAGE_USAGE)) {
                throw new ParseException(e.getMessage());
            }
            throw new ParseException(e.getMessage());
        }
    }
}
