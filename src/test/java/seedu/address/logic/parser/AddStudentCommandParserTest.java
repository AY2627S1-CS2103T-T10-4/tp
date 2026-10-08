package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddStudentCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;

class AddStudentCommandParserTest {
    private final AddStudentCommandParser parser = new AddStudentCommandParser();

    @Test
    void parse_validFieldsAndPrefixOrder_success() throws Exception {
        ModelManager firstModel = new ModelManager();
        parser.parse("n/Mei   Lin al/sec 2").execute(firstModel);
        assertEquals("Mei Lin", firstModel.getStudentList().get(0).getName().toString());
        assertEquals("Sec 2", firstModel.getStudentList().get(0).getAcademicLevel().toString());

        ModelManager secondModel = new ModelManager();
        parser.parse("AL/IP 3 N/O'Connor").execute(secondModel);
        assertEquals("O'Connor", secondModel.getStudentList().get(0).getName().toString());
        assertEquals("IP 3", secondModel.getStudentList().get(0).getAcademicLevel().toString());
    }

    @Test
    void parse_missingRepeatedOrUnknownPrefix_failure() {
        assertParseFailure(parser, "n/Mei Lin", AddStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Mei Lin al/Sec 2 al/Sec 3", AddStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Mei Lin al/Sec 2 x/value", AddStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected n/Mei Lin al/Sec 2", AddStudentCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_invalidNameOrLevel_failure() {
        assertParseFailure(parser, "n/Mei2 Lin al/Sec 2", StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "n/Mei Lin al/Sec 6", AcademicLevel.MESSAGE_CONSTRAINTS);
    }
}
