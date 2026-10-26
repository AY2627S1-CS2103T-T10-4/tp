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
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * API for application state, including students, guardians, group assignments and the visible Student filter.
 */
public interface Model {
    /** Always-true predicate retained for retired Person command source compatibility. */
    @Deprecated Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** Predicate that displays every student in the register. */
    Predicate<Student> PREDICATE_SHOW_ALL_STUDENTS = unused -> true;

    /** Predicate that displays every guardian in the register. */
    Predicate<Guardian> PREDICATE_SHOW_ALL_GUARDIANS = unused -> true;

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

    /** Adds a new configured group. */
    default void addGroup(Group group) {
        throw new UnsupportedOperationException();
    }

    /** Deletes an existing unassigned group. */
    default void deleteGroup(GroupCode code) {
        throw new UnsupportedOperationException();
    }

    /**
     * Resolves exact ID text against the authoritative register without allocating or reserving an ID.
     *
     * @param studentId The stable ID text to look up.
     * @return The matching student, or an empty optional if the text does not identify a registered student.
     * @throws NullPointerException If {@code studentId} is null.
     */
    Optional<Student> findStudent(String studentId);

    /**
     * Resolves an existing ID against the full register, independent of the filtered view.
     *
     * @param id The previously constructed student ID; use {@link #findStudent(String)} for user input.
     * @return The matching student, or an empty optional if absent.
     * @throws NullPointerException If {@code id} is null.
     */
    default Optional<Student> findStudent(StudentId id) {
        return findStudent(id.value);
    }

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
     * Removes one existing assignment from the in-memory register, including a student's last assignment.
     * Validates the student, configured group and membership before mutation; rejection leaves the register unchanged.
     * Preserves all student and guardian records, guardian links, other assignments and the configured catalogue.
     * This operation does not save data to disk.
     *
     * @param assignment The student/group pair to remove, using the existing student's ID.
     * @throws IllegalArgumentException If the student or configured group is unknown, or the pair is not assigned.
     * @throws NullPointerException If {@code assignment} is null.
     */
    void removeGroupAssignment(GroupAssignment assignment);

    /**
     * Returns a student's assigned groups in catalogue order as an immutable snapshot.
     *
     * @param studentId The exact stable ID text to look up, without allocating an ID.
     * @return The assigned groups, or an empty list for an unknown student or a student without assignments.
     * @throws NullPointerException If {@code studentId} is null.
     */
    List<Group> getAssignedGroups(String studentId);

    /**
     * Returns the current filtered Student view.
     * Use {@link #findStudent(String)} or the complete register for authoritative lookup and persistence.
     *
     * @return An unmodifiable observable view of the students matching the active filter.
     */
    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Updates the students shown in the UI using the supplied predicate. */
    default void updateFilteredStudentList(Predicate<Student> predicate) {
        throw new UnsupportedOperationException();
    }

    /** Updates the visible students and records a human-readable summary of the active filter. */
    default void updateFilteredStudentList(Predicate<Student> predicate, String filterSummary) {
        updateFilteredStudentList(predicate);
    }

    /** Returns the summary of the active student filter, or an empty string when none is active. */
    default String getStudentFilterSummary() {
        return "";
    }

    default boolean hasStudent(Student student) {
        return false;
    }

    /**
     * Replaces an existing student after validating identity across the complete register.
     * Preserves assignments and guardians. Validation failure must leave all data unchanged.
     *
     * @param student The replacement with the same stable ID.
     * @throws IllegalArgumentException If the ID is absent or the normalized name/level duplicates another student.
     * @throws NullPointerException If {@code student} is null.
     */
    default void updateStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    default void addStudent(Student student) {
        throw new UnsupportedOperationException();
    }

    default void deleteStudent(StudentId id) {
        throw new UnsupportedOperationException();
    }

    /** Returns the current filtered Guardian view. */
    default ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Updates the guardians shown in the UI using the supplied predicate. */
    default void updateFilteredGuardianList(Predicate<Guardian> predicate) {
        throw new UnsupportedOperationException();
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

    /** Replaces a guardian by stable ID while retaining all student links. */
    default void updateGuardian(Guardian guardian) {
        throw new UnsupportedOperationException();
    }

    /** Looks up a guardian in the full register without allocating an ID. */
    default Optional<Guardian> findGuardian(String guardianId) {
        return getAddressBook().findGuardian(guardianId);
    }

    /** Returns the student's current guardian record, or empty if the student has no link. */
    default Optional<Guardian> getLinkedGuardian(String studentId) {
        return getAddressBook().getLinkedGuardian(studentId);
    }

    /** Returns the students linked to a guardian in the register's display order. */
    default List<Student> getLinkedStudents(String guardianId) {
        return List.of();
    }

    /**
     * Removes a student's guardian link and returns the former guardian ID.
     * Rejects unknown or unlinked students without changing the register.
     */
    default GuardianId unlinkGuardian(String studentId) {
        throw new UnsupportedOperationException();
    }

    /** Links existing records, rejecting missing IDs and students who already have a guardian. */
    default void linkGuardian(String studentId, String guardianId) {
        throw new UnsupportedOperationException();
    }
}
