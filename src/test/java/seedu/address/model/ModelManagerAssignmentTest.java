package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class ModelManagerAssignmentTest {
    @Test
    void equalsAndHashCode_equivalentAssignmentsAndPreferences_areConsistent() {
        ModelManager original = new ModelManager();
        Student student = new Student(new StudentId("S-0601"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        original.addStudent(student);
        original.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));
        ModelManager copy = new ModelManager(original.getAddressBook(), original.getUserPrefs());

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        copy.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("SCI")));
        assertNotEquals(original, copy);
    }

    @Test
    void getAssignedGroups_returnsReadOnlySnapshotInCatalogueOrder() {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-0601"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        model.addStudent(student);
        List<Group> before = model.getAssignedGroups("S-0601");
        model.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("S2-MAT-A")));
        model.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));

        List<Group> groups = model.getAssignedGroups("S-0601");

        assertTrue(before.isEmpty());
        assertEquals(List.of("MAT", "S2-MAT-A"), groups.stream().map(group -> group.getCode().getValue()).toList());
        assertThrows(UnsupportedOperationException.class, groups::clear);
        assertTrue(model.getAssignedGroups("S-0602").isEmpty());
    }

    @Test
    void removeGroupAssignment_hiddenStudent_updatesProjectionWithoutChangingFilter() {
        Model model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        Student mei = model.findStudent("S-0007").orElseThrow();
        Student amy = model.findStudent("S-0008").orElseThrow();
        GroupAssignment subject = new GroupAssignment(mei.getId(), new GroupCode("MAT"));
        GroupAssignment classAssignment = new GroupAssignment(mei.getId(), new GroupCode("S2-MAT-A"));
        model.addGroupAssignment(subject);
        model.addGroupAssignment(classAssignment);
        model.addGroupAssignment(new GroupAssignment(amy.getId(), new GroupCode("MAT")));
        model.updateFilteredStudentList(student -> student.equals(amy));
        List<Group> before = model.getAssignedGroups("S-0007");
        List<Group> catalogueBefore = List.copyOf(model.getGroupCatalogue().getGroups());

        model.removeGroupAssignment(subject);

        List<Group> after = model.getAssignedGroups("S-0007");
        assertEquals(List.of("S2-MAT-A"), after.stream().map(group -> group.getCode().getValue()).toList());
        assertEquals(List.of("MAT", "S2-MAT-A"), before.stream().map(group -> group.getCode().getValue()).toList());
        assertThrows(UnsupportedOperationException.class, after::clear);
        assertEquals(List.of(amy), model.getStudentList());
        assertSame(mei, model.findStudent("S-0007").orElseThrow());
        assertEquals(catalogueBefore, model.getGroupCatalogue().getGroups());

        model.removeGroupAssignment(classAssignment);

        assertTrue(model.getAssignedGroups("S-0007").isEmpty());
        assertEquals(1, after.size());
        assertEquals(List.of("MAT"), model.getAssignedGroups("S-0008").stream()
                .map(group -> group.getCode().getValue()).toList());
        assertEquals(List.of(amy), model.getStudentList());
    }

    @Test
    void removeGroupAssignment_absentPair_propagatesFailureWithoutChangingModel() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        Student student = model.findStudent("S-0007").orElseThrow();
        model.updateFilteredStudentList(candidate -> candidate.equals(student));
        ModelManager before = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        before.updateFilteredStudentList(candidate -> candidate.equals(student));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                model.removeGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("s2-mat-a"))));

        assertEquals("Student S-0007 is not assigned to S2-MAT-A.", error.getMessage());
        assertEquals(before, model);
        assertEquals(List.of(student), model.getStudentList());
    }
}
