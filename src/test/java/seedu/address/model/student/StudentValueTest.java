package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

class StudentValueTest {
    @Test
    void studentName_normalizesAndValidates() {
        StudentName name = new StudentName("  Mei   Lin  ");
        assertEquals("Mei Lin", name.toString());
        assertEquals(name, new StudentName("mei lin"));
        assertEquals(0, name.compareTo(new StudentName("MEI LIN")));
        assertTrue(StudentName.isValid("O'Connor"));
        assertTrue(StudentName.isValid("S. Kumar"));
        assertTrue(StudentName.isValid("李明"));
        assertFalse(StudentName.isValid(""));
        assertFalse(StudentName.isValid("2Mei"));
        assertFalse(StudentName.isValid("Mei!"));
        assertFalse(StudentName.isValid("A".repeat(81)));
        assertThrows(IllegalArgumentException.class, () -> new StudentName("-Mei"));
    }

    @Test
    void academicLevel_canonicalizesAndRejectsUnsupportedValues() {
        assertEquals("Sec 2", new AcademicLevel(" sec   2 ").toString());
        assertEquals("IP 6", new AcademicLevel("ip 6").toString());
        assertThrows(IllegalArgumentException.class, () -> new AcademicLevel("Sec 6"));
        assertThrows(IllegalArgumentException.class, () -> new AcademicLevel("Secondary 2"));
    }

    @Test
    void studentId_validatesComparesAndGeneratesPastExistingId() {
        StudentId laterId = new StudentId("S-01234");
        StudentId earlierId = new StudentId("S-0007");
        assertTrue(earlierId.compareTo(laterId) < 0);
        assertEquals(earlierId, new StudentId("S-0007"));
        assertNotEquals(earlierId, laterId);
        assertThrows(IllegalArgumentException.class, () -> new StudentId("G-0001"));
        assertThrows(IllegalArgumentException.class, () -> new StudentId("S-123"));
        assertTrue(StudentId.generate().compareTo(laterId) > 0);
    }

    @Test
    void student_identityEqualityAndHashCode() {
        Student first = new Student(new StudentId("S-9001"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        Student sameIdentity = new Student(new StudentId("S-9002"), new StudentName("mei   lin"),
                new AcademicLevel("sec 2"));
        Student differentLevel = new Student(new StudentId("S-9003"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 3"));
        assertTrue(first.sameIdentity(sameIdentity));
        assertFalse(first.sameIdentity(differentLevel));
        assertEquals(first, new Student(new StudentId("S-9001"), new StudentName("MEI LIN"),
                new AcademicLevel("sec 2")));
        assertEquals(first.hashCode(), new Student(new StudentId("S-9001"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")).hashCode());
        assertNotEquals(first, sameIdentity);
        assertEquals("S-9001: Mei Lin (Sec 2)", first.toString());
        assertThrows(NullPointerException.class, () -> new Student(null, new StudentName("Mei"),
                new AcademicLevel("Sec 1")));
    }
}
