package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class AssignGroupCommandParserTest {
    @Test
    void parse_validOrdersCasingAndWhitespace_assignsConfiguredGroup() throws Exception {
        for (String input : new String[] {"assign-group sid/S-0601 g/mat", "ASSIGN-GROUP G/MAT SID/S-0601",
            "assign-group g/S2-MAT-A sid/S-0601", "assign-group\tSID/ S-0601\nG/ mat "}) {
            ModelManager model = model();
            Command command = new AddressBookParser().parseCommand(input);
            CommandResult result = command.execute(model);

            assertEquals("S-0601", result.getSelectedStudentId());
            assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
            assertEquals(1, model.getAddressBook().getGroupAssignments().size());
        }
    }

    @Test
    void parse_malformedArguments_reportsExactFormatWithoutMutation() {
        String[] invalid = {"", "sid/S-0601", "g/MAT", "sid/ g/MAT", "sid/S-0601 g/",
            "sid/S-0601 SID/S-0601 g/MAT", "sid/S-0601 g/MAT G/SCI", "sid/S-0601 g/MAT p/91234567",
            "unexpected sid/S-0601 g/MAT", "sid/S-0601 extra g/MAT", "sid/S-0601 g/MAT extra",
            "sid/s-0601 g/MAT", "sid/S-601 g/MAT", "sid/G-0601 g/MAT", "sid/S-0601g/MAT",
            "n/Mei sid/S-0601 g/MAT", "sid/S-0601 g/\u2003"};
        for (String args : invalid) {
            ParseException error = assertThrows(ParseException.class, () ->
                    new AddressBookParser().parseCommand("assign-group " + args), args);
            assertEquals("Invalid command format. Use: assign-group sid/STUDENT_ID g/GROUP_CODE", error.getMessage());
        }
    }

    private static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addStudent(new Student(new StudentId("S-0601"), new StudentName("Mei Lin"), new AcademicLevel("Sec 3")));
        return model;
    }
}
