package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.student.Student;

class UnlinkGuardianModelTest {
    @Test
    void unlinkGuardian_sharedGuardian_preservesRecordsAssignmentsAndSiblingLink() {
        AddressBook register = getUnlinkedAddressBook();
        Student student = register.findStudent("S-0007").orElseThrow();
        register.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        AddressBook before = new AddressBook(register);

        GuardianId removed = register.unlinkGuardian("S-0007");

        assertEquals("G-0003", removed.toString());
        assertTrue(register.getLinkedGuardian("S-0007").isEmpty());
        assertTrue(register.getLinkedGuardian("S-0008").isPresent());
        assertEquals(before.getStudentList(), register.getStudentList());
        assertEquals(before.getGuardianList(), register.getGuardianList());
        assertEquals(before.getGroupAssignments(), register.getGroupAssignments());
        register.linkGuardian("S-0007", "G-0004");
        assertEquals("G-0004", register.getLinkedGuardian("S-0007").orElseThrow().getId().toString());
        register.resetData(before);
        assertEquals(before, register);
    }

    @Test
    void unlinkGuardian_unknownUnlinkedOrNullStudent_preservesState() {
        AddressBook register = getUnlinkedAddressBook();
        AddressBook before = new AddressBook(register);
        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class, () ->
                register.unlinkGuardian("S-9999"));
        IllegalArgumentException unlinked = assertThrows(IllegalArgumentException.class, () ->
                register.unlinkGuardian("S-0007"));

        assertEquals("Student S-9999 does not exist.", unknown.getMessage());
        assertEquals("Student S-0007 has no guardian to unlink.", unlinked.getMessage());
        assertThrows(NullPointerException.class, () -> register.unlinkGuardian(null));
        assertEquals(before, register);
    }

    @Test
    void unlinkGuardian_hiddenStudent_updatesGuardianProjectionWithoutChangingFilter() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.linkGuardian("S-0007", "G-0003");
        model.linkGuardian("S-0008", "G-0003");
        model.updateFilteredStudentList(unused -> false);

        model.unlinkGuardian("S-0007");

        assertTrue(model.getStudentList().isEmpty());
        assertEquals(List.of(model.findStudent("S-0008").orElseThrow()), model.getLinkedStudents("G-0003"));
        model.unlinkGuardian("S-0008");
        assertTrue(model.getLinkedStudents("G-0003").isEmpty());
        assertEquals(2, model.getGuardianList().size());
    }
}
