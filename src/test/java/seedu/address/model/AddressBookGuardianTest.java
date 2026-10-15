package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class AddressBookGuardianTest {
    @Test
    void addGuardian_sortsAndChecksIdAndPhone() {
        AddressBook addressBook = new AddressBook();
        Guardian raj = guardian("G-8102", "Raj Kumar", "61234567");
        Guardian anita = guardian("G-8101", "Anita Lim", "91234567");
        addressBook.addGuardian(raj);
        addressBook.addGuardian(anita);

        assertEquals(List.of(anita, raj), addressBook.getGuardianList());
        assertTrue(addressBook.hasGuardian(guardian("G-8103", "Different Name", "+65 91234567")));
        assertTrue(addressBook.hasGuardian(guardian("G-8101", "Different Name", "81234567")));
        assertFalse(addressBook.hasGuardian(guardian("G-8104", "Different Name", "81234567")));
        assertTrue(addressBook.hasGuardianId(new GuardianId("G-8102")));
        assertFalse(addressBook.hasGuardianId(new GuardianId("G-8105")));
        assertEquals(anita, addressBook.findGuardianByPhone(new GuardianPhone("+6591234567")).orElseThrow());
        assertTrue(addressBook.findGuardianByPhone(new GuardianPhone("31234567")).isEmpty());
        Executable duplicateId = () -> addressBook.addGuardian(guardian("G-8101", "New Name", "31234567"));
        Executable duplicatePhone = () -> addressBook.addGuardian(guardian("G-8106", "New Name", "91234567"));
        assertThrows(IllegalArgumentException.class, duplicateId);
        assertThrows(IllegalArgumentException.class, duplicatePhone);
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getGuardianList().clear());
    }

    @Test
    void guardianOperations_nullArguments_throwNullPointerException() {
        AddressBook addressBook = new AddressBook();

        assertThrows(NullPointerException.class, () -> addressBook.setGuardians(null));
        assertThrows(NullPointerException.class, () -> addressBook.hasGuardian(null));
        assertThrows(NullPointerException.class, () -> addressBook.hasGuardianId(null));
        assertThrows(NullPointerException.class, () -> addressBook.findGuardianByPhone(null));
        assertThrows(NullPointerException.class, () -> addressBook.addGuardian(null));
    }

    @Test
    void resetData_copiesGuardianRecords() {
        AddressBook source = new AddressBook();
        source.addGuardian(guardian("G-8201", "Anita Lim", "91234567"));

        AddressBook copy = new AddressBook(source);

        assertEquals(source.getGuardianList(), copy.getGuardianList());
        assertEquals(source, copy);
        assertEquals(source.hashCode(), copy.hashCode());
        assertTrue(copy.toString().contains("guardians"));
    }

    @Test
    void modelManager_guardianOperationsUseGuardianRegister() {
        ModelManager model = new ModelManager();
        Guardian guardian = guardian("G-8301", "Anita Lim", "91234567");

        model.addGuardian(guardian);

        assertTrue(model.hasGuardian(guardian));
        assertEquals(guardian, model.getGuardianList().getFirst());
        assertEquals(guardian, model.findGuardianByPhone(new GuardianPhone("91234567")).orElseThrow());
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }
}
