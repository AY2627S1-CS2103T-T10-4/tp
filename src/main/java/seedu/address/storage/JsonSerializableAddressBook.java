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
import seedu.address.model.student.Student;

/** Student-only JSON representation of the local register. */
@JsonRootName(value = "addressbook")
@JsonIgnoreProperties("persons")
class JsonSerializableAddressBook {
    /** Retained for source compatibility with tests for the retired Person serializer. */
    @Deprecated
    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedStudent> students = new ArrayList<>();

    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> ignoredPersons,
            @JsonProperty("students") List<JsonAdaptedStudent> students) {
        if (students != null) {
            this.students.addAll(students);
        }
    }

    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        students.addAll(source.getStudentList().stream().map(JsonAdaptedStudent::new).toList());
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
        return register;
    }
}
