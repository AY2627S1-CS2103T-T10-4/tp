package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class JsonAdaptedStudentTest {
    private static final String ID = "S-0088";
    private static final String NAME = "Mei Lin";
    private static final String LEVEL = "Sec 2";
    private static final Student EXPECTED = new Student(new StudentId(ID), new StudentName(NAME),
            new AcademicLevel(LEVEL));

    @Test
    void toModelType_validStudent_returnsStudent() throws Exception {
        assertEquals(EXPECTED, new JsonAdaptedStudent(EXPECTED).toModelType());
        assertEquals(EXPECTED, new JsonAdaptedStudent(ID, NAME, LEVEL).toModelType());
    }

    @Test
    void toModelType_missingFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, "Student record is missing a required field.",
                () -> new JsonAdaptedStudent(null, NAME, LEVEL).toModelType());
        assertThrows(IllegalValueException.class, "Student record is missing a required field.",
                () -> new JsonAdaptedStudent(ID, null, LEVEL).toModelType());
        assertThrows(IllegalValueException.class, "Student record is missing a required field.",
                () -> new JsonAdaptedStudent(ID, NAME, null).toModelType());
    }

    @Test
    void toModelType_invalidValues_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedStudent("G-0001", NAME, LEVEL).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedStudent(ID, "Mei2", LEVEL).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedStudent(ID, NAME, "Sec 6").toModelType());
    }
}
