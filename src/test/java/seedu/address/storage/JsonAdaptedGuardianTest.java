package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class JsonAdaptedGuardianTest {
    private static final String ID = "G-7101";
    private static final String NAME = "Anita Lim";
    private static final String PHONE = "+65 91234567";

    @Test
    void toModelType_validGuardian_returnsGuardian() throws Exception {
        Guardian guardian = new Guardian(new GuardianId(ID), new GuardianName(NAME), new GuardianPhone(PHONE));

        assertEquals(guardian, new JsonAdaptedGuardian(guardian).toModelType());
        assertEquals(guardian, new JsonAdaptedGuardian(ID, NAME, PHONE).toModelType());
    }

    @Test
    void toModelType_missingField_throwsIllegalValueException() {
        Executable missingId = () -> new JsonAdaptedGuardian(null, NAME, PHONE).toModelType();
        Executable missingName = () -> new JsonAdaptedGuardian(ID, null, PHONE).toModelType();
        Executable missingPhone = () -> new JsonAdaptedGuardian(ID, NAME, null).toModelType();

        assertThrows(IllegalValueException.class, "Guardian record is missing a required field.", missingId);
        assertThrows(IllegalValueException.class, "Guardian record is missing a required field.", missingName);
        assertThrows(IllegalValueException.class, "Guardian record is missing a required field.", missingPhone);
    }

    @Test
    void toModelType_invalidValues_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedGuardian("S-0001", NAME, PHONE).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedGuardian(ID, "Anita2", PHONE).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedGuardian(ID, NAME, "51234567").toModelType());
    }
}
