package seedu.address.model;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/** Read-only view of Mentora's student records. */
public interface ReadOnlyAddressBook {
    /** Returns an unmodifiable view of all students. */
    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Empty compatibility view retained for legacy callers; persons are no longer stored. */
    @Deprecated
    default ObservableList<Person> getPersonList() {
        return FXCollections.observableArrayList();
    }
}
