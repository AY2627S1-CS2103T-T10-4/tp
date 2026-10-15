package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** Read-only view of Mentora's student and guardian records. */
public interface ReadOnlyAddressBook {
    /** Returns an unmodifiable view of all students. */
    ObservableList<Student> getStudentList();

    /** Returns an unmodifiable view of all guardians. */
    default ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Returns the student-to-guardian relationships, with at most one guardian per student. */
    default Map<StudentId, GuardianId> getGuardianLinks() {
        return Map.of();
    }

    /** Finds a registered student without allocating an ID or advancing its generator. */
    default Optional<Student> findStudent(String studentId) {
        requireNonNull(studentId);
        return getStudentList().stream().filter(student -> student.getId().value.equals(studentId)).findFirst();
    }

    /** Finds a registered guardian without allocating an ID or advancing its generator. */
    default Optional<Guardian> findGuardian(String guardianId) {
        requireNonNull(guardianId);
        return getGuardianList().stream()
                .filter(guardian -> guardian.getId().toString().equals(guardianId)).findFirst();
    }

    /** Resolves a student's link against the current guardian records, avoiding stale contact details. */
    default Optional<Guardian> getLinkedGuardian(String studentId) {
        return findStudent(studentId)
                .map(student -> getGuardianLinks().get(student.getId()))
                .flatMap(guardianId -> findGuardian(guardianId.toString()));
    }

    /** Empty compatibility view retained for legacy callers; persons are no longer stored. */
    @Deprecated
    default ObservableList<Person> getPersonList() {
        return FXCollections.observableArrayList();
    }
}
