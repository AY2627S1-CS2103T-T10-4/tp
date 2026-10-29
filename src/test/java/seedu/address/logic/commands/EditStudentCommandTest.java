package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;

class EditStudentCommandTest {
    @Test
    void execute_updatesStudentAndReportsLevel() throws Exception {
        ModelManager model = new ModelManager();
        Student original = student("S-0007", "Mei Lin", "Sec 2");
        model.addStudent(original);

        CommandResult result = new EditStudentCommand(original.getId(), new AcademicLevel("Sec 3")).execute(model);

        assertEquals(student("S-0007", "Mei Lin", "Sec 3"), model.getStudentList().get(0));
        assertEquals("Updated student S-0007: academic level is now Sec 3.", result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
    }

    @Test
    void execute_unknownIdOrDuplicateIdentity_failsWithoutChangingRecords() throws Exception {
        ModelManager model = new ModelManager();
        Student target = student("S-0007", "Mei Lin", "Sec 2");
        model.addStudent(target);
        model.addStudent(student("S-0008", "Mei Lin", "Sec 3"));

        assertEquals("Student S-9999 does not exist.", assertThrows(CommandException.class, () ->
                new EditStudentCommand(new StudentId("S-9999"), new AcademicLevel("Sec 3")).execute(model))
                .getMessage());
        assertEquals("Cannot update S-0007: a student named Mei Lin at Sec 3 already exists.",
                assertThrows(CommandException.class, () ->
                        new EditStudentCommand(target.getId(), new AcademicLevel("Sec 3")).execute(model))
                        .getMessage());
        assertEquals("Sec 2", model.findStudent(target.getId()).orElseThrow().getAcademicLevel().toString());
    }

    @Test
    void execute_unicodeCaseDuplicateInFullRegister_rejectsLevelChange() {
        ModelManager model = new ModelManager();
        model.addStudent(student("S-0007", "ΟΣ", "Sec 1"));
        Student target = student("S-0008", "οσ", "Sec 2");
        model.addStudent(target);
        model.updateFilteredStudentList(student -> student.getAcademicLevel().equals(new AcademicLevel("Sec 2")));

        assertEquals("Cannot update S-0008: a student named οσ at Sec 1 already exists.",
                assertThrows(CommandException.class, () ->
                        new EditStudentCommand(target.getId(), new AcademicLevel("Sec 1")).execute(model))
                        .getMessage());
        assertEquals(target, model.findStudent(target.getId()).orElseThrow());
        assertEquals(2, model.getAddressBook().getStudentList().size());
    }

    @Test
    void execute_unknownLargeId_doesNotAdvanceIdGenerator() {
        ModelManager model = new ModelManager();

        assertThrows(CommandException.class, () ->
                new EditStudentCommand("S-999999999999", new AcademicLevel("Sec 2")).execute(model));

        long nextId = Long.parseLong(StudentId.generate().value.substring(2));
        assertTrue(nextId < 999999999999L);
    }

    @Test
    void execute_updatesPhoneAndRetainsLevel() throws Exception {
        ModelManager model = new ModelManager();
        Student original = student("S-0007", "Mei Lin", "Sec 2");
        model.addStudent(original);

        CommandResult result = new EditStudentCommand("S-0007", new StudentPhone("98765432")).execute(model);

        Student updated = model.findStudent("S-0007").orElseThrow();
        assertEquals("Sec 2", updated.getAcademicLevel().toString());
        assertEquals("+65 98765432", updated.getPhone().orElseThrow().toString());
        assertEquals("Updated student S-0007 phone to +65 98765432.", result.getFeedbackToUser());
    }

    @Test
    void execute_updatesLevelAndPhoneAndReportsBoth() throws Exception {
        ModelManager model = new ModelManager();
        Student original = student("S-0007", "Mei Lin", "Sec 2");
        model.addStudent(original);

        CommandResult result = new EditStudentCommand("S-0007", new AcademicLevel("Sec 3"),
                new StudentPhone("98765432"), true).execute(model);

        Student updated = model.findStudent("S-0007").orElseThrow();
        assertEquals("Sec 3", updated.getAcademicLevel().toString());
        assertEquals("+65 98765432", updated.getPhone().orElseThrow().toString());
        assertEquals("Updated student S-0007: academic level is now Sec 3 and phone is now +65 98765432.",
                result.getFeedbackToUser());
    }

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
