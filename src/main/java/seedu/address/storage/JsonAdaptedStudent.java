package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    JsonAdaptedStudent(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("academicLevel") String academicLevel) {
        this.id = id; this.name = name; this.academicLevel = academicLevel;
    }

    JsonAdaptedStudent(Student student) {
        id = student.getId().value; name = student.getName().toString();
        academicLevel = student.getAcademicLevel().toString();
    }

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
