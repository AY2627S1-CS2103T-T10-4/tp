package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class JsonSerializableAddressBookStudentTest {
    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }

    @Test
    void toModelType_studentsRoundTripAndSort() throws Exception {
        AddressBook source = new AddressBook();
        source.addStudent(student("S-0151", "Mei Lin"));
        source.addStudent(student("S-0152", "Amy Tan"));

        AddressBook restored = new JsonSerializableAddressBook(source).toModelType();
        assertEquals(source.getStudentList(), restored.getStudentList());
    }

    @Test
    void toModelType_duplicateStudentIdOrIdentity_throwsIllegalValueException() {
        JsonAdaptedStudent first = new JsonAdaptedStudent("S-0161", "Mei Lin", "Sec 2");
        JsonAdaptedStudent duplicateId = new JsonAdaptedStudent("S-0161", "Amy Tan", "Sec 3");
        JsonAdaptedStudent duplicateIdentity = new JsonAdaptedStudent("S-0162", "mei lin", "sec 2");
        Executable sameId = () -> new JsonSerializableAddressBook(List.of(), List.of(first, duplicateId)).toModelType();
        Executable sameIdentity = () -> new JsonSerializableAddressBook(List.of(), List.of(first, duplicateIdentity))
                .toModelType();
        assertThrows(IllegalValueException.class, "Students list contains duplicate student ID or identity.", sameId);
        assertThrows(IllegalValueException.class, "Students list contains duplicate student ID or identity.",
                sameIdentity);
    }

    @Test
    void constructor_nullLists_returnsEmptyAddressBook() throws Exception {
        assertEquals(new AddressBook(), new JsonSerializableAddressBook(null, null).toModelType());
    }
}
