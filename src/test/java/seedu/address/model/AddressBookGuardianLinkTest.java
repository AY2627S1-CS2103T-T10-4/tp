package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

class AddressBookGuardianLinkTest {
    @Test
    void linkGuardian_sharedGuardian_createsIndependentLinks() {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");

        assertEquals(2, register.getGuardianLinks().size());
        assertEquals(register.getLinkedGuardian("S-0007"), register.getLinkedGuardian("S-0008"));
        assertThrows(UnsupportedOperationException.class, () -> register.getGuardianLinks().clear());
        assertTrue(register.getLinkedGuardian("S-9999").isEmpty());
    }

    @Test
    void linkGuardian_duplicateOrReplacement_preservesOriginalLink() {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        AddressBook before = new AddressBook(register);

        IllegalArgumentException duplicate = assertThrows(IllegalArgumentException.class, () ->
                register.linkGuardian("S-0007", "G-0003"));
        IllegalArgumentException replacement = assertThrows(IllegalArgumentException.class, () ->
                register.linkGuardian("S-0007", "G-0004"));
        assertEquals("Student S-0007 is already linked to guardian G-0003.", duplicate.getMessage());
        assertEquals("Student S-0007 already has guardian G-0003. Unlink it before linking another guardian.",
                replacement.getMessage());
        assertEquals(before, register);
    }

    @Test
    void linkGuardian_missingRecordsOrNullInput_doesNotCreateLink() {
        AddressBook register = getUnlinkedAddressBook();
        IllegalArgumentException missingStudent = assertThrows(IllegalArgumentException.class, () ->
                register.linkGuardian("S-9999", "G-0003"));
        IllegalArgumentException missingGuardian = assertThrows(IllegalArgumentException.class, () ->
                register.linkGuardian("S-0007", "G-9999"));
        assertEquals("Student S-9999 does not exist.", missingStudent.getMessage());
        assertEquals("Guardian G-9999 does not exist.", missingGuardian.getMessage());
        assertThrows(NullPointerException.class, () -> register.linkGuardian(null, "G-0003"));
        assertThrows(NullPointerException.class, () -> register.linkGuardian("S-0007", null));
        assertTrue(register.getGuardianLinks().isEmpty());
    }

    @Test
    void resetData_copyAndRollback_includesLinksWithoutRefreshingUnchangedStudents() {
        AddressBook register = getUnlinkedAddressBook();
        AddressBook before = new AddressBook(register);
        AtomicInteger studentChanges = new AtomicInteger();
        register.getStudentList().addListener((ListChangeListener<Student>) change ->
                studentChanges.incrementAndGet());
        register.linkGuardian("S-0007", "G-0003");
        assertNotEquals(before, register);
        AddressBook linkedCopy = new AddressBook(register);
        register.resetData(register);
        assertEquals(linkedCopy, register);
        assertEquals(linkedCopy.hashCode(), register.hashCode());

        register.resetData(before);

        assertEquals(before, register);
        assertEquals(1, linkedCopy.getGuardianLinks().size());
        assertEquals(0, studentChanges.get());
    }

    @Test
    void resetData_danglingLink_rejectsReplacementWithoutPartialChanges() {
        AddressBook register = getUnlinkedAddressBook();
        AddressBook before = new AddressBook(register);
        ReadOnlyAddressBook invalid = new ReadOnlyAddressBook() {
            @Override
            public ObservableList<Student> getStudentList() {
                return before.getStudentList();
            }

            @Override
            public Map<StudentId, GuardianId> getGuardianLinks() {
                return Map.of(before.findStudent("S-0007").orElseThrow().getId(), new GuardianId("G-9999"));
            }
        };
        assertThrows(IllegalArgumentException.class, () -> register.resetData(invalid));
        assertEquals(before, register);
    }

    @Test
    void deleteStudent_linkedSibling_preservesGuardianAndOtherLink() {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");

        register.deleteStudent(register.findStudent("S-0007").orElseThrow().getId());

        assertEquals(1, register.getGuardianLinks().size());
        assertTrue(register.getLinkedGuardian("S-0008").isPresent());
        assertEquals(2, register.getGuardianList().size());
    }

    @Test
    void setStudents_removedStudent_removesOnlyItsLink() {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        register.setStudents(List.of(register.findStudent("S-0008").orElseThrow()));
        assertEquals(1, register.getGuardianLinks().size());
        assertTrue(register.getLinkedGuardian("S-0008").isPresent());
    }

    @Test
    void setGuardians_updatedContact_resolvesCurrentRecordAndRejectsDanglingLinks() {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        Guardian original = register.getLinkedGuardian("S-0007").orElseThrow();
        Guardian updated = new Guardian(original.getId(), original.getName(), new GuardianPhone("81234567"));
        register.setGuardians(List.of(updated));
        assertEquals("+65 81234567", register.getLinkedGuardian("S-0007").orElseThrow().getPhone().toString());
        assertThrows(IllegalArgumentException.class, () -> register.setGuardians(List.of()));
        assertEquals(updated, register.getLinkedGuardian("S-0007").orElseThrow());
    }
}
