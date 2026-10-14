package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
