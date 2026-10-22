package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class ListStudentsCommandTest {
    @Test
    public void execute_afterSearch_restoresCompleteList() throws Exception {
        ModelManager model = new ModelManager();
        Student amy = student("S-1201", "Amy Tan");
        Student mei = student("S-1202", "Mei Lin");
        model.addStudent(amy);
        model.addStudent(mei);
        model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
        new FilterStudentsCommand(new GroupCode("MAT")).execute(model);

        CommandResult result = new ListStudentsCommand().execute(model);

        assertEquals("Listed 2 student(s).", result.getFeedbackToUser());
        assertEquals(List.of(amy, mei), model.getStudentList());
        assertEquals("", model.getStudentFilterSummary());
    }

    @Test
    public void execute_emptyRegister_returnsEmptyMessage() {
        CommandResult result = new ListStudentsCommand().execute(new ModelManager());

        assertEquals("No students registered.", result.getFeedbackToUser());
    }

    @Test
    public void equals_anyListStudentsCommand_isEqual() {
        ListStudentsCommand command = new ListStudentsCommand();
        ListStudentsCommand equivalent = new ListStudentsCommand();

        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListStudentsCommand().execute(null));
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
