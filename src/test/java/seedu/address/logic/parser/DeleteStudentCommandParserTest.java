package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.model.student.StudentId;

class DeleteStudentCommandParserTest {
    private final DeleteStudentCommandParser parser = new DeleteStudentCommandParser();

    @Test
    void parse_validId_success() throws Exception {
        assertEquals(new DeleteStudentCommand("S-0007"), parser.parse("SID/S-0007"));
    }

    @Test
    void parse_largeUnknownId_doesNotAdvanceIdGenerator() throws Exception {
        long previousId = Long.parseLong(StudentId.generate().value.substring(2));
        parser.parse("sid/S-999999999999");

        long after = Long.parseLong(StudentId.generate().value.substring(2));
        assertEquals(previousId + 1, after);
        assertTrue(after < 999999999999L);
    }

    @Test
    void parse_missingRepeatedUnknownOrInvalidArgument_failure() {
        assertParseFailure(parser, "", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 sid/S-0008", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 x/value", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-7", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected sid/S-0007", DeleteStudentCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_whitespaceAndMalformedValues_followSinglePrefixContract() throws Exception {
        assertEquals(new DeleteStudentCommand("S-0007"), parser.parse("\tSID/S-0007\n"));
        for (String args : new String[] {"sid/", "sid/s-0007", "sid/S-0007 extra", "sid/S-0007 SID/S-0008",
            "x/value sid/S-0007", "sid/S-0007\nX/value"}) {
            assertParseFailure(parser, args, DeleteStudentCommand.MESSAGE_USAGE);
        }
    }
}
