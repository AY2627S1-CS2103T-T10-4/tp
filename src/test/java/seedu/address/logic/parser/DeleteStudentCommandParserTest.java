package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.model.student.StudentId;

class DeleteStudentCommandParserTest {
    private final DeleteStudentCommandParser parser = new DeleteStudentCommandParser();

    @Test
    void parse_validId_success() throws Exception {
        assertEquals(new DeleteStudentCommand(new StudentId("S-0007")), parser.parse("SID/S-0007"));
    }

    @Test
    void parse_missingRepeatedUnknownOrInvalidArgument_failure() {
        assertParseFailure(parser, "", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 sid/S-0008", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 x/value", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-7", DeleteStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected sid/S-0007", DeleteStudentCommand.MESSAGE_USAGE);
    }
}
