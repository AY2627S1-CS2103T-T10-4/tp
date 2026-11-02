package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

class AddGroupCommandParserTest {
    private final AddGroupCommandParser parser = new AddGroupCommandParser();

    @Test
    void parse_validArguments_acceptsAnyOrderAndCaseAndNormalizesValues() throws Exception {
        AddGroupCommand expected = new AddGroupCommand(
                new Group(new GroupCode("S3-PHY-B"), "Secondary 3 Physics B", GroupKind.CLASS));

        assertEquals(expected, parser.parse(" K/class  L/ Secondary   3 Physics B G/s3-phy-b "));
    }

    @Test
    void parse_asciiAndAccentedCodes_acceptsOnlyAsciiCode() throws Exception {
        AddGroupCommand expected = new AddGroupCommand(
                new Group(new GroupCode("ET"), "French", GroupKind.SUBJECT));
        String expectedError = "Group code must be 2-20 ASCII letters (A-Z or a-z), digits, or hyphens, "
                + "start with a letter, and end with a letter or digit.";

        assertEquals(expected, parser.parse(" g/ET l/French k/Subject"));
        assertError(" g/ÉT l/French k/Subject", expectedError);
    }

    @Test
    void parse_invalidCodeLabelAndKind_reportsFieldErrors() {
        assertError(" g/P l/Physics k/Subject", GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        assertError(" g/PHY l/" + "a".repeat(81) + " k/Subject", Group.MESSAGE_LABEL_CONSTRAINTS);
        assertError(" g/PHY l/Physics k/Tutorial", "Group kind must be Subject or Class.");
    }

    @Test
    void parse_boundaryValues_countUnicodeCharacters() throws Exception {
        String twentyCharacterCode = "A" + "1".repeat(19);
        String eightyCharacterLabel = "😀".repeat(80);

        parser.parse("g/AB l/A k/Subject");
        parser.parse("g/" + twentyCharacterCode + " l/" + eightyCharacterLabel + " k/Class");
        assertError("g/A l/A k/Subject", GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        assertError("g/A" + "1".repeat(20) + " l/A k/Subject", GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        assertError("g/AB l/" + "😀".repeat(81) + " k/Subject", Group.MESSAGE_LABEL_CONSTRAINTS);
    }

    @Test
    void parse_tabSeparatedRepeatedPrefixes_reportsUsage() {
        for (String input : new String[] {"g/PHY l/Physics\tl/Chemistry k/Subject",
            "g/PHY\tg/SCI l/Physics k/Subject", "g/PHY l/Physics k/Subject\tk/Class",
            "g/PHY k/Subject l/Physics\tg/SCI", "g/PHY k/Subject l/Physics\tk/Class",
            "K/Subject G/PHY L/Physics\tL/Chemistry"}) {
            assertError(input, AddGroupCommand.MESSAGE_USAGE);
        }
    }

    @Test
    void parse_spaceSeparatedRepeatedLabelAndKind_reportsUsage() {
        assertError("g/PHY l/Physics l/Chemistry k/Subject", AddGroupCommand.MESSAGE_USAGE);
        assertError("g/PHY l/Physics k/Subject k/Class", AddGroupCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_labelWhitespaceAndSlashes_preservesNormalizedValues() throws Exception {
        String[][] labels = {
            {"Physics   Mathematics", "Physics Mathematics"},
            {"Physics\tMathematics", "Physics Mathematics"},
            {"Physics/Math", "Physics/Math"},
            {"Physics / Mathematics", "Physics / Mathematics"}
        };
        for (String[] label : labels) {
            AddGroupCommand expected = new AddGroupCommand(
                    new Group(new GroupCode("PHY"), label[1], GroupKind.SUBJECT));
            assertEquals(expected, parser.parse("g/phy l/" + label[0] + " k/subject"));
        }
    }

    @Test
    void parse_tabSeparatedUniquePrefix_reportsUsage() {
        assertError("g/PHY\tl/Physics k/Subject", AddGroupCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_missingRepeatedUnknownOrPreamble_reportsUsage() {
        for (String input : new String[] {" g/PHY l/Physics", " g/PHY g/SCI l/Physics k/Subject",
            " g/PHY l/Physics k/Subject x/value", " g/PHY l/Physics\tx/value k/Subject",
            "unexpected g/PHY l/Physics k/Subject",
            " g/PHY l/Physics k/", " g/ l/Physics k/Subject", " g/PHY l/ k/Subject"}) {
            assertError(input, AddGroupCommand.MESSAGE_USAGE);
        }
    }

    @Test
    void parse_invalidFormat_reportsExactUsage() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(" g/PHY l/Physics"));

        assertEquals("Invalid command format. Use: add-group g/GROUP_CODE l/GROUP_LABEL k/GROUP_KIND",
                error.getMessage());
    }

    private void assertError(String input, String message) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
        assertEquals(message, error.getMessage());
    }
}
