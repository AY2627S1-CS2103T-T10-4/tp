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
    void parse_invalidCodeLabelAndKind_reportsFieldErrors() {
        assertError(" g/P l/Physics k/Subject", GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        assertError(" g/PHY l/" + "a".repeat(81) + " k/Subject", Group.MESSAGE_LABEL_CONSTRAINTS);
        assertError(" g/PHY l/Physics k/Tutorial", "Group kind must be Subject or Class.");
    }

    @Test
    void parse_missingRepeatedUnknownOrPreamble_reportsUsage() {
        for (String input : new String[] {" g/PHY l/Physics", " g/PHY g/SCI l/Physics k/Subject",
            " g/PHY l/Physics k/Subject x/value", "unexpected g/PHY l/Physics k/Subject",
            " g/PHY l/Physics k/", " g/ l/Physics k/Subject", " g/PHY l/ k/Subject"}) {
            assertError(input, AddGroupCommand.MESSAGE_USAGE);
        }
    }

    private void assertError(String input, String message) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
        assertEquals(message, error.getMessage());
    }
}
