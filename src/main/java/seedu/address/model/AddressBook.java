package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public static final String MESSAGE_UNKNOWN_STUDENT = "Student %s does not exist.";
    public static final String MESSAGE_UNKNOWN_GUARDIAN = "Guardian %s does not exist.";
    public static final String MESSAGE_DUPLICATE_LINK = "Student %s is already linked to guardian %s.";
    public static final String MESSAGE_EXISTING_GUARDIAN =
            "Student %s already has guardian %s. Unlink it before linking another guardian.";

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Guardian> guardians = FXCollections.observableArrayList();
    private final Map<StudentId, GuardianId> guardianLinks = new LinkedHashMap<>();

    public AddressBook() { }

    /** Creates a copy of the supplied Mentora register. */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        resetData(toBeCopied);
    }

    /**
     * Replaces records and links after validating the entire replacement, including self-assignment.
     * Unchanged record lists retain their contents and selection listeners are not notified.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        AddressBook replacement = new AddressBook();
        List.copyOf(newData.getStudentList()).forEach(replacement::addStudent);
        List.copyOf(newData.getGuardianList()).forEach(replacement::addGuardian);
        newData.getGuardianLinks().forEach((studentId, guardianId) ->
                replacement.linkGuardian(studentId.toString(), guardianId.toString()));
        guardianLinks.clear();
        guardianLinks.putAll(replacement.guardianLinks);
        if (!students.equals(replacement.students)) {
            students.setAll(replacement.students);
        }
        if (!guardians.equals(replacement.guardians)) {
            guardians.setAll(replacement.guardians);
        }
    }

    /** Replaces student records while enforcing unique IDs and identities. */
    public void setStudents(List<Student> newStudents) {
        AddressBook replacement = new AddressBook();
        List.copyOf(newStudents).forEach(replacement::addStudent);
        guardianLinks.keySet().removeIf(studentId -> !replacement.hasStudentId(studentId));
        students.setAll(replacement.students);
    }

    /** Replaces guardian records while enforcing unique IDs and phone numbers. */
    public void setGuardians(List<Guardian> newGuardians) {
        AddressBook replacement = new AddressBook();
        List.copyOf(newGuardians).forEach(replacement::addGuardian);
        for (GuardianId guardianId : guardianLinks.values()) {
            if (!replacement.hasGuardianId(guardianId)) {
                throw new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_GUARDIAN, guardianId));
            }
        }
        guardians.setAll(replacement.guardians);
    }

    /**
     * Links existing records without replacing a student's guardian or copying contact details.
     *
     * @throws IllegalArgumentException If either record is missing or the student already has a guardian.
     */
    public void linkGuardian(String studentId, String guardianId) {
        Student student = findStudent(studentId).orElseThrow(() ->
                new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_STUDENT, studentId)));
        Guardian guardian = findGuardian(guardianId).orElseThrow(() ->
                new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_GUARDIAN, guardianId)));
        GuardianId existingGuardianId = guardianLinks.get(student.getId());
        if (existingGuardianId != null) {
            String message = existingGuardianId.equals(guardian.getId())
                    ? MESSAGE_DUPLICATE_LINK : MESSAGE_EXISTING_GUARDIAN;
            throw new IllegalArgumentException(String.format(message, studentId, existingGuardianId));
        }
        guardianLinks.put(student.getId(), guardian.getId());
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
        guardianLinks.remove(id);
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
    public Map<StudentId, GuardianId> getGuardianLinks() {
        return Collections.unmodifiableMap(guardianLinks);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("students", students).add("guardians", guardians)
                .add("guardianLinks", guardianLinks).toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddressBook addressBook && students.equals(addressBook.students)
                && guardians.equals(addressBook.guardians) && guardianLinks.equals(addressBook.guardianLinks);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(students, guardians, guardianLinks);
    }
}
