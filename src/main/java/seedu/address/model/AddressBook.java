package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Stores student and guardian records and validated student/group relationships.
 * Each assignment references an existing student and a configured group, and each pair is unique.
 */
public class AddressBook implements ReadOnlyAddressBook {
    public static final String MESSAGE_UNKNOWN_STUDENT = "Student %s does not exist.";
    public static final String MESSAGE_UNKNOWN_GUARDIAN = "Guardian %s does not exist.";
    public static final String MESSAGE_NO_GUARDIAN = "Student %s has no guardian to unlink.";
    public static final String MESSAGE_DUPLICATE_LINK = "Student %s is already linked to guardian %s.";
    public static final String MESSAGE_EXISTING_GUARDIAN =
            "Student %s already has guardian %s. Unlink it before linking another guardian.";

    public static final String MESSAGE_UNKNOWN_GROUP =
            "Group %s is not configured. Run list-groups to see available groups.";
    public static final String MESSAGE_DUPLICATE_ASSIGNMENT = "Student %s is already assigned to %s.";
    /** Error message when an existing student has no assignment to a configured group. */
    public static final String MESSAGE_MISSING_ASSIGNMENT = "Student %s is not assigned to %s.";

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Guardian> guardians = FXCollections.observableArrayList();
    private final Map<StudentId, GuardianId> guardianLinks = new LinkedHashMap<>();
    private final Set<GroupAssignment> groupAssignments = new LinkedHashSet<>();

    public AddressBook() { }

    /**
     * Copies the complete register, sharing immutable values but not mutable collections.
     *
     * @param toBeCopied The register to validate and copy.
     * @throws IllegalArgumentException If the register violates student, guardian or assignment invariants.
     * @throws NullPointerException If the register, a collection or an entry is null.
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        resetData(toBeCopied);
    }

    /**
     * Replaces the complete register after validating all students, guardians and assignments.
     * Validation failure preserves the current state. Passing this register itself is supported.
     *
     * @param newData The replacement state, whose mutable collections are copied.
     * @throws IllegalArgumentException If the replacement violates student, guardian or assignment invariants.
     * @throws NullPointerException If the register, a collection or an entry is null.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        AddressBook replacement = new AddressBook();
        List.copyOf(newData.getStudentList()).forEach(replacement::addStudent);
        List.copyOf(newData.getGuardianList()).forEach(replacement::addGuardian);
        newData.getGroupAssignments().forEach(replacement::addGroupAssignment);
        newData.getGuardianLinks().forEach((studentId, guardianId) ->
                replacement.linkGuardian(studentId.toString(), guardianId.toString()));
        guardianLinks.clear();
        guardianLinks.putAll(replacement.guardianLinks);
        groupAssignments.clear();
        groupAssignments.addAll(replacement.groupAssignments);
        if (!guardians.equals(replacement.guardians)) {
            guardians.setAll(replacement.guardians);
        }
        if (!students.equals(replacement.students)) {
            students.setAll(replacement.students);
        }
    }

    /**
     * Replaces students after validation, retaining assignments only for IDs still present.
     * Replacing a Student object with the same ID preserves its assignments.
     *
     * @param newStudents The students to copy into the register.
     * @throws IllegalArgumentException If student IDs or normalized name/level identities repeat.
     * @throws NullPointerException If the list or a student is null.
     */
    public void setStudents(List<Student> newStudents) {
        AddressBook replacement = new AddressBook();
        List.copyOf(newStudents).forEach(replacement::addStudent);
        guardianLinks.keySet().removeIf(studentId -> !replacement.hasStudentId(studentId));
        groupAssignments.removeIf(assignment -> !replacement.hasStudentId(assignment.getStudentId()));
        students.setAll(replacement.students);
    }

    /**
     * Replaces guardians after validation, preserving students and their assignments.
     *
     * @param newGuardians Guardian records to copy; a live view of this register is supported.
     * @throws IllegalArgumentException If guardian IDs or canonical phone numbers repeat.
     * @throws NullPointerException If the list or an entry is null.
     */
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

