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
import seedu.address.model.student.StudentPhone;

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
    void parse_emptyMalformedAndRepeatedValues_rejectsFormat() {
        String[] invalid = {"", "al/Sec 2", "sid/ al/Sec 2", "sid/S-0007 al/", "sid/ al/",
            "sid/s-0007 al/Sec 2", "sid/S-007 al/Sec 2", "sid/S-0007 extra al/Sec 2",
            "SID/S-0007 sid/S-0008 al/Sec 2", "sid/S-0007 AL/Sec 2 al/Sec 3",
            "sid/S-0007 al/Sec 2 X/value", "x/value sid/S-0007 al/Sec 2",
            "sid/S-0007 p/91234567 p/98765432"};
        for (String args : invalid) {
            assertParseFailure(parser, args, EditStudentCommand.MESSAGE_USAGE);
        }
        assertParseFailure(parser, "sid/S-0007 al/Sec 6", AcademicLevel.MESSAGE_CONSTRAINTS);
    }

    @Test
    void parse_reverseOrderAndWhitespace_acceptsCanonicalValues() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));

        parser.parse("\tAL/ip  4\nSID/S-0007\t").execute(model);

        assertEquals(new AcademicLevel("IP 4"), model.findStudent("S-0007").orElseThrow().getAcademicLevel());
    }

    @Test
    void parse_largeLookupId_doesNotAdvanceIdGenerator() throws Exception {
        long previousId = Long.parseLong(StudentId.generate().value.substring(2));
        parser.parse("sid/S-999999999999 al/Sec 2");

        long nextId = Long.parseLong(StudentId.generate().value.substring(2));
        assertEquals(previousId + 1, nextId);
        assertTrue(nextId < 999999999999L);
    }

    @Test
    void parse_phoneOnlyAndCombinedUpdate_success() throws Exception {
        ModelManager model = new ModelManager();
        model.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        parser.parse("sid/S-0007 p/+65 91234567").execute(model);
        assertEquals("+65 91234567", model.findStudent("S-0007").orElseThrow().getPhone().orElseThrow().toString());
        parser.parse("al/Sec 3 sid/S-0007 p/98765432").execute(model);
        assertEquals("Sec 3", model.findStudent("S-0007").orElseThrow().getAcademicLevel().toString());
        assertEquals("+65 98765432", model.findStudent("S-0007").orElseThrow().getPhone().orElseThrow().toString());
        assertParseFailure(parser, "sid/S-0007 p/71234567", StudentPhone.MESSAGE_CONSTRAINTS);
    }
}
