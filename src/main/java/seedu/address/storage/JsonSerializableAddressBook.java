package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSetter;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;

/** JSON representation of students, guardians and student/group assignments. */
@JsonRootName(value = "addressbook")
@JsonIgnoreProperties({"persons", "_comment"})
class JsonSerializableAddressBook {
    /** Retained for source compatibility with tests for the retired Person serializer. */
    @Deprecated
    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    static final String MESSAGE_NULL_GUARDIAN = "Guardians list contains null guardian(s).";

    private final List<JsonAdaptedStudent> students = new ArrayList<>();
    private final List<JsonAdaptedGuardian> guardians = new ArrayList<>();
    private final List<JsonAdaptedGroupAssignment> groupAssignments = new ArrayList<>();

    @JsonCreator
    private JsonSerializableAddressBook() { }

    /**
     * Retains the old constructor for source compatibility; retired Person records are intentionally ignored.
     *
     * @param ignoredPersons Legacy records which cannot be converted to Students.
     * @param students Student records, or null for an absent legacy collection.
     * @param guardians Guardian records, or null for an absent legacy collection.
     */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> ignoredPersons, List<JsonAdaptedStudent> students,
            List<JsonAdaptedGuardian> guardians) {
        if (students != null) {
            this.students.addAll(students);
        }
        if (guardians != null) {
            this.guardians.addAll(guardians);
        }
    }

    /** Retained for source compatibility with student-only storage tests and callers. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> ignoredPersons, List<JsonAdaptedStudent> students) {
        this(ignoredPersons, students, null);
    }

    /**
     * Copies the complete register into JSON values without retaining its mutable collections.
     *
     * @param source Validated register to serialize.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        students.addAll(source.getStudentList().stream().map(JsonAdaptedStudent::new).toList());
        guardians.addAll(source.getGuardianList().stream().map(JsonAdaptedGuardian::new).toList());
        groupAssignments.addAll(source.getGroupAssignments().stream().map(JsonAdaptedGroupAssignment::new).toList());
    }

    /** Accepts an absent guardian property but rejects a malformed present collection. */
    @JsonSetter("guardians")
    private void setGuardians(List<JsonAdaptedGuardian> records) {
        if (records == null) {
            throw new IllegalArgumentException("Guardians must be a JSON array, not null.");
        }
        guardians.clear();
        guardians.addAll(records);
    }

    /** Accepts an absent legacy property but rejects a malformed present assignment array. */
    @JsonSetter("groupAssignments")
    private void setGroupAssignments(List<JsonAdaptedGroupAssignment> records) {
        if (records == null) {
            throw new IllegalArgumentException("Group assignments must be a JSON array, not null.");
        }
        groupAssignments.clear();
        groupAssignments.addAll(records);
    }

    /** Distinguishes an absent legacy list from an explicitly invalid null list in JSON. */
    @JsonSetter("students")
    private void setStudents(List<JsonAdaptedStudent> records) {
        if (records == null) {
            throw new IllegalArgumentException("Students must be a JSON array, not null.");
        }
        students.clear();
        students.addAll(records);
    }

    /** Rejects unsupported stored data rather than silently discarding it on the next save. */
    @JsonAnySetter
    private void rejectUnknownProperty(String property, Object ignoredValue) {
        throw new IllegalArgumentException("Unsupported local data field: " + property + ".");
    }

    /**
     * Validates students and guardians, then all assignment references, before returning a complete register.
     * No partially converted register is published when validation fails.
     *
     * @return A new validated register containing every stored student, guardian and assignment.
     * @throws IllegalValueException If a record, reference or uniqueness constraint is invalid.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook register = new AddressBook();
        for (JsonAdaptedStudent adaptedStudent : students) {
            if (adaptedStudent == null) {
                throw new IllegalValueException("Students list contains a null record.");
            }
            Student student = adaptedStudent.toModelType();
            if (register.hasStudent(student)) {
                throw new IllegalValueException("Students list contains duplicate student ID or identity.");
            }
            register.addStudent(student);
        }
        for (JsonAdaptedGuardian adaptedGuardian : guardians) {
            if (adaptedGuardian == null) {
                throw new IllegalValueException(MESSAGE_NULL_GUARDIAN);
            }
            Guardian guardian = adaptedGuardian.toModelType();
            if (register.hasGuardian(guardian)) {
                throw new IllegalValueException("Guardians list contains duplicate guardian ID or phone number.");
            }
            register.addGuardian(guardian);
        }
        for (JsonAdaptedGroupAssignment assignment : groupAssignments) {
            if (assignment == null) {
                throw new IllegalValueException("Group assignments contain a null record.");
            }
            try {
                register.addGroupAssignment(assignment.toModelType(register));
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage());
            }
        }
        return register;
    }
}