    /**
     * Removes only the student's guardian relationship, retaining both records and other links.
     *
     * @param studentId The existing student's ID text, independent of the displayed filter.
     * @return The removed guardian ID, without allocating a new ID.
     * @throws IllegalArgumentException If the student is unknown or has no guardian.
     * @throws NullPointerException If studentId is null.
     */
    public GuardianId unlinkGuardian(String studentId) {
        Student student = findStudent(studentId).orElseThrow(() ->
                new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_STUDENT, studentId)));
        GuardianId guardianId = guardianLinks.get(student.getId());
        if (guardianId == null) {
            throw new IllegalArgumentException(String.format(MESSAGE_NO_GUARDIAN, studentId));
        }
        guardianLinks.remove(student.getId());
        return guardianId;
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

    /**
     * Looks up exact stable ID text without constructing an ID or advancing its generator.
     *
     * @param studentId The ID text to look up.
     * @return The matching student, or an empty optional if no registered student has that ID.
     * @throws NullPointerException If {@code studentId} is null.
     */
    public Optional<Student> findStudent(String studentId) {
        requireNonNull(studentId);
        return students.stream().filter(student -> student.getId().value.equals(studentId)).findFirst();
    }

    /**
     * Looks up an existing ID in the complete register.
     *
     * @param id The previously constructed student ID.
     * @return The matching student, or an empty optional if absent.
     * @throws NullPointerException If {@code id} is null.
     */
    public Optional<Student> findStudent(StudentId id) {
        return findStudent(requireNonNull(id).value);
    }

    /**
     * Adds an assignment after checking both references and pair uniqueness.
     * Validation failure leaves the register unchanged.
     *
     * @param assignment The immutable student/group pair to add.
     * @throws IllegalArgumentException If either reference is unknown or the pair already exists.
     * @throws NullPointerException If the assignment is null.
     */
    public void addGroupAssignment(GroupAssignment assignment) {
        requireNonNull(assignment);
        if (!hasStudentId(assignment.getStudentId())) {
            throw new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_STUDENT, assignment.getStudentId()));
        }
        if (ConfiguredGroups.getCatalogue().findByCode(assignment.getGroupCode().getValue()).isEmpty()) {
            throw new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_GROUP, assignment.getGroupCode()));
        }
        if (groupAssignments.contains(assignment)) {
            throw new IllegalArgumentException(String.format(MESSAGE_DUPLICATE_ASSIGNMENT,
                    assignment.getStudentId(), assignment.getGroupCode()));
        }
        groupAssignments.add(assignment);
    }

    /**
     * Removes one existing assignment from this in-memory register, including a student's last assignment.
     * Validates the student, configured group and membership before mutation; rejection leaves the register unchanged.
     * Preserves all student and guardian records, guardian links, other assignments and the configured catalogue.
     * This operation does not save data to disk.
     *
     * @param assignment The immutable student/group pair to remove.
     * @throws IllegalArgumentException If the student or configured group is unknown, or the pair is not assigned.
     * @throws NullPointerException If {@code assignment} is null.
     */
    public void removeGroupAssignment(GroupAssignment assignment) {
        requireNonNull(assignment);
        if (!hasStudentId(assignment.getStudentId())) {
            throw new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_STUDENT, assignment.getStudentId()));
        }
        if (ConfiguredGroups.getCatalogue().findByCode(assignment.getGroupCode().getValue()).isEmpty()) {
            throw new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_GROUP, assignment.getGroupCode()));
        }
        if (!groupAssignments.contains(assignment)) {
            throw new IllegalArgumentException(String.format(MESSAGE_MISSING_ASSIGNMENT,
                    assignment.getStudentId(), assignment.getGroupCode()));
        }
        groupAssignments.remove(assignment);
    }

    /**
     * Replaces a student by stable ID after validating identity against the complete register.
     * All assignments, guardians and other students are retained; failed validation changes nothing.
     *
     * @param student The replacement record with an existing ID.
     * @throws IllegalArgumentException If the ID is absent or another student has the same normalized name/level.
     * @throws NullPointerException If {@code student} is null.
     */
    public void updateStudent(Student student) {
        requireNonNull(student);
        int index = -1;
        for (int i = 0; i < students.size(); i++) {
            Student existing = students.get(i);
            if (existing.getId().equals(student.getId())) {
                index = i;
            } else if (existing.sameIdentity(student)) {
                throw new IllegalArgumentException("Duplicate student identity.");
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Student ID does not exist.");
        }
        students.set(index, student);
        sortStudents();
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

    /**
     * Removes a student and only that student's assignments, preserving the configured catalogue.
     * Does nothing if the ID is not registered.
     *
     * @param id The stable ID of the student to remove.
     * @throws NullPointerException If {@code id} is null.
     */
    public void deleteStudent(StudentId id) {
        requireNonNull(id);
        guardianLinks.remove(id);
        groupAssignments.removeIf(assignment -> assignment.getStudentId().equals(id));
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
    public Map<StudentId, GuardianId> getGuardianLinks() {
        return Collections.unmodifiableMap(guardianLinks);
    }

    @Override
    public Set<GroupAssignment> getGroupAssignments() {
        return Collections.unmodifiableSet(groupAssignments);
    }

    @Override
    public ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(guardians);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("students", students).add("guardians", guardians)
                .add("guardianLinks", guardianLinks).add("groupAssignments", groupAssignments).toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddressBook addressBook && students.equals(addressBook.students)
                && guardians.equals(addressBook.guardians) && guardianLinks.equals(addressBook.guardianLinks)
                && groupAssignments.equals(addressBook.groupAssignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(students, guardians, guardianLinks, groupAssignments);
    }
}
