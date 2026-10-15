package seedu.address.testutil;

import seedu.address.model.AddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

/** Creates independent registers for relationship tests. */
public final class GuardianLinkTestUtil {
    private GuardianLinkTestUtil() { }

    /** Returns two students and two guardians, initially without any relationships. */
    public static AddressBook getUnlinkedAddressBook() {
        AddressBook register = new AddressBook();
        register.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        register.addStudent(new Student(new StudentId("S-0008"), new StudentName("Mei Ling"),
                new AcademicLevel("Sec 3")));
        register.addGuardian(new Guardian(new GuardianId("G-0003"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567")));
        register.addGuardian(new Guardian(new GuardianId("G-0004"), new GuardianName("Raj Kumar"),
                new GuardianPhone("61234567")));
        return register;
    }
}
