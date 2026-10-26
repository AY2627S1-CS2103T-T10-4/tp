package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.DeleteGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.GroupCode;

/** Parses the single group code required to delete a group. */
public class DeleteGroupCommandParser implements Parser<DeleteGroupCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public DeleteGroupCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonicalArguments = canonicalizePrefix(args);
        verifyNoUnknownPrefixes(canonicalArguments);
        ArgumentMultimap arguments = tokenizeArguments(canonicalArguments);
        verifyArgumentStructure(arguments);
        return createCommand(arguments);
    }

    private static String canonicalizePrefix(String arguments) {
        return arguments.strip().replaceAll("(?i)(^|\\s)g/", "$1g/");
    }

    private static void verifyNoUnknownPrefixes(String arguments) throws ParseException {
        Matcher prefixes = PREFIX.matcher(arguments);
        while (prefixes.find()) {
            if (!prefixes.group(1).equals("g/")) {
                throw new ParseException(DeleteGroupCommand.MESSAGE_USAGE);
            }
        }
    }

    private static ArgumentMultimap tokenizeArguments(String arguments) {
        return ArgumentTokenizer.tokenize(" " + arguments, PREFIX_GROUP);
    }

    private static void verifyArgumentStructure(ArgumentMultimap arguments) throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(DeleteGroupCommand.MESSAGE_USAGE);
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP);
        } catch (ParseException e) {
            throw new ParseException(DeleteGroupCommand.MESSAGE_USAGE);
        }
    }

    private static DeleteGroupCommand createCommand(ArgumentMultimap arguments) throws ParseException {
        String code = arguments.getValue(PREFIX_GROUP).orElse("").strip();
        if (code.isEmpty() || code.codePoints().anyMatch(Character::isWhitespace)) {
            throw new ParseException(DeleteGroupCommand.MESSAGE_USAGE);
        }
        if (!GroupCode.isValidNewCode(code)) {
            throw new ParseException(GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        }
        return new DeleteGroupCommand(new GroupCode(code));
    }
}
