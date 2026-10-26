package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP_KIND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP_LABEL;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

/** Parses the code, label, and kind required to create a group. */
public class AddGroupCommandParser implements Parser<AddGroupCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");
    private static final String MESSAGE_KIND_CONSTRAINTS = "Group kind must be Subject or Class.";

    @Override
    public AddGroupCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonicalArguments = canonicalizePrefixes(args);
        verifyNoUnknownPrefixes(canonicalArguments);
        ArgumentMultimap arguments = tokenizeArguments(canonicalArguments);
        verifyArgumentStructure(arguments);
        return createCommand(arguments);
    }

    private static String canonicalizePrefixes(String arguments) {
        return arguments.strip().replaceAll("(?i)(^|\\s)g/", "$1g/")
                .replaceAll("(?i)(^|\\s)l/", "$1l/")
                .replaceAll("(?i)(^|\\s)k/", "$1k/");
    }

    private static void verifyNoUnknownPrefixes(String arguments) throws ParseException {
        Matcher prefixes = PREFIX.matcher(arguments);
        while (prefixes.find()) {
            String prefix = prefixes.group(1);
            if (!prefix.equals("g/") && !prefix.equals("l/") && !prefix.equals("k/")) {
                throw new ParseException(AddGroupCommand.MESSAGE_USAGE);
            }
        }
    }

    private static ArgumentMultimap tokenizeArguments(String arguments) {
        return ArgumentTokenizer.tokenize(" " + arguments, PREFIX_GROUP, PREFIX_GROUP_LABEL, PREFIX_GROUP_KIND);
    }

    private static void verifyArgumentStructure(ArgumentMultimap arguments) throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(AddGroupCommand.MESSAGE_USAGE);
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP, PREFIX_GROUP_LABEL, PREFIX_GROUP_KIND);
        } catch (ParseException e) {
            throw new ParseException(AddGroupCommand.MESSAGE_USAGE);
        }
    }

    private static AddGroupCommand createCommand(ArgumentMultimap arguments) throws ParseException {
        String code = arguments.getValue(PREFIX_GROUP).orElse("").strip();
        String label = arguments.getValue(PREFIX_GROUP_LABEL).orElse("").strip().replaceAll("\\s+", " ");
        String kind = arguments.getValue(PREFIX_GROUP_KIND).orElse("").strip();
        if (code.isEmpty() || label.isEmpty() || kind.isEmpty()) {
            throw new ParseException(AddGroupCommand.MESSAGE_USAGE);
        }
        if (!GroupCode.isValidNewCode(code)) {
            throw new ParseException(GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        }
        if (!Group.isValidNewLabel(label)) {
            throw new ParseException(Group.MESSAGE_LABEL_CONSTRAINTS);
        }
        return new AddGroupCommand(new Group(new GroupCode(code), label, parseKind(kind)));
    }

    private static GroupKind parseKind(String kind) throws ParseException {
        return switch (kind.toLowerCase(Locale.ROOT)) {
            case "subject" -> GroupKind.SUBJECT;
            case "class" -> GroupKind.CLASS;
            default -> throw new ParseException(MESSAGE_KIND_CONSTRAINTS);
        };
    }
}
