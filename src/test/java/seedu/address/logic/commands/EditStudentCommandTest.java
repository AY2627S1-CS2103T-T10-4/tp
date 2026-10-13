package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

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

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
