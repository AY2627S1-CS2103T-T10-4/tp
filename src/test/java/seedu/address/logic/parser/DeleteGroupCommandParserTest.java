package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteGroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.GroupCode;

class DeleteGroupCommandParserTest {
    private final DeleteGroupCommandParser parser = new DeleteGroupCommandParser();

    @Test
    void parse_validCode_acceptsCaseInsensitivePrefixAndCanonicalizesCode() throws Exception {
        assertEquals(new DeleteGroupCommand(new GroupCode("S3-PHY-B")), parser.parse(" G/s3-phy-b "));
    }

    @Test
    void parse_invalidArguments_reportsUsage() {
        for (String input : new String[] {"", " g/", " g/MAT g/SCI", "x g/MAT", " g/MAT x/value",
            " g/MAT extra"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
            assertEquals(DeleteGroupCommand.MESSAGE_USAGE, error.getMessage());
        }
    }

    @Test
    void parse_invalidCode_reportsCodeConstraint() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(" g/P"));

        assertEquals(GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS, error.getMessage());
    }
}
