package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentPhone;

class AddStudentCommandTest {
    @Test
    void execute_addsStudentAndReturnsId() throws Exception {
        ModelManager model = new ModelManager();
        CommandResult result = new AddStudentCommand(new StudentName("Mei Lin"), new AcademicLevel("Sec 2"))
                .execute(model);
        assertEquals(1, model.getStudentList().size());
        assertEquals("Added student " + model.getStudentList().get(0).getId() + ": Mei Lin (Sec 2).",
                result.getFeedbackToUser());
        assertEquals(model.getStudentList().get(0).getId().value, result.getSelectedStudentId());
    }

    @Test
    void execute_duplicateIdentity_throwsAndDoesNotAdd() throws Exception {
        ModelManager model = new ModelManager();
        new AddStudentCommand(new StudentName("Mei Lin"), new AcademicLevel("Sec 2")).execute(model);
        AddStudentCommand duplicate = new AddStudentCommand(new StudentName("mei   lin"),
                new AcademicLevel("sec 2"));
        Executable execution = () -> duplicate.execute(model);
        assertThrows(CommandException.class, "A student named mei lin at Sec 2 already exists.", execution);
        assertEquals(1, model.getStudentList().size());
    }

    @Test
    void execute_unicodeCaseDuplicate_rejectsSameLevel() throws Exception {
        ModelManager model = new ModelManager();
        new AddStudentCommand(new StudentName("ΟΣ"), new AcademicLevel("Sec 1")).execute(model);

        assertThrows(CommandException.class, () ->
                new AddStudentCommand(new StudentName("οσ"), new AcademicLevel("Sec 1")).execute(model));
        assertEquals(1, model.getStudentList().size());
    }

    @Test
    void execute_addsOptionalPhoneWithoutChangingDuplicateRule() throws Exception {
        ModelManager model = new ModelManager();
        CommandResult result = new AddStudentCommand(new StudentName("Mei Lin"), new AcademicLevel("Sec 2"),
                new StudentPhone("+65 91234567")).execute(model);
        assertEquals("Added student " + model.getStudentList().get(0).getId()
                + ": Mei Lin (Sec 2) with phone +65 91234567.", result.getFeedbackToUser());
        assertEquals("+65 91234567", model.findStudent(result.getSelectedStudentId()).orElseThrow()
                .getPhone().orElseThrow().toString());
        assertThrows(CommandException.class, () -> new AddStudentCommand(new StudentName("mei lin"),
                new AcademicLevel("sec 2")).execute(model));
    }
}
