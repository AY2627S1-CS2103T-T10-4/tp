package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;
import seedu.address.model.tag.Tag;

/** Creates sample student records for a new local register. */
public final class SampleDataUtil {
    private SampleDataUtil() { }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook register = new AddressBook();
        register.addStudent(new Student(new StudentName("Mei Lin"), new AcademicLevel("Sec 2")));
        register.addStudent(new Student(new StudentName("S. Kumar"), new AcademicLevel("IP 4")));
        return register;
    }

    /** Legacy tag helper retained for Person-focused source tests. */
    @Deprecated
    public static Set<Tag> getTagSet(String... tags) {
        return Arrays.stream(tags).map(Tag::new).collect(Collectors.toSet());
    }
}
