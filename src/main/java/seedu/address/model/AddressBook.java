package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final javafx.collections.ObservableList<Student> students = FXCollections.observableArrayList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setStudents(newData.getStudentList());
    }

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
        if (hasStudent(student)) {
            throw new IllegalArgumentException("Duplicate student ID or identity.");
        }
        students.add(student);
        students.sort(Comparator.comparing((Student s) -> s.getName().normalized()).thenComparing(Student::getId));
    }

    public boolean hasStudentId(StudentId id) {
        return students.stream().anyMatch(student -> student.getId().equals(id));
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this).add("persons", persons);
        if (!students.isEmpty()) {
            builder.add("students", students);
        }
        return builder.toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(students);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons) && students.equals(otherAddressBook.students);
    }

    @Override
    public int hashCode() {
        return 31 * persons.hashCode() + students.hashCode();
    }
}
