package seedu.address.model;

import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/**
 * Read-only view of all students, guardians and validated group relationships.
 */
public interface ReadOnlyAddressBook {
    /**
     * Returns an unmodifiable view of all students.
     *
     * @return The current students; subsequent aggregate changes are reflected in this view.
     */
    ObservableList<Student> getStudentList();

    /**
     * Returns an unmodifiable view of the immutable student/group relationships.
     *
     * @return The current assignments; subsequent aggregate changes are reflected in this view.
     */
    Set<GroupAssignment> getGroupAssignments();

    /** Returns an unmodifiable view of all guardians. */
    default ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Empty compatibility view retained for legacy callers; persons are no longer stored. */
    @Deprecated
    default ObservableList<Person> getPersonList() {
        return FXCollections.observableArrayList();
    }
}
