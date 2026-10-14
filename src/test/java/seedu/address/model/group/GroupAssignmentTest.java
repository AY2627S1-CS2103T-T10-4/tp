package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.StudentId;

class GroupAssignmentTest {
    @Test
    void constructor_retainsExistingIdAndCanonicalCode() {
        StudentId id = new StudentId("S-0601");
        GroupAssignment assignment = new GroupAssignment(id, new GroupCode(" mat "));

        assertSame(id, assignment.getStudentId());
        assertEquals("MAT", assignment.getGroupCode().getValue());
        assertThrows(NullPointerException.class, () -> new GroupAssignment(null, new GroupCode("MAT")));
        assertThrows(NullPointerException.class, () -> new GroupAssignment(id, null));
    }

    @Test
    void equalsAndHashCode_useStudentIdAndCanonicalGroupIdentity() {
        StudentId id = new StudentId("S-0601");
        GroupAssignment assignment = new GroupAssignment(id, new GroupCode("mat"));
        GroupAssignment equivalent = new GroupAssignment(new StudentId("S-0601"), new GroupCode("MAT"));

        assertEquals(assignment, equivalent);
        assertEquals(assignment.hashCode(), equivalent.hashCode());
        assertNotEquals(assignment, new GroupAssignment(id, new GroupCode("SCI")));
        assertNotEquals(assignment, new GroupAssignment(new StudentId("S-0602"), new GroupCode("MAT")));
        assertNotEquals(null, assignment);
    }
}
