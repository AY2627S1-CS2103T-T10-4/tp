package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.person.Person;

/** Stores all student records managed by Mentora. */
public class AddressBook implements ReadOnlyAddressBook {
    private final ObservableList<Student> students = FXCollections.observableArrayList();

    public AddressBook() { }

    /** Creates a copy of the supplied student register. */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        resetData(toBeCopied);
    }

    /** Replaces the current student records. */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        setStudents(newData.getStudentList());
    }

    /** Replaces student records while enforcing unique IDs and identities. */
    public void setStudents(List<Student> newStudents) {
        students.clear();
        newStudents.forEach(this::addStudent);
    }

    /** Returns whether a student with the same ID or normalized name and level exists. */
    public boolean hasStudent(Student student) {
        return students.stream().anyMatch(existing -> existing.getId().equals(student.getId())
                || existing.sameIdentity(student));
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

    /** Removes the student with the given stable ID. */
    public void deleteStudent(StudentId id) {
        requireNonNull(id);
        students.removeIf(student -> student.getId().equals(id));
    }

    /** Retired Person API retained only to keep legacy callers source-compatible. */
    @Deprecated public void setPersons(List<Person> persons) { throw new UnsupportedOperationException(); }
    @Deprecated public boolean hasPerson(Person person) { return false; }
    @Deprecated public void addPerson(Person person) { throw new UnsupportedOperationException(); }
    @Deprecated public void removePerson(Person person) { throw new UnsupportedOperationException(); }

    private void sortStudents() {
        students.sort(Comparator.comparing((Student student) -> student.getName().normalized())
                .thenComparing(Student::getId));
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(students);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("students", students).toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddressBook addressBook && students.equals(addressBook.students);
    }

    @Override
    public int hashCode() {
        return students.hashCode();
    }
}
