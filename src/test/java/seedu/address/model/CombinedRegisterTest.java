package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class CombinedRegisterTest {
    @Test
    void copyResetAndHash_includeAllRecordsWithoutAliasing() {
        AddressBook original = register();
        AddressBook copy = new AddressBook(original);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        original.resetData(original);
        assertEquals(copy, original);
        copy.addGuardian(guardian("G-9102", "Raj Kumar", "61234567"));
        assertNotEquals(original, copy);
        assertEquals(1, original.getGuardianList().size());
        original.resetData(copy);
        assertEquals(copy, original);

        Student amy = copy.findStudent("S-9102").orElseThrow();
        copy.addGroupAssignment(new GroupAssignment(amy.getId(), new GroupCode("SCI")));
        assertNotEquals(original, copy);
        assertEquals(1, original.getGroupAssignments().size());
        assertThrows(UnsupportedOperationException.class, () -> original.getGuardianList().clear());
        assertThrows(UnsupportedOperationException.class, () -> original.getGroupAssignments().clear());
    }

    @Test
    void resetData_invalidGuardianOrAssignment_preservesCompleteState() {
        AddressBook original = register();
        AddressBook before = new AddressBook(original);
        Guardian first = original.getGuardianList().getFirst();
        AddressBook invalidGuardians = new AddressBook() {
            @Override
            public ObservableList<Guardian> getGuardianList() {
                return FXCollections.observableArrayList(first, first);
            }
        };
        assertThrows(IllegalArgumentException.class, () -> original.resetData(invalidGuardians));
        assertEquals(before, original);

        AddressBook invalidAssignments = new AddressBook(original) {
            @Override
            public Set<GroupAssignment> getGroupAssignments() {
                return Set.of(new GroupAssignment(new StudentId("S-9199"), new GroupCode("MAT")));
            }
        };
        assertThrows(IllegalArgumentException.class, () -> original.resetData(invalidAssignments));
        assertEquals(before, original);
    }

    @Test
    void setGuardians_validatesBeforeReplacementAndCopiesInput() {
        AddressBook register = register();
        AddressBook before = new AddressBook(register);
        Guardian first = register.getGuardianList().getFirst();
        assertThrows(IllegalArgumentException.class, () -> register.setGuardians(List.of(first, first)));
        assertEquals(before, register);

        register.setGuardians(register.getGuardianList());
        assertEquals(before, register);
        List<Guardian> input = new ArrayList<>(register.getGuardianList());
        register.setGuardians(input);
        input.clear();
        assertEquals(before, register);
    }

    @Test
    void studentReplacementAndDeletion_preserveGuardiansAndOtherAssignments() {
        AddressBook register = register();
        Student mei = register.findStudent("S-9101").orElseThrow();
        Student amy = register.findStudent("S-9102").orElseThrow();
        List<Guardian> guardians = List.copyOf(register.getGuardianList());
        GroupAssignment amyGroup = new GroupAssignment(amy.getId(), new GroupCode("SCI"));
        register.addGroupAssignment(amyGroup);

        register.setStudents(List.of(new Student(mei.getId(), mei.getName(), new AcademicLevel("Sec 4")), amy));
        assertEquals(2, register.getGroupAssignments().size());
        register.deleteStudent(mei.getId());
        assertEquals(List.of(amy), register.getStudentList());
        assertEquals(Set.of(amyGroup), register.getGroupAssignments());
        assertEquals(guardians, register.getGuardianList());
    }

    @Test
    void updateStudent_validatesFullRegisterAndPreservesRelationships() {
        AddressBook register = register();
        Student mei = register.findStudent("S-9101").orElseThrow();
        register.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("S2-MAT-A")));
        AddressBook before = new AddressBook(register);
        Student edited = new Student(mei.getId(), mei.getName(), new AcademicLevel("Sec 4"));

        register.updateStudent(edited);

        assertEquals(edited, register.findStudent(mei.getId()).orElseThrow());
        assertEquals(before.getGuardianList(), register.getGuardianList());
        assertEquals(before.getGroupAssignments(), register.getGroupAssignments());
        assertEquals(before.findStudent("S-9102"), register.findStudent("S-9102"));
        assertNotEquals(before, register);
        AddressBook copy = new AddressBook(register);
        register.resetData(register);
        assertEquals(copy, register);
        assertEquals(copy.hashCode(), register.hashCode());

        Student amy = register.findStudent("S-9102").orElseThrow();
        Student duplicate = new Student(amy.getId(), edited.getName(), edited.getAcademicLevel());
        assertThrows(IllegalArgumentException.class, () -> register.updateStudent(duplicate));
        assertThrows(IllegalArgumentException.class, () -> register.updateStudent(new Student(
                new StudentId("S-9199"), new StudentName("Unknown Student"), new AcademicLevel("Sec 1"))));
        assertEquals(copy, register);
        register.resetData(before);
        assertEquals(before, register);
    }

    private static AddressBook register() {
        AddressBook register = new AddressBook();
        Student mei = new Student(new StudentId("S-9101"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        register.addStudent(mei);
        register.addStudent(new Student(new StudentId("S-9102"), new StudentName("Amy Tan"),
                new AcademicLevel("Sec 3")));
        register.addGuardian(guardian("G-9101", "Anita Lim", "91234567"));
        register.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
        return register;
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }
}
