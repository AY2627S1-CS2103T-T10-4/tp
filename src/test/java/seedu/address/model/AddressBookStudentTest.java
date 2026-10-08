package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class AddressBookStudentTest {
    @Test
    void addStudent_sortsAndChecksIdAndIdentity() {
        AddressBook addressBook = new AddressBook();
        Student mei = student("S-0101", "Mei Lin", "Sec 2");
        Student amy = student("S-0102", "Amy Tan", "IP 1");
        addressBook.addStudent(mei);
        addressBook.addStudent(amy);

        assertEquals(List.of(amy, mei), addressBook.getStudentList());
        assertTrue(addressBook.hasStudent(student("S-0103", "mei   lin", "sec 2")));
        assertTrue(addressBook.hasStudent(student("S-0101", "Different Name", "Sec 5")));
        assertTrue(addressBook.hasStudentId(new StudentId("S-0102")));
        assertFalse(addressBook.hasStudentId(new StudentId("S-0104")));
        assertThrows(IllegalArgumentException.class, () -> addressBook.addStudent(student("S-0101", "New Person",
                "Sec 1")));
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getStudentList().clear());
    }

    @Test
    void resetData_copiesStudentRecords() {
        AddressBook source = new AddressBook();
        source.addStudent(student("S-0110", "Anita Lim", "Sec 3"));
        AddressBook copy = new AddressBook(source);
        assertEquals(source.getStudentList(), copy.getStudentList());
        assertEquals(source, copy);
    }

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
