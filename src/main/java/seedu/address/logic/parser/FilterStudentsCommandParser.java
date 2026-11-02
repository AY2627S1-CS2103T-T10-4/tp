package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACADEMIC_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FilterStudentsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;

/** Parses exactly one group or academic-level variant of filter-students. */
public class FilterStudentsCommandParser implements Parser<FilterStudentsCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?:^|\\s)([a-z][a-z0-9-]*/)");

    @Override
    public FilterStudentsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String canonical = args.strip().replaceAll("\\s+", " ")
                .replaceAll("(?i)(^|\\s)g/", "$1g/")
                .replaceAll("(?i)(^|\\s)al/", "$1al/");
        Matcher prefixes = PREFIX.matcher(canonical);
        while (prefixes.find()) {
            if (!prefixes.group(1).equals("g/") && !prefixes.group(1).equals("al/")) {
                throw usage();
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + canonical, PREFIX_GROUP, PREFIX_ACADEMIC_LEVEL);
        if (!arguments.getPreamble().isEmpty()) {
            throw usage();
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP, PREFIX_ACADEMIC_LEVEL);
        } catch (ParseException e) {
            throw usage();
        }

        Optional<String> groupCode = arguments.getValue(PREFIX_GROUP);
        Optional<String> academicLevel = arguments.getValue(PREFIX_ACADEMIC_LEVEL);
        if (groupCode.isPresent() == academicLevel.isPresent()) {
            throw usage();
        }
        if (groupCode.isPresent()) {
            String code = groupCode.orElseThrow().strip();
            boolean containsWhitespace = code.codePoints()
                    .anyMatch(value -> Character.isWhitespace(value) || Character.isSpaceChar(value));
            if (code.isBlank() || containsWhitespace || code.contains("/")) {
                throw usage();
            }
            return new FilterStudentsCommand(new GroupCode(code));
        }

        String level = academicLevel.orElseThrow().strip();
        // A remaining delimiter belongs to malformed or additional command syntax, not a level value.
        if (level.isBlank() || level.contains("/")) {
            throw usage();
        }
        try {
            return new FilterStudentsCommand(new AcademicLevel(level));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private static ParseException usage() {
        return new ParseException(FilterStudentsCommand.MESSAGE_USAGE);
    }
}
