package seedu.address.model;

import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/**
 * Read-only view of Mentora's student records and validated group relationships.
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

    /** Empty compatibility view retained for legacy callers; persons are no longer stored. */
    @Deprecated
    default ObservableList<Person> getPersonList() {
        return FXCollections.observableArrayList();
    }
}
