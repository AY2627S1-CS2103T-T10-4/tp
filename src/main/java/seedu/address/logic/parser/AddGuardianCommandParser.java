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
        String canonicalArguments = canonicalizePrefixes(args);
        verifyNoUnknownPrefixes(canonicalArguments);
        ArgumentMultimap arguments = tokenizeArguments(canonicalArguments);
        verifyEmptyPreamble(arguments);
        return parseCommand(arguments);
    }

    private static String canonicalizePrefixes(String arguments) {
        return arguments.replaceAll("(?i)n/", "n/").replaceAll("(?i)p/", "p/");
    }

    private static void verifyNoUnknownPrefixes(String arguments) throws ParseException {
        Matcher matcher = UNKNOWN_PREFIX.matcher(arguments);
        while (matcher.find()) {
            if (!matcher.group(1).equalsIgnoreCase("n/") && !matcher.group(1).equalsIgnoreCase("p/")) {
                throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
            }
        }
    }

    private static ArgumentMultimap tokenizeArguments(String arguments) {
        return ArgumentTokenizer.tokenize(" " + arguments,
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_PHONE);
    }

    private static void verifyEmptyPreamble(ArgumentMultimap arguments) throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
        }
    }

    private static AddGuardianCommand parseCommand(ArgumentMultimap arguments) throws ParseException {
        try {
            arguments.verifyNoDuplicatePrefixesFor(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_PHONE);
            String rawName = arguments.getValue(CliSyntax.PREFIX_NAME)
                    .orElseThrow(() -> new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE));
            String rawPhone = arguments.getValue(CliSyntax.PREFIX_PHONE)
                    .orElseThrow(() -> new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE));
            if (rawName.isEmpty() || rawPhone.isEmpty()) {
                throw new IllegalArgumentException(AddGuardianCommand.MESSAGE_USAGE);
            }
            verifyNoTrailingTextAfterPhone(rawPhone);
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

    private static void verifyNoTrailingTextAfterPhone(String rawPhone) throws ParseException {
        for (int index = 0; index < rawPhone.length(); index++) {
            if (Character.isWhitespace(rawPhone.charAt(index))
                    && GuardianPhone.isValid(rawPhone.substring(0, index))) {
                throw new ParseException(AddGuardianCommand.MESSAGE_USAGE);
            }
        }
    }
}
