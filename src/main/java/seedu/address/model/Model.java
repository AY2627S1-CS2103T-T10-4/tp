package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** API for application state. */
public interface Model {
    /** Always-true predicate retained for retired Person command source compatibility. */
    @Deprecated Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** Predicate that displays every student in the register. */
    Predicate<Student> PREDICATE_SHOW_ALL_STUDENTS = unused -> true;

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
    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Updates the students shown in the UI using the supplied predicate. */
    default void updateFilteredStudentList(Predicate<Student> predicate) {
        throw new UnsupportedOperationException();
    }

    default boolean hasStudent(Student student) {
        return false;
    }

    default Optional<Student> findStudent(StudentId id) {
        return getStudentList().stream().filter(student -> student.getId().equals(id)).findFirst();
    }

    default void updateStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    default void addStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    default void deleteStudent(StudentId id) {
        throw new UnsupportedOperationException();
    }

    /** Returns an unmodifiable view of all guardians. */
    default ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Returns whether a guardian with the same ID or phone exists. */
    default boolean hasGuardian(Guardian guardian) {
        return false;
    }

    /** Finds a guardian by canonical phone number. */
    default Optional<Guardian> findGuardianByPhone(GuardianPhone phone) {
        return Optional.empty();
    }

    /** Adds a guardian to the model. */
    default void addGuardian(Guardian guardian) {
        throw new UnsupportedOperationException();
    }
}
