package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * API for application state, including student records and validated group assignments.
 */
public interface Model {
    /** Always-true predicate retained for retired Person command source compatibility. */
    @Deprecated Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** Compatibility view for retired Address Book commands; the student register contains no persons. */
    @Deprecated
    default ObservableList<Person> getFilteredPersonList() {
        return FXCollections.observableArrayList();
    }

    /** Retired Person commands are unsupported in the student-only application. */
    @Deprecated
    default boolean hasPerson(Person person) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default void addPerson(Person person) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default void deletePerson(Person person) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default void setPerson(Person target, Person edited) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    default void updateFilteredPersonList(Predicate<Person> predicate) {
        throw new UnsupportedOperationException();
    }

    ReadOnlyUserPrefs getUserPrefs();
    GuiSettings getGuiSettings();
    void setGuiSettings(GuiSettings guiSettings);
    void setAddressBook(ReadOnlyAddressBook addressBook);
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns the immutable configured groups, separate from persisted student data.
     *
     * @return The shared catalogue in configured display order.
     */
    GroupCatalogue getGroupCatalogue();

    /**
     * Resolves exact ID text against the authoritative register without allocating or reserving an ID.
     *
     * @param studentId The stable ID text to look up.
     * @return The matching student, or an empty optional if the text does not identify a registered student.
     * @throws NullPointerException If {@code studentId} is null.
     */
    Optional<Student> findStudent(String studentId);

    /**
     * Adds a relationship after validating both references and canonical pair uniqueness.
     * Validation failure leaves the register unchanged.
     *
     * @param assignment The student/group pair to add.
     * @throws IllegalArgumentException If a reference is unknown or the pair already exists.
     * @throws NullPointerException If {@code assignment} is null.
     */
    void addGroupAssignment(GroupAssignment assignment);

    /**
     * Returns a student's assigned groups in catalogue order as an immutable snapshot.
     *
     * @param studentId The exact stable ID text to look up, without allocating an ID.
     * @return The assigned groups, or an empty list for an unknown student or a student without assignments.
     * @throws NullPointerException If {@code studentId} is null.
     */
    List<Group> getAssignedGroups(String studentId);

    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }
    default boolean hasStudent(Student student) {
        return false;
    }

    default void addStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    default void deleteStudent(StudentId id) {
        throw new UnsupportedOperationException();
    }
}
