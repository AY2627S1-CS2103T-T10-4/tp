package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class EditGuardianCommandTest {
    @Test
    void execute_updatesSharedGuardianAndReportsLinkedProfiles() throws Exception {
        ModelManager model = new ModelManager();
        Guardian guardian = guardian("G-0003", "Anita Lim", "91234567");
        model.addGuardian(guardian);
        model.addStudent(student("S-0001"));
        model.addStudent(student("S-0002"));
        model.linkGuardian("S-0001", "G-0003");
        model.linkGuardian("S-0002", "G-0003");

        CommandResult result = new EditGuardianCommand("G-0003", new GuardianPhone("+65 98765432")).execute(model);

        assertEquals("Updated guardian G-0003 phone to +65 98765432. Updated contact will appear in 2 linked "
                + "student profile(s).", result.getFeedbackToUser());
        assertEquals(new GuardianPhone("98765432"), model.getLinkedGuardian("S-0001").orElseThrow().getPhone());
        assertEquals(new GuardianPhone("98765432"), model.getLinkedGuardian("S-0002").orElseThrow().getPhone());
    }

    @Test
    void execute_unknownIdAndDuplicatePhone_failWithoutMutation() {
        ModelManager model = new ModelManager();
        model.addGuardian(guardian("G-0003", "Anita Lim", "91234567"));
        model.addGuardian(guardian("G-0004", "Raj Lim", "81234567"));

        EditGuardianCommand unknownCommand = new EditGuardianCommand("G-9999", new GuardianPhone("98765432"));
        CommandException unknown = assertThrows(CommandException.class, () -> unknownCommand.execute(model));
        assertEquals("Guardian G-9999 does not exist.", unknown.getMessage());
        EditGuardianCommand duplicateCommand = new EditGuardianCommand("G-0003", new GuardianPhone("+65 81234567"));
        CommandException duplicate = assertThrows(CommandException.class, () -> duplicateCommand.execute(model));
        assertEquals("Phone +65 81234567 is already used by guardian G-0004.", duplicate.getMessage());
        assertEquals(new GuardianPhone("91234567"), model.findGuardian("G-0003").orElseThrow().getPhone());
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }

    private static Student student(String id) {
        return new Student(new StudentId(id), new StudentName(id.endsWith("1") ? "Mei Lin" : "Raj Kumar"),
                new AcademicLevel("Sec 2"));
    }
}
