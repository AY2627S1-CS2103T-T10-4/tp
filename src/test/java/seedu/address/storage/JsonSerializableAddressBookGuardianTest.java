package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class JsonSerializableAddressBookGuardianTest {
    @Test
    void toModelType_guardiansRoundTripAndSort() throws Exception {
        AddressBook source = new AddressBook();
        source.addGuardian(guardian("G-7202", "Raj Kumar", "61234567"));
        source.addGuardian(guardian("G-7201", "Anita Lim", "91234567"));

        AddressBook restored = new JsonSerializableAddressBook(source).toModelType();

        assertEquals(source.getGuardianList(), restored.getGuardianList());
    }

    @Test
    void toModelType_duplicateGuardianIdOrPhone_throwsIllegalValueException() {
        JsonAdaptedGuardian first = new JsonAdaptedGuardian("G-7301", "Anita Lim", "91234567");
        JsonAdaptedGuardian duplicateId = new JsonAdaptedGuardian("G-7301", "Raj Kumar", "61234567");
        JsonAdaptedGuardian duplicatePhone = new JsonAdaptedGuardian("G-7302", "Raj Kumar", "+6591234567");
        Executable sameId = () -> new JsonSerializableAddressBook(List.of(), List.of(),
                List.of(first, duplicateId)).toModelType();
        Executable samePhone = () -> new JsonSerializableAddressBook(List.of(), List.of(),
                List.of(first, duplicatePhone)).toModelType();

        String message = "Guardians list contains duplicate guardian ID or phone number.";
        assertThrows(IllegalValueException.class, message, sameId);
        assertThrows(IllegalValueException.class, message, samePhone);
    }

    @Test
    void constructor_nullGuardianList_returnsEmptyAddressBook() throws Exception {
        assertEquals(new AddressBook(), new JsonSerializableAddressBook(null, null, null).toModelType());
    }

    @Test
    void toModelType_nullGuardianEntry_throwsIllegalValueException() {
        List<JsonAdaptedGuardian> guardians = Collections.singletonList(null);
        Executable conversion = () -> new JsonSerializableAddressBook(List.of(), List.of(), guardians).toModelType();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_GUARDIAN, conversion);
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }
}
