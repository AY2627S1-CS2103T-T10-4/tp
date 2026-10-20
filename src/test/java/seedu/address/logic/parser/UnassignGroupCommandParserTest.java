package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UnassignGroupCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

class UnassignGroupCommandParserTest {
    private final UnassignGroupCommandParser parser = new UnassignGroupCommandParser();

    @Test
    void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void parse_validOrdersCasingAndWhitespace_removesCanonicalGroup() throws Exception {
        Map<String, String> validArguments = Map.of(
                "sid/S-0007 g/MAT", "MAT",
                "g/S2-MAT-A sid/S-0007", "S2-MAT-A",
                "SiD/S-0007 G/mAt", "MAT",
                " \tG/ s2-mAt-a\nSID/ S-0007 \t", "S2-MAT-A",
                "  sid/  S-0007   g/   mat  ", "MAT");
        for (Map.Entry<String, String> entry : validArguments.entrySet()) {
            ModelManager model = model();
            Student student = model.findStudent("S-0007").orElseThrow();
            String remainingCode = entry.getValue().equals("MAT") ? "S2-MAT-A" : "MAT";

            UnassignGroupCommand command = parser.parse(entry.getKey());
            CommandResult result = command.execute(model);

            assertEquals("Removed group " + entry.getValue() + " from student S-0007.", result.getFeedbackToUser());
            assertEquals("S-0007", result.getSelectedStudentId());
            assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
            assertEquals(Set.of(new GroupAssignment(student.getId(), new GroupCode(remainingCode))),
                    model.getAddressBook().getGroupAssignments());
        }
    }

    @Test
    void parse_malformedArguments_reportsExactFormat() {
        String[] invalid = {"", " \t\n", "sid/S-0007", "g/MAT", "sid/ g/MAT", "sid/S-0007 g/",
            "sid/S-0007 sid/S-0007 g/MAT", "sid/S-0007 SID/S-0007 g/MAT",
            "sid/S-0007 g/MAT g/SCI", "sid/S-0007 g/MAT G/SCI",
            "sid/S-0007 g/MAT p/91234567", "n/Mei sid/S-0007 g/MAT",
            "unexpected sid/S-0007 g/MAT", "unassign-group sid/S-0007 g/MAT",
            "sid/S-0007 extra g/MAT", "sid/S-0007 g/MAT extra", "g/MAT sid/S-0007 extra",
            "sid/s-0007 g/MAT", "sid/G-0007 g/MAT", "sid/S-00a7 g/MAT", "sid/S-007 g/MAT",
            "sid/S-0007.0 g/MAT", "sid/S-+0007 g/MAT", "sid/S-\u0660\u0660\u0660\u0667 g/MAT",
            "sid/S-\uFF10\uFF10\uFF10\uFF17 g/MAT", "sid/S-0007g/MAT", "sid S-0007 g/MAT",
            "sid/S-0007 g MAT", "sid/S-0007 g/\u2003", "sid/S-0007 g/\u00A0",
            "sid/S-0007 g/MAT\u2003extra", "sid/S-0007 g/MAT\u00A0extra"};
        for (String args : invalid) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals("Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE",
                    error.getMessage(), args);
        }
    }

    @Test
    void parse_validUnknownIdLengths_defersLookupWithoutAdvancingGenerator() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        long previous = Long.parseLong(StudentId.generate().value.substring(2));
        String dynamicStudentId = "S-" + String.format(Locale.ROOT, "%04d", Math.addExact(previous, 2L));
        String[] unknownStudentIds = {"S-0000", "S-9999", "S-10000", "S-00007", dynamicStudentId,
            "S-" + "9".repeat(1000)};
        for (String studentId : unknownStudentIds) {
            assertTrue(model.findStudent(studentId).isEmpty(), "Test student ID must be absent: " + studentId);
            UnassignGroupCommand command = parser.parse("sid/" + studentId + " g/MAT");

            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));

            assertEquals("Student " + studentId + " does not exist.", error.getMessage());
            assertEquals(before, model.getAddressBook());
        }
        assertEquals(previous + 1, Long.parseLong(StudentId.generate().value.substring(2)));
    }

    @Test
    void parse_unconfiguredSingleTokenGroup_defersCatalogueValidation() throws Exception {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        Map<String, String> unknownGroups = Map.of("missing", "MISSING", "MAT/extra", "MAT/EXTRA", "/MAT", "/MAT");
        for (Map.Entry<String, String> entry : unknownGroups.entrySet()) {
            UnassignGroupCommand command = parser.parse("sid/S-0007 g/" + entry.getKey());

            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));

            assertEquals("Group " + entry.getValue()
                    + " is not configured. Run list-groups to see available groups.", error.getMessage());
            assertEquals(before, model.getAddressBook());
        }
    }

    private static ModelManager model() {
        AddressBook register = getUnlinkedAddressBook();
        Student student = register.findStudent("S-0007").orElseThrow();
        register.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));
        register.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("S2-MAT-A")));
        return new ModelManager(register, new UserPrefs());
    }
}
