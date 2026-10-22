package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class FilterStudentsCommandTest {
    private final Student amy = student("S-1801", "Amy Tan", "Sec 2");
    private final Student mei = student("S-1802", "Mei Lin", "Sec 2");
    private final Student kumar = student("S-1803", "S. Kumar", "IP 4");
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addStudent(kumar);
        model.addStudent(mei);
        model.addStudent(amy);
        model.addGroupAssignment(assignment(amy, "SCI"));
        model.addGroupAssignment(assignment(mei, "MAT"));
        model.addGroupAssignment(assignment(mei, "SCI"));
    }

    @Test
    public void execute_subjectGroup_filtersAssignedStudentsAndSetsSummary() throws Exception {
        CommandResult result = groupCommand("mat").execute(model);

        assertEquals("Found 1 student(s) assigned to MAT.", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertEquals(List.of(mei), model.getStudentList());
        assertEquals("Filtered by group: MAT — Mathematics", model.getStudentFilterSummary());
        assertEquals(3, model.getAddressBook().getStudentList().size());
        assertTrue(groupCommand("MAT").isReadOnly());
    }

    @Test
    public void execute_subjectGroupWithNoAssignments_returnsEmptyResult() throws Exception {
        CommandResult result = groupCommand("CHI").execute(model);

        assertEquals("No students are assigned to CHI.", result.getFeedbackToUser());
        assertEquals(List.of(), model.getStudentList());
        assertEquals("Filtered by group: CHI — Chinese", model.getStudentFilterSummary());
    }

    @Test
    public void execute_academicLevel_filtersStudentsAndSetsSummary() throws Exception {
        CommandResult result = levelCommand("sec 2").execute(model);

        assertEquals("Found 2 student(s) at Sec 2.", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertEquals(List.of(amy, mei), model.getStudentList());
        assertEquals("Filtered by academic level: Sec 2", model.getStudentFilterSummary());
    }

    @Test
    public void execute_academicLevelWithNoStudents_returnsEmptyResult() throws Exception {
        CommandResult result = levelCommand("IP 6").execute(model);

        assertEquals("No students are at IP 6.", result.getFeedbackToUser());
        assertEquals(List.of(), model.getStudentList());
        assertEquals("Filtered by academic level: IP 6", model.getStudentFilterSummary());
    }

    @Test
    public void execute_unknownOrClassGroup_preservesCurrentFilter() throws Exception {
        groupCommand("SCI").execute(model);
        List<Student> studentsBeforeError = List.copyOf(model.getStudentList());
        String summaryBeforeError = model.getStudentFilterSummary();

        CommandException unknown = assertThrows(CommandException.class, () -> groupCommand("ART").execute(model));
        assertEquals("Group ART is not configured. Run list-groups to see available groups.", unknown.getMessage());
        assertEquals(studentsBeforeError, model.getStudentList());
        assertEquals(summaryBeforeError, model.getStudentFilterSummary());

        CommandException classGroup = assertThrows(CommandException.class, () ->
                groupCommand("S2-MAT-A").execute(model));
        assertEquals("Group S2-MAT-A is a Class group; use the academic-level filter.", classGroup.getMessage());
        assertEquals(studentsBeforeError, model.getStudentList());
        assertEquals(summaryBeforeError, model.getStudentFilterSummary());
    }

    @Test
    public void assignmentChanges_refreshActiveGroupFilter() throws Exception {
        groupCommand("MAT").execute(model);
        model.addGroupAssignment(assignment(amy, "MAT"));
        assertEquals(List.of(amy, mei), model.getStudentList());

        model.removeGroupAssignment(assignment(mei, "MAT"));
        assertEquals(List.of(amy), model.getStudentList());
        assertEquals("Filtered by group: MAT — Mathematics", model.getStudentFilterSummary());
    }

    @Test
    public void equals_sameVariantAndValue_areEqual() {
        assertEquals(groupCommand("mat"), groupCommand("MAT"));
        assertEquals(levelCommand("sec 2"), levelCommand("SEC 2"));
        assertNotEquals(groupCommand("MAT"), groupCommand("SCI"));
        assertNotEquals(levelCommand("Sec 2"), levelCommand("Sec 3"));
        assertNotEquals(groupCommand("MAT"), levelCommand("Sec 2"));
        assertNotEquals(groupCommand("MAT"), null);
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FilterStudentsCommand((GroupCode) null));
        assertThrows(NullPointerException.class, () -> new FilterStudentsCommand((AcademicLevel) null));
        assertThrows(NullPointerException.class, () -> groupCommand("MAT").execute(null));
    }

    private static FilterStudentsCommand groupCommand(String code) {
        return new FilterStudentsCommand(new GroupCode(code));
    }

    private static FilterStudentsCommand levelCommand(String level) {
        return new FilterStudentsCommand(new AcademicLevel(level));
    }

    private static GroupAssignment assignment(Student student, String groupCode) {
        return new GroupAssignment(student.getId(), new GroupCode(groupCode));
    }

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
