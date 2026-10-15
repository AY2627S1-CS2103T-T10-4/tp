package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;

/** JSON representation of the local student and guardian register. */
@JsonRootName(value = "addressbook")
@JsonIgnoreProperties("persons")
class JsonSerializableAddressBook {
    /** Retained for source compatibility with tests for the retired Person serializer. */
    @Deprecated
    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    static final String MESSAGE_NULL_GUARDIAN = "Guardians list contains null guardian(s).";

    private final List<JsonAdaptedStudent> students = new ArrayList<>();
    private final List<JsonAdaptedGuardian> guardians = new ArrayList<>();

    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> ignoredPersons,
            @JsonProperty("students") List<JsonAdaptedStudent> students,
            @JsonProperty("guardians") List<JsonAdaptedGuardian> guardians) {
        if (students != null) {
            this.students.addAll(students);
        }
        if (guardians != null) {
            this.guardians.addAll(guardians);
        }
    }

    /** Retained for source compatibility with student-only storage tests and callers. */
    JsonSerializableAddressBook(List<JsonAdaptedPerson> ignoredPersons, List<JsonAdaptedStudent> students) {
        this(ignoredPersons, students, null);
    }

    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        students.addAll(source.getStudentList().stream().map(JsonAdaptedStudent::new).toList());
        guardians.addAll(source.getGuardianList().stream().map(JsonAdaptedGuardian::new).toList());
    }

    public AddressBook toModelType() throws IllegalValueException {
        AddressBook register = new AddressBook();
        for (JsonAdaptedStudent adaptedStudent : students) {
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
        return register;
    }
}
