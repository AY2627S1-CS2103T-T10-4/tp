package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/** Read-only view of Mentora's student and guardian records. */
public interface ReadOnlyAddressBook {
    /** Returns an unmodifiable view of all students. */
    ObservableList<Student> getStudentList();

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
