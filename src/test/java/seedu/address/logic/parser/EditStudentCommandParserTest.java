package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditStudentCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class EditStudentCommandParserTest {
    private final EditStudentCommandParser parser = new EditStudentCommandParser();

    @Test
    void parse_validFieldsAndCase_success() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        parser.parse("SID/S-0007 AL/sec 3").execute(model);
        assertEquals("Sec 3", model.getStudentList().get(0).getAcademicLevel().toString());
    }

    @Test
    void parse_missingDuplicateOrUnknownPrefix_failure() {
        assertParseFailure(parser, "sid/S-0007", EditStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 al/Sec 2 al/Sec 3", EditStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "sid/S-0007 al/Sec 2 x/value", EditStudentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected sid/S-0007 al/Sec 2", EditStudentCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_largeLookupId_doesNotAdvanceIdGenerator() throws Exception {
        parser.parse("sid/S-999999999999 al/Sec 2");

        long nextId = Long.parseLong(StudentId.generate().value.substring(2));
        assertTrue(nextId < 999999999999L);
    }
}
