package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

/** Jackson representation of a student. */
class JsonAdaptedStudent {
    private final String id;
    private final String name;
    private final String academicLevel;

    @JsonCreator
    JsonAdaptedStudent(
            @JsonProperty("id") @JsonDeserialize(using = StoredStringDeserializer.class) String id,
            @JsonProperty("name") @JsonDeserialize(using = StoredStringDeserializer.class) String name,
            @JsonProperty("academicLevel") @JsonDeserialize(using = StoredStringDeserializer.class)
            String academicLevel) {
        this.id = id;
        this.name = name;
        this.academicLevel = academicLevel;
    }

    JsonAdaptedStudent(Student student) {
        id = student.getId().value;
        name = student.getName().toString();
        academicLevel = student.getAcademicLevel().toString();
    }

    /** Prevents data from an unsupported record format from being discarded during a later save. */
    @JsonAnySetter
    private void rejectUnknownProperty(String property, Object ignoredValue) {
        throw new IllegalArgumentException("Unsupported student field: " + property + ".");
    }

    /**
     * Converts all required fields to a Student using the existing stored-ID loading policy.
     *
     * @return The validated student.
     * @throws IllegalValueException If a required field is absent or violates a domain constraint.
     */
    Student toModelType() throws IllegalValueException {
        try {
            if (id == null || name == null || academicLevel == null) {
                throw new IllegalArgumentException("Student record is missing a required field.");
            }
            return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(academicLevel));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
