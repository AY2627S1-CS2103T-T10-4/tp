package seedu.address.model.group;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.model.student.StudentId;

/**
 * An immutable student/group pair with equality based on stable ID and canonical group code.
 * The owning aggregate validates reference existence and pair uniqueness.
 */
public final class GroupAssignment {
    private final StudentId studentId;
    private final GroupCode groupCode;

    /**
     * Creates a relationship using an existing student ID and a canonical group code.
     *
     * @param studentId The existing ID to retain without allocating or reserving another ID.
     * @param groupCode The canonical group identity.
     * @throws NullPointerException If either value is null.
     */
    public GroupAssignment(StudentId studentId, GroupCode groupCode) {
        requireAllNonNull(studentId, groupCode);
        this.studentId = studentId;
        this.groupCode = groupCode;
    }

    /**
     * Returns the stable ID of the assigned student.
     *
     * @return The immutable student ID supplied at construction.
     */
    public StudentId getStudentId() {
        return studentId;
    }

    /**
     * Returns the canonical code of the assigned group.
     *
     * @return The immutable group code supplied at construction.
     */
    public GroupCode getGroupCode() {
        return groupCode;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GroupAssignment assignment
                && studentId.equals(assignment.studentId) && groupCode.equals(assignment.groupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, groupCode);
    }

    @Override
    public String toString() {
        return studentId + " -> " + groupCode;
    }
}
