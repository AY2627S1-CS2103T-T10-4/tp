package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class AddressBookTest {
    @Test
    void studentOperations_keepSortedStudentOnlyRecords() {
        AddressBook register = new AddressBook();
        Student mei = new Student(new StudentId("S-0102"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        Student amy = new Student(new StudentId("S-0101"), new StudentName("Amy Tan"), new AcademicLevel("IP 1"));

        register.addStudent(mei);
        register.addStudent(amy);

        assertEquals(List.of(amy, mei), register.getStudentList());
        assertTrue(register.hasStudentId(mei.getId()));
        assertFalse(register.hasStudentId(new StudentId("S-0103")));
        assertThrows(UnsupportedOperationException.class, () -> register.getStudentList().clear());
    }

    @Test
    void retiredPersonOperations_cannotStoreRecords() {
        AddressBook register = new AddressBook();
        assertFalse(register.hasPerson(null));
        assertThrows(UnsupportedOperationException.class, () -> register.addPerson((Person) null));
        assertTrue(register.getPersonList().isEmpty());
    }
}
