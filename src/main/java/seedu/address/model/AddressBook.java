package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** Stores all student and guardian records managed by Mentora. */
public class AddressBook implements ReadOnlyAddressBook {
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Guardian> guardians = FXCollections.observableArrayList();

    public AddressBook() { }

    /** Creates a copy of the supplied Mentora register. */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        resetData(toBeCopied);
    }

    /** Replaces the current student and guardian records. */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        setStudents(newData.getStudentList());
        setGuardians(newData.getGuardianList());
    }

    /** Replaces student records while enforcing unique IDs and identities. */
    public void setStudents(List<Student> newStudents) {
        students.clear();
        newStudents.forEach(this::addStudent);
    }

    /** Replaces guardian records while enforcing unique IDs and phone numbers. */
    public void setGuardians(List<Guardian> newGuardians) {
        guardians.clear();
        newGuardians.forEach(this::addGuardian);
    }

    /** Returns whether a student with the same ID or normalized name and level exists. */
    public boolean hasStudent(Student student) {
        return students.stream().anyMatch(existing -> existing.getId().equals(student.getId())
                || existing.sameIdentity(student));
    }

    /** Returns whether the student ID is present. */
    public boolean hasStudentId(StudentId id) {
        requireNonNull(id);
        return students.stream().anyMatch(student -> student.getId().equals(id));
    }

    /** Adds a student while enforcing unique IDs and identities. */
    public void addStudent(Student student) {
        requireNonNull(student);
        if (hasStudent(student)) {
            throw new IllegalArgumentException("Duplicate student ID or identity.");
        }
        students.add(student);
        sortStudents();
    }

    /** Returns whether a guardian with the same ID or canonical phone number exists. */
    public boolean hasGuardian(Guardian guardian) {
        requireNonNull(guardian);
        return guardians.stream().anyMatch(existing -> existing.getId().equals(guardian.getId())
                || existing.hasSameIdentityAs(guardian));
    }

    /** Returns whether the guardian ID is present. */
    public boolean hasGuardianId(GuardianId id) {
        requireNonNull(id);
        return guardians.stream().anyMatch(guardian -> guardian.getId().equals(id));
    }

    /** Finds the guardian using a canonical phone number. */
    public Optional<Guardian> findGuardianByPhone(GuardianPhone phone) {
        requireNonNull(phone);
        return guardians.stream().filter(guardian -> guardian.getPhone().equals(phone)).findFirst();
    }

    /** Adds a guardian while enforcing unique IDs and phone numbers. */
    public void addGuardian(Guardian guardian) {
        requireNonNull(guardian);
        if (hasGuardian(guardian)) {
            throw new IllegalArgumentException("Duplicate guardian ID or phone number.");
        }
        guardians.add(guardian);
        sortGuardians();
    }

    /** Removes the student with the given stable ID. */
    public void deleteStudent(StudentId id) {
        requireNonNull(id);
        students.removeIf(student -> student.getId().equals(id));
    }

    /** Retired Person API retained only to keep legacy callers source-compatible. */
    @Deprecated
    public void setPersons(List<Person> persons) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean hasPerson(Person person) {
        return false;
    }

    @Deprecated
    public void addPerson(Person person) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void removePerson(Person person) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setPerson(Person target, Person edited) {
        throw new UnsupportedOperationException();
    }

    private void sortStudents() {
        students.sort(Comparator.comparing((Student student) -> student.getName().normalized())
                .thenComparing(Student::getId));
    }

    private void sortGuardians() {
        guardians.sort(Comparator.comparing((Guardian guardian) -> guardian.getName().getNormalized())
                .thenComparing(Guardian::getId));
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(students);
    }

    @Override
    public ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(guardians);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("students", students).add("guardians", guardians).toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddressBook addressBook && students.equals(addressBook.students)
                && guardians.equals(addressBook.guardians);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(students, guardians);
    }
}
