package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class GuardianWorkspaceModelTest {
    @Test
    void guardianView_filtersIndependentlyAndLinkedStudentsUseDisplayOrder() {
        ModelManager model = new ModelManager();
        Guardian anita = guardian("G-0201", "Anita Lim", "91234567");
        Guardian raj = guardian("G-0202", "Raj Kumar", "61234567");
        Student mei = student("S-0202", "Mei Lin");
        Student amy = student("S-0201", "Amy Tan");
        model.addGuardian(raj);
        model.addGuardian(anita);
        model.addStudent(mei);
        model.addStudent(amy);
        model.linkGuardian(mei.getId().value, anita.getId().toString());
        model.linkGuardian(amy.getId().value, anita.getId().toString());

        model.updateFilteredGuardianList(guardian -> guardian.equals(raj));

        assertEquals(List.of(raj), model.getGuardianList());
        assertEquals(List.of(amy, mei), model.getLinkedStudents(anita.getId().toString()));
        assertEquals(List.of(), model.getLinkedStudents("G-9999"));
        model.updateFilteredGuardianList(Model.PREDICATE_SHOW_ALL_GUARDIANS);
        assertEquals(List.of(anita, raj), model.getGuardianList());
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
